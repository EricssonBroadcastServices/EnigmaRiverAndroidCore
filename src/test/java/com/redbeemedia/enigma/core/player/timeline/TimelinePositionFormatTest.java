package com.redbeemedia.enigma.core.player.timeline;

import com.redbeemedia.enigma.core.time.Duration;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

public class TimelinePositionFormatTest {
    @Test
    public void testPatterns() {
        TimeZone oldDefaultTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            TimelinePositionFormat positionFormat = TimelinePositionFormat.newTimestampFormat("TIME:${minutes}m+${sec}s ", "HH:mm");
            Assert.assertEquals("TIME:712m+42s " , positionFormat.formatDuration(Duration.millis(42762727)));
            Assert.assertEquals("14:30" , positionFormat.formatDate(new Date(1505572214000L)));
        } finally {
            TimeZone.setDefault(oldDefaultTimeZone);
        }
    }
}
