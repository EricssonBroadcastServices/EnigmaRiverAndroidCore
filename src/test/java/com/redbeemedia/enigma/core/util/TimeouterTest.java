// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.task.TestTaskFactory;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.Duration;

import org.junit.Assert;
import org.junit.Test;

public class TimeouterTest {
    @Test
    public void testTimeout() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestTaskFactory taskFactory = new TestTaskFactory(100);

        Counter onResolveCalls = new Counter();
        Counter onTimeoutCalls = new Counter();

        StringBuilder log = new StringBuilder();

        Timeouter timeouter = new Timeouter(taskFactory, Duration.seconds(1));
        timeouter.setOnResolve(() -> {
            log.append("[onResolve]");
            onResolveCalls.count();
        });
        timeouter.setOnTimeout(() -> {
            log.append("[onTimeout]");
            onTimeoutCalls.count();
        });

        timeouter.start();
        taskFactory.letTimePass(500);
        onResolveCalls.assertNone();
        onTimeoutCalls.assertNone();

        taskFactory.letTimePass(600);
        onResolveCalls.assertCount(1);
        onTimeoutCalls.assertCount(1);

        Assert.assertEquals("Expected onResolve to be called before onTimeout","[onResolve][onTimeout]", log.toString());
    }

    @Test
    public void testOnResolveCalledOnCancel() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestTaskFactory taskFactory = new TestTaskFactory(100);

        Counter onResolveCalls = new Counter();
        Counter onTimeoutCalls = new Counter();

        Timeouter timeouter = new Timeouter(taskFactory, Duration.millis(350));
        timeouter.setOnResolve(() -> {
            onResolveCalls.count();
        });
        timeouter.setOnTimeout(() -> {
            onTimeoutCalls.count();
        });

        timeouter.start();
        taskFactory.letTimePass(200);
        onResolveCalls.assertNone();
        onTimeoutCalls.assertNone();

        timeouter.cancel();
        onResolveCalls.assertCount(1);
        onTimeoutCalls.assertNone();

        taskFactory.letTimePass(1000);
        onResolveCalls.assertCount(1);
        onTimeoutCalls.assertNone();
    }

    @Test
    public void testCancelBeforeStart() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestTaskFactory taskFactory = new TestTaskFactory(100);

        Counter onResolveCalls = new Counter();
        Counter onTimeoutCalls = new Counter();

        Timeouter timeouter = new Timeouter(taskFactory, Duration.seconds(1));
        timeouter.setOnResolve(() -> {
            onResolveCalls.count();
        });
        timeouter.setOnTimeout(() -> {
            onTimeoutCalls.count();
        });

        timeouter.cancel();
        onResolveCalls.assertCount(1);
        timeouter.start();
        taskFactory.letTimePass(500);
        onResolveCalls.assertCount(1);
        onTimeoutCalls.assertNone();

        taskFactory.letTimePass(600);
        onResolveCalls.assertCount(1);
        onTimeoutCalls.assertNone();
    }

    @Test
    public void testCancelAfterStart() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestTaskFactory taskFactory = new TestTaskFactory(100);

        Counter onResolveCalls = new Counter();
        Counter onTimeoutCalls = new Counter();

        Timeouter timeouter = new Timeouter(taskFactory, Duration.seconds(1));
        timeouter.setOnResolve(() -> {
            onResolveCalls.count();
        });
        timeouter.setOnTimeout(() -> {
            onTimeoutCalls.count();
        });

        timeouter.start();
        taskFactory.letTimePass(500);
        onResolveCalls.assertNone();
        onTimeoutCalls.assertNone();

        timeouter.cancel();
        onResolveCalls.assertCount(1);

        taskFactory.letTimePass(600);
        onResolveCalls.assertCount(1);
        onTimeoutCalls.assertNone();
    }

    @Test
    public void testCancelAfterTimeout() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestTaskFactory taskFactory = new TestTaskFactory(100);

        Counter onResolveCalls = new Counter();
        Counter onTimeoutCalls = new Counter();

        Timeouter timeouter = new Timeouter(taskFactory, Duration.seconds(1));
        timeouter.setOnResolve(() -> {
            onResolveCalls.count();
        });
        timeouter.setOnTimeout(() -> {
            onTimeoutCalls.count();
        });

        timeouter.start();
        taskFactory.letTimePass(500);
        onResolveCalls.assertNone();
        onTimeoutCalls.assertNone();

        taskFactory.letTimePass(600);
        onResolveCalls.assertCount(1);
        onTimeoutCalls.assertCount(1);

        timeouter.cancel();
        taskFactory.letTimePass(200);
        onResolveCalls.assertCount(1);
        onTimeoutCalls.assertCount(1);
    }

    @Test
    public void testWithOnResolveNull() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestTaskFactory taskFactory = new TestTaskFactory(100);

        Counter onTimeoutCalls = new Counter();

        Timeouter timeouter = new Timeouter(taskFactory, Duration.seconds(1));
        timeouter.setOnResolve(null);
        timeouter.setOnTimeout(() -> {
            onTimeoutCalls.count();
        });

        timeouter.start();
        taskFactory.letTimePass(500);
        onTimeoutCalls.assertNone();

        timeouter.cancel();

        taskFactory.letTimePass(600);
        onTimeoutCalls.assertNone();
    }

    @Test
    public void testWithOnResolveNull2() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestTaskFactory taskFactory = new TestTaskFactory(100);

        Counter onTimeoutCalls = new Counter();

        Timeouter timeouter = new Timeouter(taskFactory, Duration.millis(123));
        timeouter.setOnResolve(null);
        timeouter.setOnTimeout(() -> {
            onTimeoutCalls.count();
        });

        timeouter.start();
        taskFactory.letTimePass(500);
        onTimeoutCalls.assertCount(1);
    }

    @Test
    public void testWithOnTimeoutNull() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestTaskFactory taskFactory = new TestTaskFactory(100);

        Counter onResolveCalls = new Counter();

        Timeouter timeouter = new Timeouter(taskFactory, Duration.millis(200));
        timeouter.setOnResolve(() -> onResolveCalls.count());
        timeouter.setOnTimeout(null);

        timeouter.start();
        taskFactory.letTimePass(500);
        onResolveCalls.assertCount(1);
    }
}
