package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;

public class MockPlayerImplementation implements IPlayerImplementation, IPlayerImplementationControls, IPlayerImplementationInternals {
    private IPlayerImplementationListener playerImplementationListener;
    private ITimelinePositionFactory timelinePositionFactory;

    @Override
    public void install(IEnigmaPlayerEnvironment environment) {
        environment.setControls(this);
        environment.setInternals(this);
        this.playerImplementationListener = environment.getPlayerImplementationListener();
        this.timelinePositionFactory = environment.getTimelinePositionFactory();
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

    @Override
    public ITimelinePosition getCurrentPosition() {
        return timelinePositionFactory.newPosition(1234L);
    }

    @Override
    public ITimelinePosition getCurrentStartBound() {
        return timelinePositionFactory.newPosition(0L);
    }

    @Override
    public ITimelinePosition getCurrentEndBound() {
        return timelinePositionFactory.newPosition(10000L);
    }
}
