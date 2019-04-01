package com.redbeemedia.enigma.core.player;

public class MockPlayerImplementation implements IPlayerImplementation, IPlayerImplementationControls {
    private IPlayerImplementationListener playerImplementationListener;

    @Override
    public void install(IEnigmaPlayerEnvironment environment) {
        environment.setControls(this);
        this.playerImplementationListener = environment.getPlayerImplementationListener();
    }

    @Override
    public void load(String url) {
        playerImplementationListener.onLoadCompleted();
    }

    @Override
    public void start() {
        playerImplementationListener.onPlaybackStarted();
    }

    @Override
    public void seekTo(ISeekPosition seekPosition) {
    }

    @Override
    public void release() {
    }
}
