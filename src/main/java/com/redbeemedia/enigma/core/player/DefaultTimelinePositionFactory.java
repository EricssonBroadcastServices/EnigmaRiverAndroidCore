package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.timeline.TimelinePositionFormat;
import com.redbeemedia.enigma.core.time.Duration;

import java.util.Date;

/*package-protected*/ class DefaultTimelinePositionFactory implements ITimelinePositionFactory, IPlaybackSessionContainerListener {
    private IInternalPlaybackSession currentPlaybackSession = null;

    @Override
    public ITimelinePosition newPosition(long millis) {
        if(currentPlaybackSession != null) {
            StreamInfo streamInfo = currentPlaybackSession.getStreamInfo();
            if(streamInfo.isLiveStream()) {
                long startUtcMillis = streamInfo.getStartUtcSeconds()*1000L;
                return new UtcTimelinePosition(millis, startUtcMillis);
            }
        }
        return new RelativeTimelinePosition(millis);
    }

    @Override
    public void onPlaybackSessionChanged(IInternalPlaybackSession oldSession, IInternalPlaybackSession newSession) {
        this.currentPlaybackSession = newSession;
    }

    private interface IOffsetPosition extends ITimelinePosition {
        long getOffsetMillis();
    }

    private static class UtcTimelinePosition implements IOffsetPosition {
        private final long offset;
        private final long startTime;
        private final Date date;

        public UtcTimelinePosition(long offset, long startTime) {
            this.offset = offset;
            this.startTime = startTime;
            this.date = new Date(startTime+offset);
        }

        @Override
        public String toString(TimelinePositionFormat timelinePositionFormat) {
            return timelinePositionFormat.formatDate(date);
        }

        @Override
        public ITimelinePosition add(Duration duration) {
            return new UtcTimelinePosition(offset+duration.inWholeUnits(Duration.Unit.MILLISECONDS), startTime);
        }

        @Override
        public ITimelinePosition subtract(Duration duration) {
            return new UtcTimelinePosition(offset-duration.inWholeUnits(Duration.Unit.MILLISECONDS), startTime);
        }

        @Override
        public Duration subtract(ITimelinePosition other) {
            if(other instanceof RelativeTimelinePosition) {
                return Duration.millis(this.offset-((RelativeTimelinePosition) other).millis);
            } else if(other instanceof UtcTimelinePosition) {
                UtcTimelinePosition otherUtc = ((UtcTimelinePosition) other);
                return Duration.millis(this.startTime+this.offset-(otherUtc.startTime+otherUtc.offset));
            } else {
                throw new IllegalArgumentException("Incompatible timeline types");
            }
        }

        @Override
        public void seek(IEnigmaPlayerControls controls, IControlResultHandler resultHandler) {
            controls.seekTo(offset, resultHandler);
        }

        @Override
        public long getOffsetMillis() {
            return offset;
        }
    }

    private static class RelativeTimelinePosition implements IOffsetPosition {
        private final long millis;

        public RelativeTimelinePosition(long millis) {
            this.millis = millis;
        }

        @Override
        public String toString(TimelinePositionFormat timelinePositionFormat) {
            return timelinePositionFormat.formatDuration(Duration.millis(millis));
        }

        @Override
        public ITimelinePosition add(Duration duration) {
            return new RelativeTimelinePosition(millis+duration.inWholeUnits(Duration.Unit.MILLISECONDS));
        }

        @Override
        public ITimelinePosition subtract(Duration duration) {
            return new RelativeTimelinePosition(millis-duration.inWholeUnits(Duration.Unit.MILLISECONDS));
        }

        @Override
        public Duration subtract(ITimelinePosition other) {
            if(other instanceof IOffsetPosition) {
                return Duration.millis(this.millis-((IOffsetPosition) other).getOffsetMillis());
            } else {
                throw new IllegalArgumentException("Incompatible timeline types");
            }
        }

        @Override
        public void seek(IEnigmaPlayerControls controls, IControlResultHandler resultHandler) {
            controls.seekTo(millis, resultHandler);
        }

        @Override
        public long getOffsetMillis() {
            return millis;
        }
    }
}
