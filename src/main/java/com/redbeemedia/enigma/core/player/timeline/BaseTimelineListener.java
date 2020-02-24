package com.redbeemedia.enigma.core.player.timeline;

import androidx.annotation.NonNull;

public class BaseTimelineListener implements ITimelineListener {
    @Deprecated
    @Override
    public final void _dont_implement_ITimelineListener___instead_extend_BaseTimelineListener_() {
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
    }

    @Override
    public void onCurrentPositionChanged(@NonNull ITimelinePosition timelinePosition) {
    }

    @Override
    public void onBoundsChanged(@NonNull ITimelinePosition start, @NonNull ITimelinePosition end) {
    }
}
