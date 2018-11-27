package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.playable.IPlayable;

public abstract class PlayRequest implements IPlayRequest {
    private IPlayable playable;

    public PlayRequest(IPlayable playable) {
        this.playable = playable;
    }

    @Override
    public IPlayable getPlayable() {
        return playable;
    }
}
