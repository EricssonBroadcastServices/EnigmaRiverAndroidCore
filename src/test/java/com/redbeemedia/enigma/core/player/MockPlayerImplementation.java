package com.redbeemedia.enigma.core.player;


public class MockPlayerImplementation implements IPlayerImplementation, IPlayerImplementationControls, IPlayerImplementationInternals {
    private IPlayerImplementationListener playerImplementationListener;

    @Override
    public void install(IEnigmaPlayerEnvironment environment) {
        environment.setControls(this);
        environment.setInternals(this);
        this.playerImplementationListener = environment.getPlayerImplementationListener();
    }

    @Override
    public void load(String url, IPlayerImplementationControlResultHandler resultHandler) {
        resultHandler.onDone();
        playerImplementationListener.onLoadCompleted();
    }

    @Override
    public void start(IPlayerImplementationControlResultHandler resultHandler) {
        resultHandler.onDone();
        playerImplementationListener.onPlaybackStarted();
    }

    @Override
    public void pause(IPlayerImplementationControlResultHandler resultHandler) {
        resultHandler.onDone();
    }

    @Override
    public void stop(IPlayerImplementationControlResultHandler resultHandler) {
        resultHandler.onDone();
    }

    @Override
    public void seekTo(ISeekPosition seekPosition, IPlayerImplementationControlResultHandler resultHandler) {
        resultHandler.onDone();
    }

    @Override
    public void setVolume(float volume, IPlayerImplementationControlResultHandler resultHandler) {
        resultHandler.onDone();
    }

    @Override
    public void release() {
    }
}
