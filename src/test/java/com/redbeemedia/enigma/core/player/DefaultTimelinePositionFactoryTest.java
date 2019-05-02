package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.timeline.TimelinePositionFormat;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.Duration;

import org.junit.Assert;
import org.junit.Test;

public class DefaultTimelinePositionFactoryTest {


    private static IInternalPlaybackSession newLivePlaybackSession() {
        return new MockInternalPlaybackSession(true, 1505574300000L);
    }

    private static IInternalPlaybackSession newVoDPlaybackSession() {
        return new MockInternalPlaybackSession(false, -1L);
    }


    @Test
    public void testUsesRelative() {
        DefaultTimelinePositionFactory factory = new DefaultTimelinePositionFactory();
        factory.onPlaybackSessionChanged(null, newVoDPlaybackSession());
        ITimelinePosition timelinePosition = factory.newPosition(2500);
        ITimelinePosition timelinePosition2 = factory.newPosition(5250);
        long millisDiff = timelinePosition2.subtract(timelinePosition).inWholeUnits(Duration.Unit.MILLISECONDS);
        Assert.assertEquals(5250-2500, millisDiff);
        String formatted = timelinePosition.toString(TimelinePositionFormat.newFormat(duration -> "DUR_FORMAT", "'x'"));
        Assert.assertEquals("DUR_FORMAT", formatted);
    }

    @Test
    public void testUsesAbsolute() {
        DefaultTimelinePositionFactory factory = new DefaultTimelinePositionFactory();
        factory.onPlaybackSessionChanged(null, newLivePlaybackSession());
        ITimelinePosition timelinePosition = factory.newPosition(2500);
        ITimelinePosition timelinePosition2 = factory.newPosition(5250);
        long millisDiff = timelinePosition2.subtract(timelinePosition).inWholeUnits(Duration.Unit.MILLISECONDS);
        Assert.assertEquals(2750, millisDiff);
        String formatted = timelinePosition.toString(TimelinePositionFormat.newFormat(duration -> "DUR_FORMAT", "'x'"));
        Assert.assertEquals("x", formatted);
    }

    @Test
    public void testDifferentPositionTypes() {
        DefaultTimelinePositionFactory factory = new DefaultTimelinePositionFactory();
        IInternalPlaybackSession firstSession = newVoDPlaybackSession();
        IInternalPlaybackSession secondSession = newLivePlaybackSession();
        factory.onPlaybackSessionChanged(null, firstSession);
        ITimelinePosition timelinePosition = factory.newPosition(2500);
        factory.onPlaybackSessionChanged(firstSession, secondSession);
        ITimelinePosition timelinePosition2 = factory.newPosition(5250);
        long millisDiff = timelinePosition2.subtract(timelinePosition).inWholeUnits(Duration.Unit.MILLISECONDS);
        Assert.assertEquals(2750, millisDiff);
        String formatted = timelinePosition2.toString(TimelinePositionFormat.newFormat(duration -> "DUR_FORMAT", "'x'"));
        Assert.assertEquals("x", formatted);
        factory.onPlaybackSessionChanged(secondSession, firstSession);
        ITimelinePosition timelinePosition3 = factory.newPosition(10000L);
        long millisDiff2 = timelinePosition3.subtract(timelinePosition2).inWholeUnits(Duration.Unit.MILLISECONDS);
        Assert.assertEquals(4750, millisDiff2);
        String formatted2 = timelinePosition3.toString(TimelinePositionFormat.newFormat(duration -> "DUR_FORMAT", "'x'"));
        Assert.assertEquals("DUR_FORMAT", formatted2);
    }

    @Test
    public void testSeekWithVoD() {
        DefaultTimelinePositionFactory factory = new DefaultTimelinePositionFactory();
        factory.onPlaybackSessionChanged(null, newVoDPlaybackSession());
        ITimelinePosition timelinePosition = factory.newPosition(5612);
        timelinePosition = timelinePosition.add(Duration.seconds(2)).subtract(Duration.millis(112));
        final Counter seekWithMillisCalled = new Counter();
        timelinePosition.seek(new MockEnigmaPlayerControls() {
            @Override
            public void seekTo(long millis, IControlResultHandler resultHandler) {
                super.seekTo(millis, resultHandler);
                Assert.assertEquals(7500, millis);
                seekWithMillisCalled.count();
            }

        }, new DefaultControlResultHandler(DefaultTimelinePositionFactoryTest.class.getSimpleName()));
        seekWithMillisCalled.assertOnce();
    }

    @Test
    public void testSeekWithLive() {
        DefaultTimelinePositionFactory factory = new DefaultTimelinePositionFactory();
        factory.onPlaybackSessionChanged(null, newLivePlaybackSession());
        ITimelinePosition timelinePosition = factory.newPosition(5612);
        timelinePosition = timelinePosition.add(Duration.millis(3)).subtract(Duration.minutes(0));
        final Counter seekWithMillisCalled = new Counter();
        timelinePosition.seek(new MockEnigmaPlayerControls() {
            @Override
            public void seekTo(long millis, IControlResultHandler resultHandler) {
                super.seekTo(millis, resultHandler);
                Assert.assertEquals(5615, millis);
                seekWithMillisCalled.count();
            }

        }, new DefaultControlResultHandler(DefaultTimelinePositionFactoryTest.class.getSimpleName()));
        seekWithMillisCalled.assertOnce();
    }
}
