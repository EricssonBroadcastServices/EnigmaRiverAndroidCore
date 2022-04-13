package com.redbeemedia.enigma.core.player.controls;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.player.RejectReason;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.video.IVideoTrack;

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
    public void nextProgram() {
        nextProgram(getDefaultResultHandler());
    }

    @Override
    public void previousProgram() {
        previousProgram(getDefaultResultHandler());
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
    public void setSubtitleTrackById(String trackId) {
        setSubtitleTrackById(trackId, getDefaultResultHandler());
    }

    @Override
    public void setAudioTrack(IAudioTrack track) {
        setAudioTrack(track, getDefaultResultHandler());
    }

    @Override
    public void setAudioTrackById(String trackId) {
        setAudioTrackById(trackId, getDefaultResultHandler());
    }

    @Override
    public void setVideoTrack(IVideoTrack track) {
        setVideoTrack(track, getDefaultResultHandler());
    }

    @Override
    public void setMaxVideoTrackDimensions(int width, int height) {
        setMaxVideoTrackDimensions(width, height, getDefaultResultHandler());
    }

    @Override
    public final void setMaxVideoTrackDimensions(IVideoTrack videoTrack) {
        setMaxVideoTrackDimensions(videoTrack, getDefaultResultHandler());
    }

    @Override
    public final void setMaxVideoTrackDimensions(IVideoTrack videoTrack, IControlResultHandler resultHandler) {
        if(videoTrack == null) {
            resultHandler.onRejected(RejectReason.illegal("videoTrack was null"));
        } else {
            setMaxVideoTrackDimensions(videoTrack.getWidth(), videoTrack.getHeight(), resultHandler);
        }
    }

    protected abstract IControlResultHandler getDefaultResultHandler();
}
