package com.redbeemedia.enigma.core.ads;

import android.util.Log;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Responsible for pasing VOD json data.
 */
class NowtilusVodParser implements INowtilusParser {

    private final static String TAG = NowtilusVodParser.class.getName();

    private final static String CONTENT_AD = "ad";
    private final static String CONTENT_VOD = "vod";
    private final static String CONTENT_DEFAULT = "content";

    @Nullable
    public VastAdEntrySet parseEntries(JSONObject resource) throws JSONException {
        ArrayList<VastAdEntry> entries = new ArrayList<VastAdEntry>();

        if(!resource.has("clips")) {
            Log.w(TAG, "Resource contains no clips");
            return new VastAdEntrySet(entries);
        }

        JSONArray clips = resource.getJSONArray("clips");
        long adStartTime = 0;
        for(int i = 0; i < clips.length(); i++) {
            JSONObject clip = clips.getJSONObject(i);
            String category = clip.optString("category");
            if(category != null) {
                if(category.equals(CONTENT_AD)) {
                    VastAdEntry entry = parseEntry(clip, adStartTime);
                    adStartTime += entry.getDuration();
                    entries.add(entry);
                } else if(category.equals(CONTENT_VOD) || category.equals(CONTENT_DEFAULT)) {
                    adStartTime += clip.getLong("duration");
                } else if(BuildConfig.DEBUG) {
                    throw new RuntimeException("VOD Ad category '" + category + "' not identified.");
                } else {
                    Log.e(TAG, "VOD Ad category '" + category + "' not identified.");
                }
            }
        }
        return new VastAdEntrySet(entries);
    }

    private VastAdEntry parseEntry(JSONObject clip, long adStartTime) throws JSONException {
        long duration = clip.getLong("duration");
        String title = clip.optString("title");
        String id = clip.optString("titleId");
        HashMap<AdEventType, VastImpression> logEntrySets = new HashMap<>();
        EventParser eventParser = new EventParser();

        JSONObject eventsJson = clip.getJSONObject("trackingEvents");
        for(String eventType : EventParser.eventTypes.keySet()) {
            JSONArray eventUrlsJson = eventsJson.optJSONArray(eventType);
            if(eventUrlsJson != null) {
                AdEventType type = eventParser.parse(eventType);
                ArrayList<URL> eventUrls = new ArrayList<>();
                for(int i = 0; i < eventUrlsJson.length(); i++) {
                    URL url = eventParser.parseEventUrl(eventUrlsJson.get(i).toString());
                    if(url!=null){
                        eventUrls.add(url);
                    }
                }
                if (type == AdEventType.Loaded) {
                    type = AdEventType.Start;
                }
                VastImpression vastImpression = logEntrySets.get(type);
                if (vastImpression != null) {
                    vastImpression.getUrls().addAll(eventUrls);
                } else {
                    logEntrySets.put(type, new VastImpression(type, eventUrls));
                }
            }
        }
        // this is should be sent as soon as ad start
        JSONArray impressionUrlsJson = clip.optJSONArray("impressionUrlTemplates");
        if (impressionUrlsJson != null) {
            ArrayList<URL> eventUrls = new ArrayList<>();
            for (int i = 0; i < impressionUrlsJson.length(); i++) {
                URL url = eventParser.parseEventUrl(impressionUrlsJson.get(i).toString());
                if (url != null) {
                    eventUrls.add(url);
                }
            }

            VastImpression vastImpression = logEntrySets.get(AdEventType.Start);
            if (vastImpression != null) {
                vastImpression.getUrls().addAll(eventUrls);
            } else {
                logEntrySets.put(AdEventType.Start, new VastImpression(AdEventType.Start, eventUrls));
            }
        }

        VideoClicks videoClicks = null;
        // Ad click through
        JSONObject videoClicksJson = clip.optJSONObject("videoClicks");
        if (videoClicksJson != null) {
            String clickThroughUrl = videoClicksJson.optString("clickThroughUrl","");
            JSONArray clickTrackingUrlJson = videoClicksJson.optJSONArray("clickTrackingUrls");
            List<URL> clickTrackingUrls = new ArrayList<>();
            if(clickTrackingUrlJson!=null){
                for(int i = 0; i < clickTrackingUrlJson.length(); i++){
                    URL url = eventParser.parseEventUrl(clickTrackingUrlJson.get(i).toString());
                    if (url != null) {
                        clickTrackingUrls.add(url);
                    }
                }
            }
            if (clickThroughUrl != null && !clickThroughUrl.isEmpty()) {
                videoClicks = new VideoClicks(clickThroughUrl, clickTrackingUrls);
            }
        }
        return new VastAdEntry(id, title, adStartTime, duration, logEntrySets, videoClicks);
    }
}
