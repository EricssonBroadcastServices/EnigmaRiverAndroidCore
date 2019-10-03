package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;
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
        this.playFrom = playFrom;
        this.playerImplementationControls = playerImplementationControls;
    }

    @Override
    public void onError(Error error) {
        playResultHandler.onError(error);
    }

    @Override
    public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
        String message = "Manifest load was rejected ("+rejectReason.getType()+"): "+rejectReason.getDetails();
        playResultHandler.onError(new UnexpectedError(message));
    }

    @Override
    public void onDone() {
        if(playFrom == IPlaybackProperties.PlayFrom.BEGINNING
                || playFrom == IPlaybackProperties.PlayFrom.BOOKMARK) {
            IPlayerImplementationControls.ISeekPosition seekPosition = IPlayerImplementationControls.ISeekPosition.TIMELINE_START;
            if(playFrom == IPlaybackProperties.PlayFrom.BOOKMARK) {
                JSONObject bookmarks = jsonObject.optJSONObject("bookmarks");
                if (bookmarks != null) {
                    StreamInfo streamInfo;
                    try {
                        streamInfo = new StreamInfo(jsonObject.optJSONObject("streamInfo"));
                    } catch (JSONException e) {
                        streamInfo = null;
                    }
                    if (streamInfo != null) {
                        if (streamInfo.hasStaticManifest() && bookmarks.has(LAST_VIEWED_OFFSET)) {
                            long lastViewedOffsetMs = bookmarks.optLong(LAST_VIEWED_OFFSET);
                            seekPosition = new IPlayerImplementationControls.TimelineRelativePosition(lastViewedOffsetMs);
                        } else if (!streamInfo.hasStaticManifest() && bookmarks.has(LIVE_TIME)) {
                            long liveTime = bookmarks.optLong(LIVE_TIME);
                            long offset = liveTime - streamInfo.getStart(Duration.Unit.MILLISECONDS);
                            seekPosition = new IPlayerImplementationControls.TimelineRelativePosition(offset);
                        }
                    }
                }
            }
            playerImplementationControls.seekTo(seekPosition, new SeekToControlResultHandler());
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
        public void onError(Error error) {
            playResultHandler.onError(error);
        }
    }

}
