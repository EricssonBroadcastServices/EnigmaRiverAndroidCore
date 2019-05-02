package com.redbeemedia.enigma.core.time;

import org.junit.Assert;
import org.junit.Test;

public class DurationTest {
    @Test
    public void testUnitConversions() {
        Duration oneHour = Duration.hours(1);
        Duration oneMinute = Duration.minutes(1);
        Duration oneSecond = Duration.seconds(1);
        Duration oneMilliSecond = Duration.millis(1);
        Assert.assertEquals(60L, oneHour.inWholeUnits(Duration.Unit.MINUTES));
        Assert.assertEquals(3600L, oneHour.inWholeUnits(Duration.Unit.SECONDS));
        Assert.assertEquals(3600000L, oneHour.inWholeUnits(Duration.Unit.MILLISECONDS));

        Assert.assertEquals(60L, oneMinute.inWholeUnits(Duration.Unit.SECONDS));
        Assert.assertEquals(60000L, oneMinute.inWholeUnits(Duration.Unit.MILLISECONDS));

        Assert.assertEquals(1000L, oneSecond.inWholeUnits(Duration.Unit.MILLISECONDS));

        Assert.assertEquals(1L, oneMilliSecond.inWholeUnits(Duration.Unit.MILLISECONDS));

        Assert.assertEquals(0L, oneMilliSecond.inWholeUnits(Duration.Unit.SECONDS));
        Assert.assertEquals(0L, oneMilliSecond.inWholeUnits(Duration.Unit.MINUTES));
        Assert.assertEquals(0L, oneMilliSecond.inWholeUnits(Duration.Unit.HOURS));

        Assert.assertEquals(0L, oneSecond.inWholeUnits(Duration.Unit.MINUTES));
        Assert.assertEquals(0L, oneMilliSecond.inWholeUnits(Duration.Unit.HOURS));

        Assert.assertEquals(0L, oneMinute.inWholeUnits(Duration.Unit.HOURS));

        Assert.assertEquals(1L, oneHour.inWholeUnits(Duration.Unit.HOURS));

        Assert.assertEquals(2L, Duration.minutes(120).inWholeUnits(Duration.Unit.HOURS));
        Assert.assertEquals(1f, Duration.seconds(3600).inUnits(Duration.Unit.HOURS), 0f);
    }

    @Test
    public void testAdditionSubtractionAndMultiply() {
        Duration oneHour = Duration.hours(1);
        Duration oneMinute = Duration.minutes(1);
        Duration oneSecond = Duration.seconds(1);
        Duration oneMilliSecond = Duration.millis(1);


        Assert.assertEquals(Duration.hours(2) ,oneHour.add(oneMinute.multiply(60)));
        Assert.assertEquals(1 ,oneHour.inWholeUnits(Duration.Unit.HOURS));

        Duration almostAnHour = oneHour.subtract(oneMilliSecond);
        Assert.assertNotEquals(Duration.hours(1) ,almostAnHour);
        Assert.assertEquals(Duration.millis(3600000L-1L) , almostAnHour);
    }
}
