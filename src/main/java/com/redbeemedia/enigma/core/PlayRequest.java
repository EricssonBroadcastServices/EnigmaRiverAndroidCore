package com.redbeemedia.enigma.core;

import com.redbeemedia.enigma.core.error.Error;

public class PlayRequest implements IPlayRequest {
    private IPlayable playable;

    public PlayRequest(IPlayable playable) {
        this.playable = playable;
    }

    @Override
    public void onStarted() {
    }

    @Override
    public void onError(Error error) {
        throw new RuntimeException(error.toString()); //TODO
    }

    @Override
    public IPlayable getPlayable() {
        return playable;
    }
}
