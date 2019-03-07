package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.IHandler;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PlaybackSessionTest {
    @Test
    public void testLifecycle() {
        final Counter newTaskCalled = new Counter();
        final Counter taskStartCalled = new Counter();
        final Counter taskCancelCalled = new Counter();

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setTaskFactory(new ITaskFactory() {
            @Override
            public ITask newTask(Runnable runnable) {
                newTaskCalled.count();
                return new ITask() {
                    @Override
                    public void start() throws IllegalStateException {
                        taskStartCalled.count();
                    }

                    @Override
                    public void cancel(long joinMillis) throws IllegalStateException {
                        taskCancelCalled.count();
                    }
                };
            }
        }));


        PlaybackSession playbackSession = new PlaybackSession(new MockSession(), "mockPbSId", new MockTimeProvider());
        newTaskCalled.assertOnce();

        final Counter addListenerCalled = new Counter();
        final Counter removeListenerCalled = new Counter();
        MockEnigmaPlayer mockEnigmaPlayer = new MockEnigmaPlayer() {
            @Override
            public boolean addListener(IEnigmaPlayerListener playerListener) {
                addListenerCalled.count();
                return true;
            }

            @Override
            public boolean removeListener(IEnigmaPlayerListener playerListener) {
                removeListenerCalled.count();
                return true;
            }
        };
        newTaskCalled.assertOnce();
        taskStartCalled.assertNone();
        taskCancelCalled.assertNone();
        addListenerCalled.assertNone();
        removeListenerCalled.assertNone();

        playbackSession.onStart(mockEnigmaPlayer);
        newTaskCalled.assertOnce();
        taskStartCalled.assertOnce();
        taskCancelCalled.assertNone();
        addListenerCalled.assertOnce();
        removeListenerCalled.assertNone();

        playbackSession.onStop(mockEnigmaPlayer);
        newTaskCalled.assertOnce();
        taskStartCalled.assertOnce();
        taskCancelCalled.assertOnce();
        addListenerCalled.assertOnce();
        removeListenerCalled.assertOnce();
    }

    @Test
    public void testThreadJoins() throws InterruptedException {
        TestTaskFactory taskFactory = new TestTaskFactory();
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setTaskFactory(taskFactory));
        PlaybackSession playbackSession = new PlaybackSession(new MockSession(), "junit", new MockTimeProvider());

        MockEnigmaPlayer mockPlayer = new MockEnigmaPlayer() {
            @Override
            public boolean addListener(IEnigmaPlayerListener playerListener) {
                return true;
            }

            @Override
            public boolean removeListener(IEnigmaPlayerListener playerListener) {
                return true;
            }
        };
        playbackSession.onStart(mockPlayer);
        Thread.sleep(100);
        playbackSession.onStop(mockPlayer);

        taskFactory.joinAllThreads();
    }

    private abstract static class MockEnigmaPlayer implements IEnigmaPlayer {
        @Override
        public void play(IPlayRequest playerRequest) {
            throw new UnsupportedOperationException();
        }


        @Override
        public IEnigmaPlayer setCallbackHandler(IHandler handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IEnigmaPlayer setCallbackHandler(Handler handler) {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestTaskFactory implements ITaskFactory {
        private ExecutorService executorService = Executors.newCachedThreadPool();

        @Override
        public ITask newTask(final Runnable runnable) {
            return new ITask() {
                private Future future;

                @Override
                public void start() throws IllegalStateException {
                    this.future = executorService.submit(runnable);
                }

                @Override
                public void cancel(long joinMillis) throws IllegalStateException {
                    future.cancel(true);
                }
            };
        }

        public void joinAllThreads() {
            executorService.shutdown();
            try {
                Assert.assertTrue("Failed to join threads." ,executorService.awaitTermination(10000, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
