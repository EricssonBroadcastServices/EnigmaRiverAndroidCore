package com.redbeemedia.enigma.core.time;

import org.junit.Assert;
import org.junit.Test;

public class SimpleDurationFormatTest {
    @Test
    public void testJustText() {
        SimpleDurationFormat format = new SimpleDurationFormat("this should be text");
        String formatted = format.format(Duration.minutes(1).add(Duration.seconds(20)));
        Assert.assertEquals("this should be text", formatted);
    }

    @Test
    public void testMinAndSec() {
        SimpleDurationFormat format = new SimpleDurationFormat("${min}:${sec}");
        Assert.assertEquals("01:20", format.format(Duration.minutes(1).add(Duration.seconds(20))));
        Assert.assertEquals("01:00", format.format(Duration.minutes(1).add(Duration.seconds(0))));
        Assert.assertEquals("01:01", format.format(Duration.minutes(1).add(Duration.seconds(1))));
        Assert.assertEquals("57:01", format.format(Duration.minutes(57).add(Duration.seconds(1))));
        Assert.assertEquals("00:01", format.format(Duration.minutes(59).add(Duration.seconds(61))));
        Assert.assertEquals("03:00", format.format(Duration.minutes(62).add(Duration.seconds(60))));
    }

    @Test
    public void testMinutesAndSec() {
        SimpleDurationFormat format = new SimpleDurationFormat("${minutes}:${sec}");
        Assert.assertEquals("1:20", format.format(Duration.minutes(1).add(Duration.seconds(20))));
        Assert.assertEquals("1:00", format.format(Duration.minutes(1).add(Duration.seconds(0))));
        Assert.assertEquals("1:01", format.format(Duration.minutes(1).add(Duration.seconds(1))));
        Assert.assertEquals("57:01", format.format(Duration.minutes(57).add(Duration.seconds(1))));
        Assert.assertEquals("61:01", format.format(Duration.minutes(60).add(Duration.seconds(61))));
    }

    @Test
    public void testSeconds() {
        SimpleDurationFormat format = new SimpleDurationFormat("${seconds} s");
        Assert.assertEquals("0 s", format.format(Duration.minutes(0)));
        Assert.assertEquals("120 s", format.format(Duration.minutes(2)));
        Assert.assertEquals("5 s", format.format(Duration.millis(5999)));
        Assert.assertEquals("86400 s", format.format(Duration.hours(24)));
    }

    @Test
    public void testMillis() {
        SimpleDurationFormat format = new SimpleDurationFormat("${seconds}.${millis} s");
        Assert.assertEquals("0.000 s", format.format(Duration.minutes(0)));
        Assert.assertEquals("120.000 s", format.format(Duration.minutes(2)));
        Assert.assertEquals("5.999 s", format.format(Duration.millis(5999)));
        Assert.assertEquals("86400.000 s", format.format(Duration.hours(24)));
        Assert.assertEquals("0.123 s", format.format(Duration.millis(123)));
    }

    @Test
    public void testHours() {
        SimpleDurationFormat format = new SimpleDurationFormat("${hours}:${min}:${sec}");
        Assert.assertEquals("0:59:35", format.format(Duration.minutes(59).add(Duration.seconds(35)).add(Duration.millis(23))));
        Assert.assertEquals("0:00:00", format.format(Duration.seconds(0)));
        Assert.assertEquals("2:46:40", format.format(Duration.seconds(10000)));
        Assert.assertEquals("100:00:00", format.format(Duration.hours(100)));
    }

    @Test
    public void testHoursAndDays() {
        SimpleDurationFormat format = new SimpleDurationFormat("${days} day(s) and ${hou}:${min}:${sec}");
        Assert.assertEquals("0 day(s) and 00:59:35", format.format(Duration.minutes(59).add(Duration.seconds(35)).add(Duration.millis(23))));
        Assert.assertEquals("0 day(s) and 00:00:00", format.format(Duration.seconds(0)));
        Assert.assertEquals("0 day(s) and 02:46:40", format.format(Duration.seconds(10000)));
        Assert.assertEquals("4 day(s) and 04:00:00", format.format(Duration.hours(100)));
        Assert.assertEquals("11 day(s) and 13:46:40", format.format(Duration.seconds(1000000)));


        format = new SimpleDurationFormat("${days} day(s) and ${hour_in_day}:${min}:${sec}");
        Assert.assertEquals("0 day(s) and 0:59:35", format.format(Duration.minutes(59).add(Duration.seconds(35)).add(Duration.millis(23))));
        Assert.assertEquals("0 day(s) and 0:00:00", format.format(Duration.seconds(0)));
        Assert.assertEquals("0 day(s) and 2:46:40", format.format(Duration.seconds(10000)));
        Assert.assertEquals("4 day(s) and 4:00:00", format.format(Duration.hours(100)));
        Assert.assertEquals("11 day(s) and 13:46:40", format.format(Duration.seconds(1000000)));
    }

    @Test
    public void testCustomVars() {
        SimpleDurationFormat format = new SimpleDurationFormat("My ${MyVar}");
        Assert.assertEquals("My ${MyVar}", format.format(Duration.seconds(10)));
    }
}
