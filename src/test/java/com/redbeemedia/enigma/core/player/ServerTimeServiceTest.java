// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.network.MockNetworkMonitor;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.task.TestTaskFactory;
import com.redbeemedia.enigma.core.task.TestTaskFactoryProvider;
import com.redbeemedia.enigma.core.testutil.json.JsonObjectBuilder;
import com.redbeemedia.enigma.core.testutil.thread.Interruptor;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.IStopWatch;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockStopWatch;
import com.redbeemedia.enigma.core.time.MockTimeProvider;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class ServerTimeServiceTest {

    @Test
    public void testFirstSyncRetry() {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new TimeoutException());
        mockHttpHandler.queueResponse(new TimeoutException());
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), "{\"epochMillis\" : 123}");
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));
        TestTaskFactory mockTaskFactory = new TestTaskFactory(50);
        final long[] currentTimeMillis = new long[]{977698800000L};
        EnigmaPlayer.EnigmaPlayerLifecycle mockLifecycle = new EnigmaPlayer.EnigmaPlayerLifecycle();
        ServerTimeService serverTimeService = new ServerTimeService(new MockSession().getBusinessUnit(), mockTaskFactory, mockLifecycle) {
            @Override
            protected void handleError(EnigmaError error) {
                boolean handled = false;
                if(error instanceof UnexpectedError) {
                    Exception exception = ((UnexpectedError) error).getException();
                    if(exception instanceof TimeoutException) {
                        handled = true;
                    }
                }
                if(!handled) {
                    Assert.fail(error.getTrace());
                }
            }

            @Override
            protected ITimeProvider newFallbackTimeProvider() {
                return new MockTimeProvider(177294792);
            }

            @Override
            public long getTime(){
                if (getLocalStartTime() == null) {
                    // System time, when its offline
                    return newFallbackTimeProvider().getTime();
                }
                return this.getServerStartTime() + (getLocalTimeMillis() - this.getLocalStartTime());
            }

            @Override
            protected long getLocalTimeMillis() {
                return currentTimeMillis[0];
            }
        };
        mockLifecycle.fireOnStart(null);
        Assert.assertEquals(177294792, serverTimeService.getTime()); //Assert FallbackTimeProvide used
        mockTaskFactory.letTimePass(300);
        Assert.assertEquals(177294792, serverTimeService.getTime()); //Assert FallbackTimeProvide used
        mockTaskFactory.letTimePass(1000);
        int virtualDeviceTimePassed = 107;
        currentTimeMillis[0] += virtualDeviceTimePassed;
        long currentTime = serverTimeService.getTime();
        Assert.assertEquals(123+virtualDeviceTimePassed, currentTime);
    }

    @Test
    public void testWorksOffline() {
        MockNetworkMonitor mockNetworkMonitor = new MockNetworkMonitor();
        MockHttpHandler httpHandler = new MockHttpHandler() {
            @Override
            protected void onIgnoredRequest(URL url, IHttpCall httpCall, IHttpResponseHandler response) {
                System.out.println(url.toString());
            }
        };
        TestTaskFactoryProvider testTaskFactoryProvider = new TestTaskFactoryProvider(250);
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization()
                .setExposureBaseUrl("https://example.com/testWorksOffline/")
                .setNetworkMonitor(mockNetworkMonitor)
                .setTaskFactoryProvider(testTaskFactoryProvider)
                .setHttpHandler(httpHandler));
        MockTimeProvider mockTimeProvider = new MockTimeProvider(5);
        final MockStopWatch mockStopWatch = new MockStopWatch();

        final MockTimeProvider localTimeMillis = new MockTimeProvider(237);

        EnigmaPlayer.EnigmaPlayerLifecycle lifecycle = new EnigmaPlayer.EnigmaPlayerLifecycle();
        ServerTimeService serverTimeService = new ServerTimeService(
                new BusinessUnit("CU_test","BU"),
                EnigmaRiverContext.getTaskFactoryProvider().getTaskFactory(),
                lifecycle) {
            @Override
            protected ITimeProvider newFallbackTimeProvider() {
                return mockTimeProvider;
            }
            @Override
            public long getTime(){
                if (getLocalStartTime() == null) {
                    // System time, when its offline
                    return mockTimeProvider.getTime();
                }
                return this.getServerStartTime() + (getLocalTimeMillis() - this.getLocalStartTime());
            }

            @Override
            protected IStopWatch newStopWatch() {
                return mockStopWatch;
            }

            @Override
            protected long getLocalTimeMillis() {
                return localTimeMillis.getTime();
            }
        };

        mockNetworkMonitor.setInternetAccess(false);

        lifecycle.fireOnStart(null);
        testTaskFactoryProvider.letTimePass(1000);
        mockTimeProvider.addTime(1000);

        Interruptor interruptor = new Interruptor(Thread.currentThread(), 1500);
        interruptor.start();
        Assert.assertTrue(serverTimeService.isReady(Duration.seconds(2)));
        interruptor.cancel();
        Assert.assertFalse("isReady timed out!",interruptor.didInterrupt());

        mockTimeProvider.addTime(130);

        Assert.assertEquals(mockTimeProvider.getTime(), serverTimeService.getTime());

        JsonObjectBuilder serverResponse = new JsonObjectBuilder();
        serverResponse.put("epochMillis", 93752324);
        httpHandler.queueResponseOk(Pattern.compile(".*/customer/CU_test/businessunit/BU/time"), serverResponse.toString());

        //Simulate internet now available
        mockNetworkMonitor.setInternetAccess(true);
        testTaskFactoryProvider.letTimePass(1000);

        interruptor = new Interruptor(Thread.currentThread(), 1000);
        interruptor.start();
        Assert.assertEquals(true, serverTimeService.isReady(Duration.seconds(2)));
        interruptor.cancel();
        Assert.assertFalse("isReady timed out!", interruptor.didInterrupt());

        Assert.assertEquals(93752324 ,serverTimeService.getTime());
        mockTimeProvider.addTime(10000000); //Should only affect fallback timeProvider
        Assert.assertEquals(93752324 ,serverTimeService.getTime());

        localTimeMillis.addTime(10000000); //Should affect
        Assert.assertEquals(103752324, serverTimeService.getTime());

        //Simulate internet goes down
        mockNetworkMonitor.setInternetAccess(false);
        testTaskFactoryProvider.letTimePass(1000);

        interruptor = new Interruptor(Thread.currentThread(), 500);
        interruptor.start();
        Assert.assertEquals(true, serverTimeService.isReady(Duration.seconds(1)));
        interruptor.cancel();
        Assert.assertFalse("isReady timed out!", interruptor.didInterrupt());

        Assert.assertEquals(103752324, serverTimeService.getTime());
        localTimeMillis.addTime(7676);
        Assert.assertEquals(103760000, serverTimeService.getTime());
    }
}
