package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.session.ISession;

public class PlayRequest extends BasePlayRequest {
    private final ISession session;
    private final IPlayable playable;
    private final IPlaybackProperties playbackProperties;
    private final IPlayResultHandler resultHandler;

    public PlayRequest(IPlayable playable, IPlayResultHandler resultHandler) {
        this(playable, new PlaybackProperties() ,resultHandler);
    }

    public PlayRequest(ISession session, IPlayable playable, IPlayResultHandler resultHandler) {
        this(session, playable, new PlaybackProperties(), resultHandler);
    }

    public PlayRequest(IPlayable playable, IPlaybackProperties playbackProperties, IPlayResultHandler resultHandler) {
        this(null, playable, playbackProperties, resultHandler);
    }

    public PlayRequest(ISession session, IPlayable playable, IPlaybackProperties playbackProperties, IPlayResultHandler resultHandler) {
        this.session = session;
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

    @Override
    public ISession getSession() {
        return session;
    }
}
