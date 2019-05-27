package com.redbeemedia.enigma.core.util;

import com.redbeemedia.enigma.core.testutil.Counter;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RuntimeExceptionHandlerTest {

    @Test
    public void testNoExceptionWhenNoneRun() {
        RuntimeExceptionHandler exceptionHandler = new RuntimeExceptionHandler();
        exceptionHandler.rethrowIfAnyExceptions();
    }

    @Test
    public void testNoExceptionSingle() {
        Counter invocationCounter = new Counter();

        RuntimeExceptionHandler exceptionHandler = new RuntimeExceptionHandler();
        exceptionHandler.catchExceptions(new MockJob(false, invocationCounter));

        invocationCounter.assertOnce();

        exceptionHandler.rethrowIfAnyExceptions();

        invocationCounter.assertOnce();
    }

    @Test
    public void testOneException() {
        RuntimeExceptionHandler exceptionHandler = new RuntimeExceptionHandler();

        Counter invocationCounter = new Counter();

        exceptionHandler.catchExceptions(new MockJob(false, invocationCounter));
        exceptionHandler.catchExceptions(new MockJob(true, invocationCounter));
        exceptionHandler.catchExceptions(new MockJob(false, invocationCounter));
        exceptionHandler.catchExceptions(new MockJob(false, invocationCounter));

        invocationCounter.assertCount(4);

        try {
            exceptionHandler.rethrowIfAnyExceptions();
            Assert.fail("Expected exception to have been thrown");
        } catch (RuntimeException e) {
            Assert.assertEquals(MockJob.ERROR_MESSAGE, e.getMessage());
        }
    }

    @Test
    public void testMultipleExceptions() {
        RuntimeExceptionHandler exceptionHandler = new RuntimeExceptionHandler();

        Counter invocationCounter = new Counter();

        exceptionHandler.catchExceptions(new MockJob(true, invocationCounter).setMessage("Fail 1"));
        exceptionHandler.catchExceptions(new MockJob(true, invocationCounter));
        exceptionHandler.catchExceptions(new MockJob(false, invocationCounter));
        exceptionHandler.catchExceptions(new MockJob(true, invocationCounter));
        exceptionHandler.catchExceptions(new MockJob(false, invocationCounter));

        invocationCounter.assertCount(5);

        try {
            exceptionHandler.rethrowIfAnyExceptions();
            Assert.fail("Expected exception to have been thrown");
        } catch (RuntimeException e) {
            Assert.assertEquals("Fail 1", e.getMessage());
            Throwable[] suppressed = e.getSuppressed();
            Assert.assertEquals(2, suppressed.length);
            Assert.assertEquals(RuntimeException.class, suppressed[0].getClass());
            Assert.assertEquals(MockJob.ERROR_MESSAGE, suppressed[0].getMessage());
            Assert.assertEquals(RuntimeException.class, suppressed[1].getClass());
            Assert.assertEquals(MockJob.ERROR_MESSAGE, suppressed[1].getMessage());
        }
    }

    @Test
    public void testCatchFromIterable() {
        List<MockJob> jobs = new ArrayList<>();

        jobs.add(new MockJob(false, new Counter()));
        jobs.add(new MockJob(false, new Counter()));
        jobs.add(new MockJob(true, new Counter()).setMessage("First fail"));
        jobs.add(new MockJob(false, new Counter()));
        jobs.add(new MockJob(false, new Counter()));
        jobs.add(new MockJob(true, new Counter()).setMessage("Second fail"));

        RuntimeExceptionHandler exceptionHandler = new RuntimeExceptionHandler();

        exceptionHandler.catchExceptions(jobs, obj -> obj.run());

        for(MockJob job : jobs) {
            job.invocationCounter.assertOnce();
        }

        try {
            exceptionHandler.rethrowIfAnyExceptions();
            Assert.fail("Expected exception to have been thrown");
        } catch (RuntimeException e) {
            Assert.assertEquals("First fail", e.getMessage());
            Throwable[] suppressed = e.getSuppressed();
            Assert.assertEquals(1, suppressed.length);
            Assert.assertEquals(RuntimeException.class, suppressed[0].getClass());
            Assert.assertEquals("Second fail", suppressed[0].getMessage());
        }
    }


    private static class MockJob implements Runnable {
        private static final String ERROR_MESSAGE = "MockJob failed!";
        private Counter invocationCounter;
        private boolean fail;
        private String message = ERROR_MESSAGE;

        public MockJob(boolean fail, Counter invocationCounter) {
            this.fail = fail;
            this.invocationCounter = invocationCounter;
        }

        public MockJob setMessage(String specialMessage) {
            this.message = specialMessage;
            return this;
        }

        @Override
        public void run() {
            invocationCounter.count();
            if(fail) {
                throw new RuntimeException(message);
            }
        }
    }
}
