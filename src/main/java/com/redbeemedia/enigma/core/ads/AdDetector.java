package com.redbeemedia.enigma.core.ads;

import android.util.Log;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.SimpleHttpCall;
import com.redbeemedia.enigma.core.player.ITimelinePositionFactory;
import com.redbeemedia.enigma.core.player.timeline.BaseTimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.ITimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.AndroidThreadUtil;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Detect ads inserted in a VAST document.
 */
public class AdDetector extends BaseTimelineListener implements IAdDetector, ITimelineListener {

    private final String TAG = "SSAI";

    private final List<WeakReference<IAdStateListener>> listeners = new ArrayList<>();
    private final IHttpHandler httpHandler;
    private final ITimeline timeline;
    private final ITimelinePositionFactory timelinePositionFactory;
    private final IAdResourceLoaderFactory factory;
    private final AdIncludedTimeline adIncludedTimeline;
    private volatile boolean adPlaying;
    private volatile long programDateTimeAtSyncMs;
    private volatile VastAdEntrySet ads;
    private VastAdEntry lastAd;
    private boolean ssaiEnabled;
    private Duration liveDelay;
    private HashMap<VastAdEntry, List<AdEventType>> broadcastedEvents = new HashMap<>();
    private List<AdBreak> adBreaks;
    private List<Long> contentBreaks;
    private ITimelinePosition jumpOnOriginalScrubTime = null;
    private VastAdEntry currentAdEntry;

    public AdDetector(IHttpHandler httpHandler, ITimeline timeline, ITimelinePositionFactory timelinePositionFactory) {
        this(httpHandler, timeline, timelinePositionFactory, null);
    }

        /**
         * Creates a the ad detector.
         * @param httpHandler Responsible for fetching the VAST document and reporting events.
         * @param timeline ITimeline being observed for changes in the player position.
         * @param timelinePositionFactory
         * @param resourceFactory Optional IAdsDetectorFactory to be used for creating resource requests.
         */
    public AdDetector(IHttpHandler httpHandler, ITimeline timeline, ITimelinePositionFactory timelinePositionFactory, @Nullable IAdResourceLoaderFactory resourceFactory) {
        this.httpHandler = httpHandler;
        this.timeline = timeline;
        this.factory = new AdResourceLoaderFactory(httpHandler);
        this.timelinePositionFactory = timelinePositionFactory;
        this.adIncludedTimeline = new AdIncludedTimeline(timeline, this);
    }

    public boolean isAdPlaying() { return adPlaying; }

    public void setAdPlaying(boolean isAdPlaying) {  adPlaying = isAdPlaying; }

    public void setEnabled(boolean isEnabled) {
        ssaiEnabled = isEnabled;
        if(isEnabled) {
            timeline.addListener(this);
        } else {
            timeline.removeListener(this);
        }
    }

    @Override public IAdIncludedTimeline getTimeline() {
        return adIncludedTimeline;
    }

    /**
     * @return returns the available ad breaks for the current asset.
     */
    List<AdBreak> getAdBreaks() { return adBreaks; }

    public IAdResourceLoaderFactory getFactory() { return factory; }

    public void addListener(IAdStateListener listener) { listeners.add(new WeakReference<>(listener)); }

    @Nullable public VastAdEntrySet getLatestAds() { return ads; }

    public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
        synchronized(this) {
            detectAds();
        }
    }

    /**
     * Will check the VAST url for metadata and report ad events if detected.
     * This will fetch the metadata from the manifest, parse it and call
     * impression links found in the document if the playback position is
     * within the bounds of an ad.
     *
     * @param startTime Latest detected absolute time of the stream.
     */
    public void update(IAdResourceLoader resourceLoader, long startTime) {
        programDateTimeAtSyncMs = startTime;
        resourceLoader.load(entries -> {
            if(ads == null || !ads.equals(entries)) {
                detectAds();
                broadcastedEvents = new HashMap<>();
                ads = entries;
                detectAdBreaks();
                contentBreaks = adIncludedTimeline.detectContentBreaks();
            }
        });
    }

    private void detectAdBreaks() {
        long lastAdFinished = 0;
        long adBreakStart = -1;
        List<VastAdEntry> currentAdBreak = new ArrayList<>();
        adBreaks = new ArrayList<>();
        for(VastAdEntry ad : ads.getEntries()) {
            if(ad.getStartTime() != lastAdFinished) {
                AdBreak adBreak = new AdBreak(timelinePositionFactory.newPosition(adBreakStart), Duration.millis(lastAdFinished - adBreakStart), currentAdBreak);
                adBreaks.add(adBreak);
                adBreakStart = -1;
                currentAdBreak = new ArrayList<>();
            }

            if (adBreakStart == -1) {
                adBreakStart = ad.getStartTime();
            }

            currentAdBreak.add(ad);
            lastAdFinished = ad.getStartTime() + ad.getDuration();
        }

        // When there is single Ad at the start, then we have VASTAdEntry only
        if (!ads.getEntries().isEmpty()) {
            AdBreak adBreak = new AdBreak(timelinePositionFactory.newPosition(adBreakStart), Duration.millis(lastAdFinished - adBreakStart), currentAdBreak);
            adBreaks.add(adBreak);
        }

        Log.d(TAG, "SSAI Total adbreaks:" + adBreaks.size());
    }

    private synchronized void callListeners(VastAdEntry entry, AdEventType eventType) {
        if(lastAd != null && entry == null) {
            broadcast(null, null);
        } else if(entry != null && eventType != null) {
            if(!broadcastedEvents.containsKey(entry)) { broadcastedEvents.put(entry, new ArrayList<>()); }
            if(!Objects.requireNonNull(broadcastedEvents.get(entry)).contains(eventType)) {
                Objects.requireNonNull(broadcastedEvents.get(entry)).add(eventType);
                broadcast(entry, eventType);
            }
        }
    }

    private void detectAds() {
        if(!hasStarted() || ads == null) { return; }
        long currentPosition = getCurrentPosition();
        VastAdEntry entry = ads.getEntry(currentPosition);
        adPlaying = entry != null;

        if(entry != null) {
            AdBreak adBreak = adIncludedTimeline.getAdBreakIfPositionIsBetweenTheAd(timelinePositionFactory.newPosition(currentPosition));
            sendImpression(entry, adBreak);
        }

        callListeners(entry, entry == null ? null : entry.getEventType(currentPosition));
        lastAd = entry;
    }

    private void broadcast(VastAdEntry entry, AdEventType eventType) {
        ArrayList<WeakReference<IAdStateListener>> nullReferences = new ArrayList<>();
        for(WeakReference<IAdStateListener> listenerReference : listeners) {
            if(listenerReference.get() == null) { nullReferences.add(listenerReference); }
            else {
                AndroidThreadUtil.runOnUiThread(() -> listenerReference.get().adStateChanged(this, entry, eventType));
            }
        }
        for(WeakReference<IAdStateListener> reference : nullReferences) { listeners.remove(reference); }
    }

    private void sendImpression(VastAdEntry entry, AdBreak adBreak) {
        VastImpression impression = entry.getImpression();
        if(impression != null && !impression.isSent()) {
            android.util.Log.d(TAG, "Sending impression: " + impression.type);
            sendAdEvents(entry, adBreak, impression);
            SimpleHttpCall apiCall = new SimpleHttpCall("GET");
            for(URL url : impression.getUrls()) {
                try {
                    httpHandler.doHttp(url, apiCall, null);
                    impression.setSent();
                } catch(Exception e) {
                    e.printStackTrace();
                    Log.w("SSAI", e);
                }
            }
        }
    }

    public void sendVideoAdClickImpression() {
        VastAdEntry currentAdEntry = getCurrentAdEntry();
        if (currentAdEntry != null) {
            Log.d(TAG, "*** Sending video click impression *** ");
            SimpleHttpCall apiCall = new SimpleHttpCall("GET");
            VideoClicks videoClicks = currentAdEntry.getVideoClicks();
            if (videoClicks != null) {
                for (URL url : videoClicks.getClickTrackingUrls()) {
                    try {
                        httpHandler.doHttp(url, apiCall, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("SSAI", e);
                    }
                }
            }
        }
    }

    /**
     * @param entry
     * @param adBreak
     * @param impression
     */
    private void sendAdEvents(VastAdEntry entry, AdBreak adBreak, VastImpression impression) {
        currentAdEntry = entry;
        if(impression.type == AdEventType.Complete){
            adBreak.setAdShown(true);
            // virtual button listens to it, so clean it up
            currentAdEntry = null;
        }
        // after setting currentAdEntry value, send event to notify the virtual buttons
        adIncludedTimeline.sendAdEvent(entry, impression.type);
    }

    @Override
    public ITimelinePosition convertToTimeline(long time){
       return timelinePositionFactory.newPosition(time);
    }

    private boolean hasStarted() {
        return timeline != null && timeline.getCurrentPosition() != null && timeline.getCurrentStartBound() != null;
    }

    private long getPlayerPosition() {
        return timeline.getCurrentPosition().subtract(timeline.getCurrentStartBound()).inWholeUnits(Duration.Unit.MILLISECONDS);
    }

    private long getCurrentPosition() {
        return programDateTimeAtSyncMs + getPlayerPosition();
    }

    public List<Long> getContentBreaks() {
        return contentBreaks;
    }

    public ITimelinePosition getJumpOnOriginalScrubTime() {
        return jumpOnOriginalScrubTime;
    }

    public void setJumpOnOriginalScrubTime(ITimelinePosition jumpOnOriginalScrubTime) {
        this.jumpOnOriginalScrubTime = jumpOnOriginalScrubTime;
    }

    public boolean isSsaiEnabled() {
        return ssaiEnabled;
    }

    @Override
    public Duration getLiveDelay() {
        return liveDelay;
    }

    @Override
    public void setLiveDelay(Duration liveDelay) {
        this.liveDelay = liveDelay;
    }

    public VastAdEntry getCurrentAdEntry() {
        return currentAdEntry;
    }
}
