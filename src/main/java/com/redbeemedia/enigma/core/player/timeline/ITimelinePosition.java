package com.redbeemedia.enigma.core.player.timeline;

import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.time.Duration;

public interface ITimelinePosition {
    String toString(TimelinePositionFormat timelinePositionFormat);
    ITimelinePosition add(Duration duration);
    ITimelinePosition subtract(Duration duration);
    Duration subtract(ITimelinePosition other);
    boolean after(ITimelinePosition other);
    boolean before(ITimelinePosition other);
    boolean afterOrEqual(ITimelinePosition other);
    boolean beforeOrEqual(ITimelinePosition other);

    /**
     * Don't call this method.
     * Use {@link IEnigmaPlayerControls#seekTo(ITimelinePosition)}
     * or {@link IEnigmaPlayerControls#seekTo(ITimelinePosition, IControlResultHandler)}
     * instead.
     */
    void seek(IEnigmaPlayerControls controls, IControlResultHandler resultHandler);
}
