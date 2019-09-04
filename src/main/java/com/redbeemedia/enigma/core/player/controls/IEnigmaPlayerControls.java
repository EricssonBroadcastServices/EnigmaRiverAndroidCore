package com.redbeemedia.enigma.core.player.controls;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;

public interface IEnigmaPlayerControls {
    void start();
    void start(IControlResultHandler resultHandler);
    void pause();
    void pause(IControlResultHandler resultHandler);
    void stop();
    void stop(IControlResultHandler resultHandler);
    void seekTo(long millis); //Seeks to a position along the timeline relative to the start of it
    void seekTo(long millis, IControlResultHandler resultHandler); //Seeks to a position along the timeline relative to the start of it
    void seekTo(StreamPosition streamPosition);
    void seekTo(StreamPosition streamPosition, IControlResultHandler resultHandler);
    void seekTo(ITimelinePosition timelinePos);
    void seekTo(ITimelinePosition timelinePos, IControlResultHandler resultHandler);
    void setVolume(float volume);
    void setVolume(float volume, IControlResultHandler resultHandler);
    void setSubtitleTrack(ISubtitleTrack track);
    void setSubtitleTrack(ISubtitleTrack track, IControlResultHandler resultHandler);

    enum StreamPosition {
        START, LIVE_EDGE
    }
}
