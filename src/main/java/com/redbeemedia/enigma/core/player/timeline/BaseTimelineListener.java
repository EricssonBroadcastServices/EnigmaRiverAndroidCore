package com.redbeemedia.enigma.core.player.timeline;

import com.redbeemedia.enigma.core.ads.AdEventType;
import com.redbeemedia.enigma.core.ads.VastAdEntry;

public class BaseTimelineListener implements ITimelineListener {
    @Deprecated
    @Override
    public final void _dont_implement_ITimelineListener___instead_extend_BaseTimelineListener_() {
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
    }

    @Override
    public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
    }

    @Override
    public void onBoundsChanged(ITimelinePosition start, ITimelinePosition end) {
    }

    @Override
    public void onLivePositionChanged(ITimelinePosition timelinePosition) {
    }

    @Override
    public void onAdEvent(VastAdEntry entry, AdEventType eventType) {

    }
}
