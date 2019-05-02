package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.player.controls.AbstractEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;

public class MockEnigmaPlayerControls extends AbstractEnigmaPlayerControls {
    private EnigmaPlayerState state = EnigmaPlayerState.IDLE;

    @Override
    protected IControlResultHandler getDefaultResultHandler() {
        return new DefaultControlResultHandler("UNIT_TEST");
    }

    @Override
    public void start(IControlResultHandler resultHandler) {
        resultHandler.onDone();
        state = EnigmaPlayerState.PLAYING;
    }

    @Override
    public void pause(IControlResultHandler resultHandler) {
        resultHandler.onDone();
        state = EnigmaPlayerState.PAUSED;
    }

    @Override
    public void stop(IControlResultHandler resultHandler) {
        resultHandler.onDone();
        state = EnigmaPlayerState.IDLE;
    }

    @Override
    public void setVolume(float volume, IControlResultHandler resultHandler) {
        resultHandler.onDone();
    }

    @Override
    public void seekTo(long millis, IControlResultHandler resultHandler) {
        resultHandler.onDone();
    }

    @Override
    public void seekTo(StreamPosition streamPosition, IControlResultHandler resultHandler) {
        resultHandler.onDone();
    }
}
