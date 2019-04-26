package com.redbeemedia.enigma.core.player.timeline;

import android.os.Handler;

public interface ITimeline {
    void addListener(ITimelineListener listener);
    void addListener(ITimelineListener listener, Handler handler);
    void removeListener(ITimelineListener listener);
    ITimelinePosition getCurrentPosition();
    ITimelinePosition getCurrentStartBound();
    ITimelinePosition getCurrentEndBound();
    boolean getVisibility();
}
