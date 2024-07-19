// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.testutil.thread.Interruptor;
import com.redbeemedia.enigma.core.testutil.thread.ThreadHalter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AndroidThreadUtilTest {
    private AndroidThreadUtil.IAndroidThreadHandler storedThreadHandler;
    private ExecutorService executorService;

    @Before
    public void setupExecutorService() {
        executorService = Executors.newCachedThreadPool();
    }

    @After
    public void terminateExecutorService() throws InterruptedException {
        executorService.shutdown();
        Assert.assertTrue("Could not terminate threads",executorService.awaitTermination(1000, TimeUnit.MILLISECONDS));
        executorService = null;
    }

    @Before
    public void storeThreadHandler() {
        storedThreadHandler = AndroidThreadUtil.getThreadHandler();
    }

    @After
    public void restoreThreadHandler() {
        AndroidThreadUtil.setAndroidThreadHandler(storedThreadHandler);
        storedThreadHandler = null;
    }

    @After
    public void resetInterruptedFlag() {
        Thread.currentThread().interrupted();
    }

    @Test
    public void testGetOnUIThread() throws InterruptedException {
        final Thread uiThread = Thread.currentThread();

        final List<Runnable> postedRunnables = new ArrayList<>();
        final ThreadHalter mainTreadHalter = new ThreadHalter();
        AndroidThreadUtil.setAndroidThreadHandler(new AndroidThreadUtil.IAndroidThreadHandler() {
            @Override
            public boolean isOnUiThread() {
                return Thread.currentThread() == uiThread;
            }

            @Override
            public void postToUiThread(Runnable runnable) {
                synchronized (postedRunnables) {
                    postedRunnables.add(runnable);
                }
                mainTreadHalter.proceed();
            }
        });

        String value = AndroidThreadUtil.getBlockingOnUiThread(new Callable<String>() {
            @Override
            public String call() throws Exception {
                AndroidThreadUtil.verifyCalledFromUiThread();
                return "Success";
            }
        });

        Assert.assertEquals("Success", value);

        final Counter threadRunnableFinished = new Counter();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mainTreadHalter.dependOnCurrentThread();
                try {
                    boolean gotException = false;
                    try {
                        AndroidThreadUtil.verifyCalledFromUiThread();
                    } catch (IllegalStateException e) {
                        gotException = true;
                    }
                    Assert.assertTrue("Expected exception from line before!", gotException);

                    String value = AndroidThreadUtil.getBlockingOnUiThread(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            AndroidThreadUtil.verifyCalledFromUiThread();
                            return "Successness";
                        }
                    });
                    Assert.assertEquals("Successness", value);

                    threadRunnableFinished.count();

                    mainTreadHalter.proceed();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "TestThread").start();

        mainTreadHalter.waitForGo();
        threadRunnableFinished.assertNone();

        synchronized (postedRunnables) {
            for(Runnable runnable : postedRunnables) {
                runnable.run();
            }
            postedRunnables.clear();
        }

        mainTreadHalter.waitForGo();
        threadRunnableFinished.assertOnce();
    }

    @Test
    public void testGetOnUIThreadWithTimeout() throws InterruptedException, TimeoutException {
        final Thread uiThread = Thread.currentThread();

        final List<Runnable> postedRunnables = new ArrayList<>();
        AndroidThreadUtil.setAndroidThreadHandler(new AndroidThreadUtil.IAndroidThreadHandler() {
            @Override
            public boolean isOnUiThread() {
                return Thread.currentThread() == uiThread;
            }

            @Override
            public void postToUiThread(Runnable runnable) {
                synchronized (postedRunnables) {
                    postedRunnables.add(runnable);
                }
            }
        });

        String value = AndroidThreadUtil.getBlockingOnUiThread(10000, new Callable<String>() {
            @Override
            public String call() throws Exception {
                AndroidThreadUtil.verifyCalledFromUiThread();
                return "Already_on_ui_thread";
            }
        });
        Assert.assertEquals("Already_on_ui_thread", value);


        final ThreadHalter mainTreadHalter = new ThreadHalter();
        final ThreadHalter secondaryTreadHalter = new ThreadHalter();
        final Flag gotTimeout = new Flag();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mainTreadHalter.dependOnCurrentThread();
                try {
                    boolean gotException = false;
                    try {
                        AndroidThreadUtil.verifyCalledFromUiThread();
                    } catch (IllegalStateException e) {
                        gotException = true;
                    }
                    Assert.assertTrue("Expected exception from line before!", gotException);

                    try {
                        secondaryTreadHalter.waitForGo();
                        String value = AndroidThreadUtil.getBlockingOnUiThread(50, new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                AndroidThreadUtil.verifyCalledFromUiThread();
                                return "TestSuccess?";
                            }
                        });
                        Assert.fail("Expected to timeout!");
                    } catch (TimeoutException e) {
                        //Expected
                        gotTimeout.setFlag();
                        mainTreadHalter.proceed();

                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "TestThread").start();

        gotTimeout.assertNotSet();
        secondaryTreadHalter.proceed();
        mainTreadHalter.waitForGo();
        gotTimeout.assertSet();

        Assert.assertEquals(1, postedRunnables.size());
    }

    @Test
    public void testGetOnUiThreadWithTimeoutDoesNotBlockMainThread() throws InterruptedException {
        final Thread uiThread = Thread.currentThread();

        final ThreadHalter mainTreadHalter = new ThreadHalter();
        final ThreadHalter secondaryTreadHalter = new ThreadHalter();
        final List<Runnable> postedRunnables = new ArrayList<>();
        AndroidThreadUtil.setAndroidThreadHandler(new AndroidThreadUtil.IAndroidThreadHandler() {
            @Override
            public boolean isOnUiThread() {
                return Thread.currentThread() == uiThread;
            }

            @Override
            public void postToUiThread(Runnable runnable) {
                synchronized (postedRunnables) {
                    postedRunnables.add(runnable);
                }
                mainTreadHalter.proceed();
            }
        });

        final Counter timeoutExceptionCaught = new Counter();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    secondaryTreadHalter.waitForGo();
                    Assert.assertFalse(AndroidThreadUtil.isOnUiThread());
                    AndroidThreadUtil.getBlockingOnUiThread(50, new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            while(true) { //BLOCK THREAD!
                                if(Thread.currentThread().isInterrupted()) {
                                    throw new InterruptedException();
                                }
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Assert.fail("Did not expect calling thread to be interrupted");
                } catch (TimeoutException e) {
                    timeoutExceptionCaught.count();
                }
            }
        }, "TestThread").start();

        Interruptor interruptor = new Interruptor(Thread.currentThread(), 250);
        interruptor.start();
        secondaryTreadHalter.proceed();
        mainTreadHalter.waitForGo();
        synchronized (postedRunnables) {
            Assert.assertEquals(1, postedRunnables.size());
            timeoutExceptionCaught.assertNone();
            postedRunnables.get(0).run();
            timeoutExceptionCaught.assertOnce();
            interruptor.cancel();
        }
        Assert.assertFalse("Main thread had to be interrupted by unit test interruptor.",interruptor.didInterrupt());
    }
}
