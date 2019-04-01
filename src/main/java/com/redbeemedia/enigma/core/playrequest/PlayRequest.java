package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.playable.IPlayable;

public class PlayRequest implements IPlayRequest {
    private IPlayable playable;
    private IPlaybackProperties playbackProperties;
    private IPlayResultHandler resultHandler;

    public PlayRequest(IPlayable playable, IPlayResultHandler resultHandler) {
        this(playable, new PlaybackProperties() ,resultHandler);
    }

    public PlayRequest(IPlayable playable, IPlaybackProperties playbackProperties, IPlayResultHandler resultHandler) {
        this.playable = playable;
        this.playbackProperties = playbackProperties;
        this.resultHandler = resultHandler;
    }

    @Override
    public IPlayable getPlayable() {
        return playable;
    }

    @Override
    public IPlaybackProperties getPlaybackProperties() {
        return playbackProperties;
    }

    @Override
    public IPlayResultHandler getResultHandler() {
        return resultHandler;
    }
}
