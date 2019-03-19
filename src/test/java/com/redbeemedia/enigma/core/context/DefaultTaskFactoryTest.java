package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.Flag;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class DefaultTaskFactoryTest {
    @Test
    public void testThreadStart() throws InterruptedException, TaskException {
        DefaultTaskFactory defaultTaskFactory = new DefaultTaskFactory();
        final Counter runCalled = new Counter();
        final Flag doneFlag = new Flag();
        ITask task = defaultTaskFactory.newTask(new Runnable() {
            @Override
            public void run() {
                runCalled.count();
                doneFlag.setFlag();
            }
        });
        runCalled.assertNone();
        task.start();
        try {
            waitUntilTrue(doneFlag, 2000);
        } catch (TimeoutException e) {
            Assert.fail("Thread not started within acceptable time");
        }
    }

    @Test
    public void testThreadCanceledBeforeStart() throws InterruptedException, TaskException {
        DefaultTaskFactory defaultTaskFactory = new DefaultTaskFactory();
        final Counter runCalled = new Counter();
        ITask task = defaultTaskFactory.newTask(new Runnable() {
            @Override
            public void run() {
                runCalled.count();
                Assert.fail("Should never have been called!");
            }
        });
        runCalled.assertNone();
        task.cancel(0);
        task.start();
        Thread.sleep(100);
        runCalled.assertNone();
    }

    private static void waitUntilTrue(Flag flag, long timeout) throws TimeoutException {
        long startWait = System.currentTimeMillis();
        while(System.currentTimeMillis()-startWait < timeout) {
            try {
                synchronized (flag) {
                    if(flag.isTrue()) {
                        return;
                    }
                }
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new TimeoutException();
    }
}
