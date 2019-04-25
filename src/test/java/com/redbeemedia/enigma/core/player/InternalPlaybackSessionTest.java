package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.IHandler;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InternalPlaybackSessionTest {
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
                    public void start() {
                        taskStartCalled.count();
                    }

                    @Override
                    public void startDelayed(long delayMillis) throws TaskException {
                    }

                    @Override
                    public void cancel(long joinMillis) {
                        taskCancelCalled.count();
                    }
                };
            }
        }));


        InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockSession(), "mockPbSId", new MockTimeProvider(), null);
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
        InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockSession(), "junit", new MockTimeProvider(), null);

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

    @Test
    public void testStreamInfoResults() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        StreamInfo nonLiveStreamInfo = new MockStreamInfo(false);
        StreamInfo liveStreamInfo = new MockStreamInfo(true);

        {
            StreamInfo streamInfo = nonLiveStreamInfo;
            InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockSession(), "junit", new MockTimeProvider(), streamInfo);
            Assert.assertFalse(playbackSession.isPlayingFromLive());
            Assert.assertTrue(playbackSession.isSeekAllowed());
            Assert.assertFalse(playbackSession.isSeekToLiveAllowed());

            playbackSession.setPlayingFromLive(true);
            Assert.assertFalse("Did not expect playingFromLive to be changed for a non-live stream", playbackSession.isPlayingFromLive());
            playbackSession.setPlayingFromLive(false);
            Assert.assertFalse(playbackSession.isPlayingFromLive());
        }
        {
            StreamInfo streamInfo = liveStreamInfo;
            InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockSession(), "junit", new MockTimeProvider(), streamInfo);
            Assert.assertFalse(playbackSession.isPlayingFromLive());
            Assert.assertTrue(playbackSession.isSeekAllowed());
            Assert.assertTrue(playbackSession.isSeekToLiveAllowed());

            playbackSession.setPlayingFromLive(true);
            Assert.assertTrue("Expected playingFromLive to be changed for a live stream", playbackSession.isPlayingFromLive());
            playbackSession.setPlayingFromLive(false);
            Assert.assertFalse("Expected playingFromLive to be changed for a live stream", playbackSession.isPlayingFromLive());
        }
    }

    @Test
    public void testListeners() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockSession(), "junit", new MockTimeProvider(), new MockStreamInfo(true));
        final StringBuilder log = new StringBuilder();
        IPlaybackSessionListener listener = new BasePlaybackSessionListener() {
            @Override
            public void onPlayingFromLiveChanged(boolean live) {
                log.append("["+live+"]");
            }
        };
        playbackSession.addListener(listener);
        final Counter onLiveChangedCalls = new Counter();
        IPlaybackSessionListener countListener = new BasePlaybackSessionListener() {
            @Override
            public void onPlayingFromLiveChanged(boolean live) {
                onLiveChangedCalls.count();
            }
        };
        playbackSession.addListener(countListener);
        Assert.assertEquals(0, log.toString().length());
        Assert.assertFalse(playbackSession.isPlayingFromLive());
        playbackSession.setPlayingFromLive(true);
        onLiveChangedCalls.assertCount(1);
        Assert.assertEquals("[true]", log.toString());
        playbackSession.setPlayingFromLive(true);
        Assert.assertEquals("[true]", log.toString());
        playbackSession.setPlayingFromLive(false);
        onLiveChangedCalls.assertCount(2);
        Assert.assertEquals("[true][false]", log.toString());
        playbackSession.setPlayingFromLive(false);
        playbackSession.setPlayingFromLive(false);
        Assert.assertEquals("[true][false]", log.toString());
        playbackSession.setPlayingFromLive(true);
        onLiveChangedCalls.assertCount(3);
        Assert.assertEquals("[true][false][true]", log.toString());

        playbackSession.removeListener(listener);
        playbackSession.setPlayingFromLive(true);
        onLiveChangedCalls.assertCount(3);
        playbackSession.setPlayingFromLive(false);
        onLiveChangedCalls.assertCount(4);
        Assert.assertEquals("[true][false][true]", log.toString());

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

        @Override
        public boolean addListener(IEnigmaPlayerListener playerListener, Handler handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IEnigmaPlayerControls getControls() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EnigmaPlayerState getState() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ITimeline getTimeline() {
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

                @Override
                public void startDelayed(long delayMillis) {
                    future = executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(delayMillis);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            future = executorService.submit(runnable);
                        }
                    });
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

    private static class MockStreamInfo extends StreamInfo {
        private final boolean liveStream;

        public MockStreamInfo(boolean liveStream) throws JSONException {
            super(null);
            this.liveStream = liveStream;
        }

        @Override
        public boolean isLiveStream() {
            return liveStream;
        }
    }
}
