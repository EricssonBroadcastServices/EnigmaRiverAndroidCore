package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.playable.IPlayable;

public class PlayRequest implements IPlayRequest {
    private IPlayable playable;
    private IPlayResultHandler resultHandler;

    @Deprecated
    public PlayRequest(IPlayable playable) {
        this.playable = playable;
        this.resultHandler = new DefaultResultHandler();
    }

    public PlayRequest(IPlayable playable, IPlayResultHandler resultHandler) {
        this.playable = playable;
        this.resultHandler = resultHandler;
    }

    @Deprecated
    @Override
    public void onStarted() {
    }

    @Deprecated
    @Override
    public void onError(Error error) {
    }

    @Override
    public IPlayable getPlayable() {
        return playable;
    }

    @Override
    public IPlayResultHandler getResultHandler() {
        return resultHandler;
    }

    private class DefaultResultHandler implements IPlayResultHandler {
        @Override
        public void onStarted(Object object) {
            PlayRequest.this.onStarted();
        }

        @Override
        public void onError(Error error) {
            PlayRequest.this.onError(error);
        }
    }
}
