package com.redbeemedia.enigma.core.player.timeline;

import com.redbeemedia.enigma.core.ads.AdEventType;
import com.redbeemedia.enigma.core.ads.VastAdEntry;
import com.redbeemedia.enigma.core.util.Collector;

public class TimelineListenerCollector extends Collector<ITimelineListener> implements ITimelineListener {
    public TimelineListenerCollector() {
        super(ITimelineListener.class);
    }

    @Deprecated
    @Override
    public final void _dont_implement_ITimelineListener___instead_extend_BaseTimelineListener_() {
        //We want compile time errors here if a new event is added, thus we implement the interface directly.
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        forEach(listener -> listener.onVisibilityChanged(visible));
    }

    @Override
    public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
        forEach(listener -> listener.onCurrentPositionChanged(timelinePosition));
    }

    @Override
    public void onBoundsChanged(ITimelinePosition start, ITimelinePosition end) {
        forEach(listener -> listener.onBoundsChanged(start, end));
    }

    @Override
    public void onLivePositionChanged(ITimelinePosition timelinePosition) {
        forEach(listener -> listener.onLivePositionChanged(timelinePosition));
    }

    @Override
    public void onAdEvent(VastAdEntry entry, AdEventType eventType) {
        forEach(listener -> listener.onAdEvent(entry, eventType));
    }

    @Override
    public void onDashMetadata(EnigmaMetadata metadata) {
        forEach(listener -> listener.onDashMetadata(metadata));
    }

    @Override
    public void onHlsMetadata(EnigmaHlsMediaPlaylist metadata) {
        forEach(listener -> listener.onHlsMetadata(metadata));
    }
}
