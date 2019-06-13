package com.redbeemedia.enigma.core.task;

import com.redbeemedia.enigma.core.testutil.Counter;

import org.junit.Assert;
import org.junit.Test;

public class RepeaterTest {
    @Test
    public void test() {
        TestTaskFactory taskFactory = new TestTaskFactory(50);
        final Counter runCalled = new Counter();
        Repeater repeater = new Repeater(taskFactory, 100, new Runnable() {
            @Override
            public void run() {
                runCalled.count();
            }
        });
        runCalled.assertNone();
        int expectedRunTimes = 0;

        taskFactory.letTimePass(5000);//Let 5 sec pass
        runCalled.assertNone();

        repeater.executeNow();
        taskFactory.letTimePass(2000);//Let 2 sec pass
        expectedRunTimes += 1;
        runCalled.assertCount(expectedRunTimes);

        repeater.setEnabled(true);
        taskFactory.letTimePass(999);//Let 0.999 sec pass
        repeater.setEnabled(false);
        expectedRunTimes += 10;
        runCalled.assertCount(expectedRunTimes);

        taskFactory.letTimePass(500);//Let half a sec pass
        runCalled.assertCount(expectedRunTimes);

        repeater.setEnabled(true);
        taskFactory.letTimePass(150);
        expectedRunTimes += 2;
        runCalled.assertCount(expectedRunTimes);
        repeater.executeNow();
        expectedRunTimes += 1;
        runCalled.assertCount(expectedRunTimes);
        taskFactory.letTimePass(150);
        expectedRunTimes += 1;
        runCalled.assertCount(expectedRunTimes);

        taskFactory.letTimePass(301);
        expectedRunTimes += 3;
        runCalled.assertCount(expectedRunTimes);

        repeater.setEnabled(true);
        taskFactory.letTimePass(1);
        runCalled.assertCount(expectedRunTimes);

        repeater.setEnabled(false);
        repeater.executeNow();
        taskFactory.letTimePass(10000);
        expectedRunTimes += 1;
        runCalled.assertCount(expectedRunTimes);

        Assert.assertEquals(0, taskFactory.getTasks().size());
    }

    @Test
    public void testFiresOnFirstEnabled() {
        TestTaskFactory taskFactory = new TestTaskFactory(50);
        final Counter runCalled = new Counter();
        Repeater repeater = new Repeater(taskFactory, 100, new Runnable() {
            @Override
            public void run() {
                runCalled.count();
            }
        });
        runCalled.assertNone();
        repeater.setEnabled(true);
        taskFactory.letTimePass(0);
        runCalled.assertOnce();
    }
}
