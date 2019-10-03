package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.task.TestTaskFactory;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

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
        ServerTimeService serverTimeService = new ServerTimeService(new MockSession(), mockTaskFactory) {
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
            protected long getLocalTimeMillis() {
                return currentTimeMillis[0];
            }
        };
        serverTimeService.start(false);
        expectNotSynced(() -> serverTimeService.getTime());
        mockTaskFactory.letTimePass(300);
        expectNotSynced(() -> serverTimeService.getTime());
        mockTaskFactory.letTimePass(1000);
        int virtualDeviceTimePassed = 107;
        currentTimeMillis[0] += virtualDeviceTimePassed;
        long currentTime = serverTimeService.getTime();
        Assert.assertEquals(123+virtualDeviceTimePassed, currentTime);
    }

    private static void expectNotSynced(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            Assert.assertEquals("Not synced!",  e.getMessage());
            return;
        }
        Assert.fail("Expected exception to be thrown");
    }
}
