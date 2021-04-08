package com.redbeemedia.enigma.core.player;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.format.IMediaFormatSelector;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playrequest.AdobePrimetime;
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
    private final IPlaybackProperties originalPlaybackProperties;

    public PlaybackSessionSeed(final IPlaybackSessionInfo playbackSessionInfo) {
        this.playable = playbackSessionInfo.getPlayable();
        this.originalPlaybackProperties = playbackSessionInfo.getPlaybackProperties();
        try {
            this.playbackOffset = AndroidThreadUtil.getBlockingOnUiThread(3000, () -> playbackSessionInfo.getCurrentPlaybackOffset());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException("It took to long to get the current playback offset");
        }
    }

    public IPlayRequest createPlayRequest(IPlayResultHandler playResultHandler) {
        IPlaybackProperties playbackProperties = withPlayFrom(originalPlaybackProperties, IPlaybackProperties.PlayFrom.OFFSET(playbackOffset));
        return new PlayRequest(playable, playbackProperties, playResultHandler);
    }

    private static IPlaybackProperties withPlayFrom(IPlaybackProperties playbackProperties, IPlaybackProperties.PlayFrom playFrom) {
        if(playbackProperties instanceof PlaybackProperties) {
            return ((PlaybackProperties) playbackProperties).setPlayFrom(playFrom);
        } else if(playbackProperties instanceof WrappedPlaybackProperties) {
            return ((WrappedPlaybackProperties) playbackProperties).setPlayFrom(playFrom);
        } else {
            return new WrappedPlaybackProperties(playbackProperties, playFrom);
        }
    }

    private static class WrappedPlaybackProperties implements IPlaybackProperties {
        private final IPlaybackProperties wrapped;
        private PlayFrom playFromOverride;

        public WrappedPlaybackProperties(IPlaybackProperties wrapped, PlayFrom playFromOverride) {
            this.wrapped = wrapped;
            this.playFromOverride = playFromOverride;
        }

        @Override
        public PlayFrom getPlayFrom() {
            return playFromOverride;
        }

        public WrappedPlaybackProperties setPlayFrom(PlayFrom playFromOverride) {
            this.playFromOverride = playFromOverride;
            return this;
        }

        @Override
        public IMediaFormatSelector getMediaFormatSelector() {
            return wrapped.getMediaFormatSelector();
        }

        @Nullable
        @Override
        public AdobePrimetime getAdobePrimetime() {
            return wrapped.getAdobePrimetime();
        }
    }
}
