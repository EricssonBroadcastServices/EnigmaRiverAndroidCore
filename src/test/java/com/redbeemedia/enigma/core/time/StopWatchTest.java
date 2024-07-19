// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.time;

import org.junit.Assert;
import org.junit.Test;

public class StopWatchTest {
    @Test
    public void testStopReadStart() {
        MockTimeProvider mockTimeProvider = new MockTimeProvider(1005);
        StopWatch stopWatch = new StopWatch(mockTimeProvider);

        stopWatch.start();

        mockTimeProvider.addTime(63);

        Assert.assertEquals(63, stopWatch.readTime().inWholeUnits(Duration.Unit.MILLISECONDS));

        mockTimeProvider.addTime(8964);

        Duration totalTime = stopWatch.stop();
        Assert.assertEquals(9027, totalTime.inWholeUnits(Duration.Unit.MILLISECONDS));
        mockTimeProvider.addTime(1000);
        Assert.assertEquals(9027, stopWatch.readTime().inWholeUnits(Duration.Unit.MILLISECONDS));
    }

    @Test
    public void testRestart() {
        MockTimeProvider mockTimeProvider = new MockTimeProvider(0);
        StopWatch stopWatch = new StopWatch(mockTimeProvider);

        mockTimeProvider.addTime(5000);
        Assert.assertEquals(Duration.millis(0), stopWatch.readTime());

        stopWatch.start();
        mockTimeProvider.addTime(4004);
        Duration timeElapsed = stopWatch.stop();
        Assert.assertEquals(Duration.millis(4004), timeElapsed);

        mockTimeProvider.addTime(16513);
        stopWatch.start();
        mockTimeProvider.addTime(50);
        Assert.assertEquals(Duration.millis(50), stopWatch.readTime());
        mockTimeProvider.addTime(50);
        Assert.assertEquals(Duration.millis(100), stopWatch.stop());
    }

    @Test(expected = IllegalStateException.class)
    public void testStopWithoutStart() {
        new StopWatch(new MockTimeProvider()).stop();
    }
}
