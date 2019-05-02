package com.redbeemedia.enigma.core.player.timeline;

import com.redbeemedia.enigma.core.util.IInternalListener;

public interface ITimelineListener extends IInternalListener {
    /**
     * <p>Inspired by {@code org.hamcrest.Matcher} from JUnit lib.</p>
     * <br>
     * <p style="margin-left: 25px; font-weight:bold;">It's easy to ignore JavaDoc, but a bit harder to ignore compile errors .</p>
     * <p style="margin-left: 50px">-- Hamcrest source</p>
     */
    @Deprecated
    void _dont_implement_ITimelineListener___instead_extend_BaseTimelineListener_();

    void onVisibilityChanged(boolean visible);
    void onCurrentPositionChanged(ITimelinePosition timelinePosition);
    void onBoundsChanged(ITimelinePosition start, ITimelinePosition end);
}
