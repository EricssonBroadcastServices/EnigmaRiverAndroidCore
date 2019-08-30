package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.timeline.TimelinePositionFormat;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.OpenContainer;

import java.util.Date;

/*package-protected*/ class DefaultTimelinePositionFactory implements ITimelinePositionFactory, IPlaybackSessionContainerListener, ProgramTracker.IProgramChangedListener {
    private final OpenContainer<IInternalPlaybackSession> currentPlaybackSession = new OpenContainer<>(null);
    private final OpenContainer<IProgram> currentProgram = new OpenContainer<>(null);

    @Override
    public ITimelinePosition newPosition(long millis) {
        return new TimelinePosition(millis);
    }

    private String formatPosition(long offsetMillis, TimelinePositionFormat positionFormat) {
        StreamInfo streamInfo = null;
        synchronized (currentPlaybackSession) {
            if(currentPlaybackSession.value != null) {
                streamInfo = currentPlaybackSession.value.getStreamInfo();
            }
        }
        if(streamInfo != null) {
            IProgram program;
            synchronized (currentProgram) {
                program = currentProgram.value;
            }
            if(streamInfo.isLiveStream() && streamInfo.hasStartUtcSeconds()) {
                return positionFormat.formatDate(new Date(streamInfo.getStartUtcSeconds()+offsetMillis));
            } else if(streamInfo.hasStartUtcSeconds() && program != null) {
                return positionFormat.formatDuration(Duration.millis(offsetMillis-program.getStartUtcMillis()+streamInfo.getStartUtcSeconds()*1000L));
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
        private final long offsetMillis;

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
        public ITimelinePosition subtract(Duration duration) {
            return new TimelinePosition(offsetMillis - duration.inWholeUnits(Duration.Unit.MILLISECONDS));
        }

        @Override
        public Duration subtract(ITimelinePosition other) {
            return Duration.millis(offsetMillis-((TimelinePosition) other).offsetMillis);
        }

        @Override
        public void seek(IEnigmaPlayerControls controls, IControlResultHandler resultHandler) {
            controls.seekTo(offsetMillis, resultHandler);
        }
    }
}
