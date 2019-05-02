package com.redbeemedia.enigma.core.player.controls;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;

public abstract class AbstractEnigmaPlayerControls implements IEnigmaPlayerControls {
    @Override
    public final void start() {
        start(getDefaultResultHandler());
    }

    @Override
    public final void pause() {
        pause(getDefaultResultHandler());
    }

    @Override
    public final void stop() {
        stop(getDefaultResultHandler());
    }

    @Override
    public final void seekTo(long millis) {
        seekTo(millis, getDefaultResultHandler());
    }

    @Override
    public final void seekTo(StreamPosition timelinePos) {
        seekTo(timelinePos, getDefaultResultHandler());
    }

    @Override
    public void seekTo(ITimelinePosition timelinePos) {
        seekTo(timelinePos, getDefaultResultHandler());
    }

    @Override
    public void seekTo(ITimelinePosition timelinePos, IControlResultHandler resultHandler) {
        timelinePos.seek(this, resultHandler);
    }

    @Override
    public void setVolume(float volume) {
        setVolume(volume, getDefaultResultHandler());
    }

    protected abstract IControlResultHandler getDefaultResultHandler();
}
