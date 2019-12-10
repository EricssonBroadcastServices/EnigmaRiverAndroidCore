package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.playrequest.PlayRequest;
import com.redbeemedia.enigma.core.playrequest.PlaybackProperties;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.AndroidThreadUtil;

import java.util.concurrent.TimeoutException;

/**
 * This class is like a marker in a playback session. It contains all the information needed to
 * recreate a playback-session. It is the <i>seed</i> of a playback session.
 */
/*package-protected*/ class PlaybackSessionSeed {
    private final IPlayable playable;
    private final Duration playbackOffset;

    public PlaybackSessionSeed(final IPlaybackSessionInfo playbackSessionInfo) {
        this.playable = playbackSessionInfo.getPlayable();
        try {
            this.playbackOffset = AndroidThreadUtil.getBlockingOnUiThread(3000, () -> playbackSessionInfo.getCurrentPlaybackOffset());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException("It took to long to get the current playback offset");
        }
    }

    public IPlayRequest createPlayRequest(IPlayResultHandler playResultHandler) {
        PlaybackProperties playbackProperties = new PlaybackProperties().setPlayFrom(IPlaybackProperties.PlayFrom.OFFSET(playbackOffset));
        return new PlayRequest(playable, playbackProperties, playResultHandler);
    }
}
