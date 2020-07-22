package com.redbeemedia.enigma.core.player.timeline;

import android.os.Handler;

import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

import java.util.Objects;

public class SimpleTimeline implements ITimeline {
    private final TimelineListenerCollector collector = new TimelineListenerCollector();
    private final OpenContainer<ITimelinePosition> currentPosition = new OpenContainer<>(null);
    private final OpenContainer<Bounds> currentBounds = new OpenContainer<>(null);
    private final OpenContainer<Boolean> visibility = new OpenContainer<>(true);

    @Override
    public void addListener(ITimelineListener listener) {
        collector.addListener(listener);
    }

    @Override
    public void addListener(ITimelineListener listener, Handler handler) {
        collector.addListener(listener, new HandlerWrapper(handler));
    }

    @Override
    public void removeListener(ITimelineListener listener) {
        collector.removeListener(listener);
    }

    @Override
    public ITimelinePosition getCurrentPosition() {
        return OpenContainerUtil.getValueSynchronized(currentPosition);
    }

    @Override
    public ITimelinePosition getCurrentStartBound() {
        synchronized (currentBounds) {
            return currentBounds.value != null ? currentBounds.value.start : null;
        }
    }

    @Override
    public ITimelinePosition getCurrentEndBound() {
        synchronized (currentBounds) {
            return currentBounds.value != null ? currentBounds.value.end : null;
        }
    }

    @Override
    public boolean getVisibility() {
        return OpenContainerUtil.getValueSynchronized(visibility);
    }

    public void setBounds(ITimelinePosition start, ITimelinePosition end) {
        OpenContainerUtil.setValueSynchronized(currentBounds, new Bounds(start, end), (oldValue, newValue) -> {
            collector.onBoundsChanged(newValue.start, newValue.end);
        });
    }

    public void setCurrentPosition(ITimelinePosition position) {
        OpenContainerUtil.setValueSynchronized(currentPosition, position, (oldValue, newValue) -> {
            collector.onCurrentPositionChanged(position);
        });
    }

    public void setVisibility(boolean visibility) {
        OpenContainerUtil.setValueSynchronized(this.visibility, visibility, (oldValue, newValue) -> {
            collector.onVisibilityChanged(newValue.booleanValue());
        });
    }

    private static class Bounds {
        private final ITimelinePosition start;
        private final ITimelinePosition end;

        public Bounds(ITimelinePosition start, ITimelinePosition end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Bounds)) {
                return false;
            }
            Bounds other = (Bounds) obj;
            return Objects.equals(this.start, other.start) && Objects.equals(this.end, other.end);
        }
    }
}
