package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.timeline.TimelinePositionFormat;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

import java.util.Date;

/*package-protected*/ class DefaultTimelinePositionFactory implements ITimelinePositionFactory, IPlaybackSessionContainerListener, ProgramTracker.IProgramChangedListener {
    private final OpenContainer<IInternalPlaybackSession> currentPlaybackSession = new OpenContainer<>(null);
    private final OpenContainer<IProgram> currentProgram = new OpenContainer<>(null);

    @Override
    public ITimelinePosition newPosition(long millis) {
        return new TimelinePosition(millis);
    }

    private String formatPosition(long offsetMillis, TimelinePositionFormat positionFormat) {
        IStreamInfo streamInfo = null;
        synchronized (currentPlaybackSession) {
            if(currentPlaybackSession.value != null) {
                streamInfo = currentPlaybackSession.value.getStreamInfo();
            }
        }
        if(streamInfo != null) {
            IProgram program = OpenContainerUtil.getValueSynchronized(currentProgram);
            if(streamInfo.isLiveStream() && streamInfo.hasStart()) {
                return positionFormat.formatDate(new Date(streamInfo.getStart(Duration.Unit.MILLISECONDS)+offsetMillis));
            } else if(streamInfo.hasStart() && program != null) {
                return positionFormat.formatDuration(Duration.millis(offsetMillis-program.getStartUtcMillis()+streamInfo.getStart(Duration.Unit.MILLISECONDS)));
            }
        }
        return positionFormat.formatDuration(Duration.millis(offsetMillis));
    }

    @Override
    public void onPlaybackSessionChanged(IInternalPlaybackSession oldSession, IInternalPlaybackSession newSession) {
        synchronized (currentPlaybackSession) {
            currentPlaybackSession.value = newSession;
        }
    }


    @Override
    public void onProgramChanged(IProgram oldProgram, IProgram newProgram) {
        synchronized (currentProgram) {
            currentProgram.value = newProgram;
        }
    }

    private class TimelinePosition implements ITimelinePosition {
        public final long offsetMillis;

        public TimelinePosition(long offsetMillis) {
            this.offsetMillis = offsetMillis;
        }

        @Override
        public String toString(TimelinePositionFormat timelinePositionFormat) {
            return formatPosition(offsetMillis, timelinePositionFormat);
        }

        @Override
        public ITimelinePosition add(Duration duration) {
            return new TimelinePosition(offsetMillis +duration.inWholeUnits(Duration.Unit.MILLISECONDS));
        }

        @Override
        public long getStart() {
            return offsetMillis;
        }

        @Override
        public ITimelinePosition subtract(Duration duration) {
            return new TimelinePosition(offsetMillis - duration.inWholeUnits(Duration.Unit.MILLISECONDS));
        }

        @Override
        public Duration subtract(ITimelinePosition other) {
            return Duration.millis(offsetMillis-((TimelinePosition) other).offsetMillis);
        }

        @Override
        public boolean after(ITimelinePosition other) {
            if(other == null) {
                return false;
            }
            return this.offsetMillis > ((TimelinePosition) other).offsetMillis;
        }

        @Override
        public boolean before(ITimelinePosition other) {
            if(other == null) {
                return false;
            }
            return other.after(this);
        }

        @Override
        public boolean afterOrEqual(ITimelinePosition other) {
            if(other == null) {
                return false;
            }
            return this.equals(other) || this.after(other);
        }

        @Override
        public boolean beforeOrEqual(ITimelinePosition other) {
            if(other == null) {
                return false;
            }
            return this.equals(other) || this.before(other);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TimelinePosition && ((TimelinePosition) obj).offsetMillis == this.offsetMillis;
        }

        @Override
        public int hashCode() {
            return Long.valueOf(offsetMillis).hashCode();
        }

        @Override
        public void seek(IEnigmaPlayerControls controls, IControlResultHandler resultHandler) {
            controls.seekTo(offsetMillis, resultHandler);
        }

        @Override
        public String toString() {
            return "TimelinePosition{ offsetMillis=" + offsetMillis + " }";
        }
    }
}
