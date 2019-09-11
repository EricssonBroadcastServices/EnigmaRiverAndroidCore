package com.redbeemedia.enigma.core.player.controls;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;

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

    @Override
    public void setSubtitleTrack(ISubtitleTrack track) {
        setSubtitleTrack(track, getDefaultResultHandler());
    }

    @Override
    public void setAudioTrack(IAudioTrack track) {
        setAudioTrack(track, getDefaultResultHandler());
    }

    protected abstract IControlResultHandler getDefaultResultHandler();
}
