// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.ads.AdIncludedTimeline;
import com.redbeemedia.enigma.core.ads.IAdDetector;
import com.redbeemedia.enigma.core.ads.IAdIncludedTimeline;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.time.Duration;

import org.json.JSONException;
import org.json.JSONObject;

/*package-protected*/ abstract class StartPlaybackControlResultHandler extends BasePlayerImplementationControlResultHandler {
    private static final String LAST_VIEWED_OFFSET = "lastViewedOffset";
    private static final String LIVE_TIME = "liveTime";
    private static final long BOOKMARK_OFFSET_DELIMITER_MS = 10000;

    private final IPlayResultHandler playResultHandler;
    private final JSONObject jsonObject;
    private final IPlaybackProperties.PlayFrom playFrom;
    private final IPlayerImplementationControls playerImplementationControls;
    private final IAdDetector adDetector;
    public StartPlaybackControlResultHandler(IPlayResultHandler playResultHandler, JSONObject jsonObject, IPlaybackProperties.PlayFrom playFrom, IPlayerImplementationControls playerImplementationControls, IAdDetector adDetector) {
        this.playResultHandler = playResultHandler;
        this.jsonObject = jsonObject;
        this.playFrom = playFrom != null ? playFrom : IPlaybackProperties.PlayFrom.PLAYER_DEFAULT;
        this.playerImplementationControls = playerImplementationControls;
        this.adDetector = adDetector;
    }

    @Override
    public void onError(EnigmaError error) {
        playResultHandler.onError(error);
    }

    @Override
    public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
        String message = "Manifest load was rejected ("+rejectReason.getType()+"): "+rejectReason.getDetails();
        playResultHandler.onError(new UnexpectedError(message));
    }

    @Override
    public void onDone() {
        for(IPlaybackProperties.PlayFrom.PlayFromPreference preference : playFrom.getPreferences()) {
            boolean preferenceApplicable = seekToPreference(preference);
            if(preferenceApplicable) {
                break; //Done
            }
        }
    }

    /**
     * @param preference
     * @return true if preference was applicable
     */
    private boolean seekToPreference(IPlaybackProperties.PlayFrom.PlayFromPreference preference) {
        boolean applicable = false;
        switch (preference) {
            case LIVE_EDGE: {
                JsonStreamInfo streamInfo = getStreamInfo();
                if (streamInfo != null && streamInfo.isLiveStream())
                {
                    playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.LIVE_EDGE, new SeekToControlResultHandler());
                    applicable = true;
                    break;
                }
                // LIVE_EDGE is the default, if streamInfo is unavailable (eg. when playing a downloaded asset)
                // then fallback to BEGINNING
            }
            case BEGINNING: {
                playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.TIMELINE_START, new SeekToControlResultHandler());
                applicable = true;
            } break;
            case BOOKMARK: {
                JSONObject bookmarks = jsonObject.optJSONObject("bookmarks");
                if (bookmarks != null) {
                    JsonStreamInfo streamInfo = getStreamInfo();
                    if (streamInfo != null) {
                        if (streamInfo.hasStaticManifest()) {
                            if(bookmarks.has(LAST_VIEWED_OFFSET)) {
                                long lastViewedOffsetMs = bookmarks.optLong(LAST_VIEWED_OFFSET);
                                if(lastViewedOffsetMs<0){
                                    lastViewedOffsetMs = 0;
                                }
                                long duration = jsonObject.optLong("durationInMs") / 1000;
                                if (duration - lastViewedOffsetMs < BOOKMARK_OFFSET_DELIMITER_MS) {
                                    playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.TIMELINE_START, new SeekToControlResultHandler());
                                    return true;
                                }
                                IAdIncludedTimeline timeline = adDetector.getTimeline();
                                long adsForGivenScrubTime = 0;
                                if (adDetector.isSsaiEnabled() && timeline instanceof AdIncludedTimeline) {
                                    AdIncludedTimeline adIncludedTimeline = (AdIncludedTimeline) timeline;
                                    adsForGivenScrubTime = adIncludedTimeline.getTotalAdDurationFromThisTime(adDetector.convertToTimeline(lastViewedOffsetMs));
                                }
                                IPlayerImplementationControls.TimelineRelativePosition seekPosition = new IPlayerImplementationControls.TimelineRelativePosition(lastViewedOffsetMs + adsForGivenScrubTime);
                                playerImplementationControls.seekTo(seekPosition, new SeekToControlResultHandler());
                                applicable = true;
                            } else if(bookmarks.has(LIVE_TIME) && streamInfo.hasStart()) {
                                long liveTime = bookmarks.optLong(LIVE_TIME);
                                long offset = liveTime - streamInfo.getStart(Duration.Unit.MILLISECONDS);
                                IPlayerImplementationControls.TimelineRelativePosition seekPosition = new IPlayerImplementationControls.TimelineRelativePosition(offset);
                                playerImplementationControls.seekTo(seekPosition, new SeekToControlResultHandler());
                                applicable = true;
                            }
                        } else if (!streamInfo.hasStaticManifest() && bookmarks.has(LIVE_TIME)) {
                            long liveTime = bookmarks.optLong(LIVE_TIME);
                            long offset = liveTime - streamInfo.getStart(Duration.Unit.MILLISECONDS);
                            IPlayerImplementationControls.TimelineRelativePosition seekPosition = new IPlayerImplementationControls.TimelineRelativePosition(offset);
                            playerImplementationControls.seekTo(seekPosition, new SeekToControlResultHandler());
                            applicable = true;
                        }
                    }
                }
            } break;
            case OFFSET: {
                if (playFrom instanceof IPlaybackProperties.PlayFromOffset) {
                    IPlaybackProperties.PlayFromOffset playFromOffset = (IPlaybackProperties.PlayFromOffset) (playFrom);
                    Duration offsetToPlay = playFromOffset.getOffset();
                    if (offsetToPlay.inUnits(Duration.Unit.MILLISECONDS) > 0) {
                        IPlayerImplementationControls.TimelineRelativePosition seekPosition = new IPlayerImplementationControls.TimelineRelativePosition(offsetToPlay.inWholeUnits(Duration.Unit.MILLISECONDS));
                        playerImplementationControls.seekTo(seekPosition, new SeekToControlResultHandler());
                        applicable = true;
                    }
                }
            }
            break;

        }
        return applicable;
    }

    private JsonStreamInfo getStreamInfo() {
        try {
            boolean audioOnly = jsonObject.optBoolean("audioOnly",false);
            MediaType mediaType = audioOnly ? MediaType.AUDIO : MediaType.VIDEO;
            return new JsonStreamInfo(jsonObject.optJSONObject("streamInfo"), mediaType);
        } catch (JSONException e) {
            return null;
        }
    }

    protected abstract void onLogDebug(String message);

    private class SeekToControlResultHandler extends  BasePlayerImplementationControlResultHandler {
        @Override
        public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
            String message = "Could not start from requested position ("+rejectReason.getType()+"): "+rejectReason.getDetails();
            onLogDebug(message);
        }

        @Override
        public void onError(EnigmaError error) {
            playResultHandler.onError(error);
        }
    }

}
