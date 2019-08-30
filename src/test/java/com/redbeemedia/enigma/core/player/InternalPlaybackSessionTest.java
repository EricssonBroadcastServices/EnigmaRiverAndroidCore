package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.IStateMachine;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InternalPlaybackSessionTest {
    @Test
    public void testLifecycle() {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();

        final Counter newTaskCalled = new Counter();
        final Counter taskStartCalled = new Counter();
        final Counter taskCancelCalled = new Counter();

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler).setTaskFactory(new ITaskFactory() {
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


        InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockInternalPlaybackSessionConstructorArgs().create());
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

        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"));
        playbackSession.onStop(mockEnigmaPlayer);
        newTaskCalled.assertCount(2);
        taskStartCalled.assertCount(2);
        taskCancelCalled.assertOnce();
        addListenerCalled.assertOnce();
        removeListenerCalled.assertOnce();
    }

    @Test
    public void testThreadJoins() throws InterruptedException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        TestTaskFactory taskFactory = new TestTaskFactory();
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setTaskFactory(taskFactory).setHttpHandler(mockHttpHandler));
        InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockInternalPlaybackSessionConstructorArgs().create());

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
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"));
        playbackSession.onStop(mockPlayer);

        taskFactory.joinAllThreads();
    }

    @Test
    public void testStreamInfoResults() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        StreamInfo nonLiveStreamInfo = new MockStreamInfo(false);
        StreamInfo liveStreamInfo = new MockStreamInfo(true);

        {
            MockInternalPlaybackSessionConstructorArgs args = new MockInternalPlaybackSessionConstructorArgs();
            InternalPlaybackSession playbackSession = new InternalPlaybackSession(args.setStreamInfo(nonLiveStreamInfo).create());
            Assert.assertFalse(playbackSession.isPlayingFromLive());
            Assert.assertTrue(playbackSession.isSeekAllowed());
            Assert.assertFalse(playbackSession.isSeekToLiveAllowed());

            playbackSession.setPlayingFromLive(true);
            Assert.assertFalse("Did not expect playingFromLive to be changed for a non-live stream", playbackSession.isPlayingFromLive());
            playbackSession.setPlayingFromLive(false);
            Assert.assertFalse(playbackSession.isPlayingFromLive());
        }
        {
            MockInternalPlaybackSessionConstructorArgs args = new MockInternalPlaybackSessionConstructorArgs();
            InternalPlaybackSession playbackSession = new InternalPlaybackSession(args.setStreamInfo(liveStreamInfo).create());
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
        MockInternalPlaybackSessionConstructorArgs args = new MockInternalPlaybackSessionConstructorArgs();
        InternalPlaybackSession playbackSession = new InternalPlaybackSession(args.setStreamInfo(new MockStreamInfo(true)).create());
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

    @Test
    public void testAnalyticsEvents() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        MockTimeProvider timeProvider = new MockTimeProvider();
        final List<JSONObject> analyticsEvents = new ArrayList<>();
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(timeProvider, jsonObject -> analyticsEvents.add(jsonObject));
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, StreamInfo.createForNull());

        IStateMachine<EnigmaPlayerState> stateMachine = EnigmaStateMachineFactory.create();
        stateMachine.addListener((from, to) -> analytics.onStateChanged(from, to));

        int expectedEvents = 0;
        timeProvider.addTime(100);
        stateMachine.setState(EnigmaPlayerState.LOADING);
        expectedEvents++;
        Assert.assertEquals(expectedEvents,analyticsEvents.size());
        Assert.assertEquals("Playback.HandshakeStarted",analyticsEvents.get(expectedEvents-1).getString("EventType"));

        timeProvider.addTime(200);
        stateMachine.setState(EnigmaPlayerState.LOADED);
        expectedEvents++;
        Assert.assertEquals(expectedEvents,analyticsEvents.size());
        Assert.assertEquals("Playback.PlayerReady",analyticsEvents.get(expectedEvents-1).getString("EventType"));

        timeProvider.addTime(50);
        stateMachine.setState(EnigmaPlayerState.PLAYING);
        expectedEvents++;
        Assert.assertEquals(expectedEvents,analyticsEvents.size());
        Assert.assertEquals("Playback.Started",analyticsEvents.get(expectedEvents-1).getString("EventType"));

        timeProvider.addTime(150);
        stateMachine.setState(EnigmaPlayerState.PAUSED);
        expectedEvents++;
        Assert.assertEquals(expectedEvents,analyticsEvents.size());
        Assert.assertEquals("Playback.Paused",analyticsEvents.get(expectedEvents-1).getString("EventType"));

        timeProvider.addTime(550);
        stateMachine.setState(EnigmaPlayerState.PLAYING);
        expectedEvents++;
        Assert.assertEquals(expectedEvents,analyticsEvents.size());
        Assert.assertEquals("Playback.Resumed",analyticsEvents.get(expectedEvents-1).getString("EventType"));

        timeProvider.addTime(150);
        stateMachine.setState(EnigmaPlayerState.PAUSED);
        expectedEvents++;
        Assert.assertEquals(expectedEvents,analyticsEvents.size());
        Assert.assertEquals("Playback.Paused",analyticsEvents.get(expectedEvents-1).getString("EventType"));

        timeProvider.addTime(123);
        stateMachine.setState(EnigmaPlayerState.PLAYING);
        expectedEvents++;
        Assert.assertEquals(expectedEvents,analyticsEvents.size());
        Assert.assertEquals("Playback.Resumed",analyticsEvents.get(expectedEvents-1).getString("EventType"));
    }

    @Test
    public void testReferenceTimePresentForLiveStreams() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        MockTimeProvider timeProvider = new MockTimeProvider();
        final List<JSONObject> analyticsEvents = new ArrayList<>();
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(timeProvider, jsonObject -> analyticsEvents.add(jsonObject));
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        StreamInfo streamInfo = new StreamInfo(new JSONObject("{\"live\" : true, \"static\" : false, \"start\" : 8765432}"));
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, streamInfo);

        analytics.onStateChanged(EnigmaPlayerState.LOADED, EnigmaPlayerState.PLAYING);
        Assert.assertEquals(1,analyticsEvents.size());
        JSONObject event = analyticsEvents.get(0);
        Assert.assertEquals("Playback.Started", event.getString("EventType"));
        Assert.assertEquals("LIVE", event.getString("PlayMode"));
        Assert.assertEquals(8765432L*1000L, event.getLong("ReferenceTime"));
    }

    @Test
    public void testReferenceTimeNotPresentForVodStreams() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        MockTimeProvider timeProvider = new MockTimeProvider();
        final List<JSONObject> analyticsEvents = new ArrayList<>();
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(timeProvider, jsonObject -> analyticsEvents.add(jsonObject));
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        StreamInfo streamInfo = new StreamInfo(new JSONObject("{\"live\" : false, \"start\" : 8765432}"));
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, streamInfo);

        analytics.onStateChanged(EnigmaPlayerState.LOADED, EnigmaPlayerState.PLAYING);
        Assert.assertEquals(1,analyticsEvents.size());
        JSONObject event = analyticsEvents.get(0);
        Assert.assertEquals("Playback.Started", event.getString("EventType"));
        Assert.assertEquals("VOD", event.getString("PlayMode"));
        Assert.assertFalse("Did not expect event to have property ReferenceTime",event.has("ReferenceTime"));
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
