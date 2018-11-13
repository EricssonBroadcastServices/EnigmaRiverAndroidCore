package com.redbeemedia.enigma.core;

public class PlayRequest implements IPlayRequest {
    private IPlayable playable;

    public PlayRequest(IPlayable playable) {
        this.playable = playable;
    }

    @Override
    public void onStarted() {
    }

    @Override
    public void onError(String errorMessage) {
        throw new RuntimeException(errorMessage);
    }

    @Override
    public IPlayable getPlayable() {
        return playable;
    }
}
