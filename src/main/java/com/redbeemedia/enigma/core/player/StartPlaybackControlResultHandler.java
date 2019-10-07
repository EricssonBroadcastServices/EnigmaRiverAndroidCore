package com.redbeemedia.enigma.core.player;

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

    private final IPlayResultHandler playResultHandler;
    private final JSONObject jsonObject;
    private final IPlaybackProperties.PlayFrom playFrom;
    private final IPlayerImplementationControls playerImplementationControls;

    public StartPlaybackControlResultHandler(IPlayResultHandler playResultHandler, JSONObject jsonObject, IPlaybackProperties.PlayFrom playFrom, IPlayerImplementationControls playerImplementationControls) {
        this.playResultHandler = playResultHandler;
        this.jsonObject = jsonObject;
        this.playFrom = playFrom != null ? playFrom : IPlaybackProperties.PlayFrom.PLAYER_DEFAULT;
        this.playerImplementationControls = playerImplementationControls;
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
            case BEGINNING: {
                playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.TIMELINE_START, new SeekToControlResultHandler());
                applicable = true;
            } break;
            case BOOKMARK: {
                JSONObject bookmarks = jsonObject.optJSONObject("bookmarks");
                if (bookmarks != null) {
                    StreamInfo streamInfo = getStreamInfo();
                    if (streamInfo != null) {
                        if (streamInfo.hasStaticManifest()) {
                            if(bookmarks.has(LAST_VIEWED_OFFSET)) {
                                long lastViewedOffsetMs = bookmarks.optLong(LAST_VIEWED_OFFSET);
                                IPlayerImplementationControls.TimelineRelativePosition seekPosition = new IPlayerImplementationControls.TimelineRelativePosition(lastViewedOffsetMs);
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
            case LIVE_EDGE: {
                StreamInfo streamInfo = getStreamInfo();
                if(streamInfo.isLiveStream()) {
                    playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.LIVE_EDGE, new SeekToControlResultHandler());
                    applicable = true;
                }
            } break;
        }
        return applicable;
    }

    private StreamInfo getStreamInfo() {
        try {
            return new StreamInfo(jsonObject.optJSONObject("streamInfo"));
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
