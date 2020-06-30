package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.IBufferingAnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.MockAnalyticsHandler;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.entitlement.EntitlementStatus;
import com.redbeemedia.enigma.core.error.AssetBlockedError;
import com.redbeemedia.enigma.core.error.ConcurrentStreamsLimitReachedError;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.GeoBlockedError;
import com.redbeemedia.enigma.core.error.NotAvailableError;
import com.redbeemedia.enigma.core.error.NotEntitledError;
import com.redbeemedia.enigma.core.error.NotPublishedError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.IStopWatch;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.time.StopWatch;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.IStateMachine;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.video.IVideoTrack;
import com.redbeemedia.enigma.core.video.MockVideoTrack;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                return super.addListener(playerListener);
            }

            @Override
            public boolean removeListener(IEnigmaPlayerListener playerListener) {
                removeListenerCalled.count();
                return super.removeListener(playerListener);
            }
        };
        int expectedNewTaskCalled = 1;
        int expectedTaskStartCalled = 0;
        int expectedTaskCancelCalled = 0;
        int expectedAddListenerCalled = 0;
        newTaskCalled.assertCount(expectedNewTaskCalled);
        taskStartCalled.assertCount(expectedTaskStartCalled);
        taskCancelCalled.assertCount(expectedTaskCancelCalled);
        addListenerCalled.assertCount(expectedAddListenerCalled);
        removeListenerCalled.assertNone();

        playbackSession.onStart(mockEnigmaPlayer);
        expectedTaskStartCalled += 1;
        expectedAddListenerCalled += 2;
        newTaskCalled.assertCount(expectedNewTaskCalled);
        taskStartCalled.assertCount(expectedTaskStartCalled);
        taskCancelCalled.assertCount(expectedTaskCancelCalled);
        addListenerCalled.assertCount(expectedAddListenerCalled);
        removeListenerCalled.assertNone();

        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"));
        playbackSession.onStop(mockEnigmaPlayer);
        expectedNewTaskCalled += 1;
        expectedTaskStartCalled += 1;
        expectedTaskCancelCalled += 1;
        newTaskCalled.assertCount(expectedNewTaskCalled);
        taskStartCalled.assertCount(expectedTaskStartCalled);
        taskCancelCalled.assertCount(expectedTaskCancelCalled);
        addListenerCalled.assertCount(expectedAddListenerCalled);
        int expectedRemoveListenerCalled = expectedAddListenerCalled;
        removeListenerCalled.assertCount(expectedRemoveListenerCalled);
    }

    @Test
    public void testThreadJoins() throws InterruptedException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        TestTaskFactory taskFactory = new TestTaskFactory();
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setTaskFactory(taskFactory).setHttpHandler(mockHttpHandler));
        InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockInternalPlaybackSessionConstructorArgs().create());

        MockEnigmaPlayer mockPlayer = new MockEnigmaPlayer();
        playbackSession.onStart(mockPlayer);
        Thread.sleep(100);
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"));
        playbackSession.onStop(mockPlayer);

        IStopWatch stopWatch = new StopWatch(new ITimeProvider() {
            @Override
            public long getTime() {
                return System.currentTimeMillis();
            }

            @Override
            public boolean isReady(Duration maxBlocktime) {
                return true;
            }
        });
        stopWatch.start();

        taskFactory.joinAllThreads();

        Assert.assertTrue("Took longer than 5 seconds to join threads...",stopWatch.stop().inUnits(Duration.Unit.SECONDS) < 5f);
    }

    @Test
    public void testStreamInfoResults() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        StreamInfo nonLiveStreamInfo = new MockStreamInfo(MockStreamInfo.Args.vod());
        StreamInfo liveStreamInfo = new MockStreamInfo(MockStreamInfo.Args.liveStream());

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
        InternalPlaybackSession playbackSession = new InternalPlaybackSession(args.setStreamInfo(new MockStreamInfo(MockStreamInfo.Args.liveStream())).create());
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
        OpenContainer<IVideoTrack> selectedVideoTrack = new OpenContainer<>(null);
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, StreamInfo.createForNull(), selectedVideoTrack);

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
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, streamInfo, new OpenContainer<>(null));

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
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, streamInfo, new OpenContainer<>(null));

        analytics.onStateChanged(EnigmaPlayerState.LOADED, EnigmaPlayerState.PLAYING);
        Assert.assertEquals(1,analyticsEvents.size());
        JSONObject event = analyticsEvents.get(0);
        Assert.assertEquals("Playback.Started", event.getString("EventType"));
        Assert.assertEquals("VOD", event.getString("PlayMode"));
        Assert.assertFalse("Did not expect event to have property ReferenceTime",event.has("ReferenceTime"));
    }

    @Test
    public void testCommunicationsCut() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        MockInternalPlaybackSessionConstructorArgs args = new MockInternalPlaybackSessionConstructorArgs();
        InternalPlaybackSession internalPlaybackSession = new InternalPlaybackSession(args.create());

        final EnigmaPlayerCollector eventSource = new EnigmaPlayerCollector();
        MockEnigmaPlayer mockEnigmaPlayer = new MockEnigmaPlayer() {
            @Override
            public boolean addListener(IEnigmaPlayerListener playerListener) {
                return eventSource.addListener(playerListener);
            }

            @Override
            public boolean removeListener(IEnigmaPlayerListener playerListener) {
                return eventSource.removeListener(playerListener);
            }
        };
        final Counter onPlaybackErrorCalled = new Counter();
        internalPlaybackSession.getPlayerConnection().openConnection(new MockCommunicationsChannel() {
            @Override
            public void onPlaybackError(EnigmaError error, boolean endStream) {
                onPlaybackErrorCalled.count();
            }
        });

        IEnigmaPlayerConnection.ICommunicationsChannel playerConnectionFromPBS = ((IEnigmaPlayerConnection.ICommunicationsChannel) internalPlaybackSession.getPlayerConnection());

        int expectedCalls = 0;
        onPlaybackErrorCalled.assertCount(expectedCalls);
        internalPlaybackSession.onStart(mockEnigmaPlayer);
        playerConnectionFromPBS.onPlaybackError(null, false);
        expectedCalls++;
        onPlaybackErrorCalled.assertCount(expectedCalls);

        internalPlaybackSession.onStop(mockEnigmaPlayer);
        playerConnectionFromPBS.onPlaybackError(null, false);
        expectedCalls++;
        onPlaybackErrorCalled.assertCount(expectedCalls);

        internalPlaybackSession.getPlayerConnection().severConnection();
        playerConnectionFromPBS.onPlaybackError(null, false);
        onPlaybackErrorCalled.assertCount(expectedCalls);
        playerConnectionFromPBS.onPlaybackError(null, false);
        onPlaybackErrorCalled.assertCount(expectedCalls);
    }

    @Test
    public void testPlaybackErrorForEveryStatus() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        Map<EntitlementStatus, Class<? extends EnigmaError>> expectedErrorType = new HashMap<>();
        expectedErrorType.put(EntitlementStatus.FORBIDDEN, NotEntitledError.class);
        expectedErrorType.put(EntitlementStatus.NOT_AVAILABLE, NotAvailableError.class);
        expectedErrorType.put(EntitlementStatus.BLOCKED, AssetBlockedError.class);
        expectedErrorType.put(EntitlementStatus.GEO_BLOCKED, GeoBlockedError.class);
        expectedErrorType.put(EntitlementStatus.CONCURRENT_STREAMS_LIMIT_REACHED, ConcurrentStreamsLimitReachedError.class);
        expectedErrorType.put(EntitlementStatus.NOT_PUBLISHED, NotPublishedError.class);
        expectedErrorType.put(EntitlementStatus.NOT_ENTITLED, NotEntitledError.class);
        expectedErrorType.put(null, NotEntitledError.class);
        final Counter errorTypesChecked = new Counter();

        for(EntitlementStatus status : EntitlementStatus.values()) {
            if(status == EntitlementStatus.SUCCESS) {
                continue;
            }
            final String assertMessage = "for "+status.name();
            final Counter onPlaybackErrorCalled = new Counter();
            MockCommunicationsChannel comChannel = new MockCommunicationsChannel() {
                @Override
                public void onPlaybackError(EnigmaError error, boolean endStream) {
                    Assert.assertTrue(assertMessage, endStream);
                    Assert.assertNotNull(assertMessage, expectedErrorType.get(status));
                    Assert.assertNotNull(assertMessage, error);
                    Assert.assertEquals(assertMessage, expectedErrorType.get(status), error.getClass());
                    errorTypesChecked.count();
                    onPlaybackErrorCalled.count();
                }
            };
            onPlaybackErrorCalled.assertNone(assertMessage);
            InternalPlaybackSession.handleEntitlementStatus(comChannel, status);
            onPlaybackErrorCalled.assertOnce(assertMessage);
        }

        final Counter onPlaybackErrorCalledForNull = new Counter();
        MockCommunicationsChannel comChannel = new MockCommunicationsChannel() {
            @Override
            public void onPlaybackError(EnigmaError error, boolean endStream) {
                Assert.assertTrue(endStream);
                Assert.assertNotNull(expectedErrorType.get(null));
                Assert.assertNotNull(error);
                Assert.assertEquals(expectedErrorType.get(null), error.getClass());
                errorTypesChecked.count();
                onPlaybackErrorCalledForNull.count();
            }
        };
        onPlaybackErrorCalledForNull.assertNone();
        InternalPlaybackSession.handleEntitlementStatus(comChannel, null);
        onPlaybackErrorCalledForNull.assertOnce();

        //Assert all types listed in expectedErrorType-map checked
        errorTypesChecked.assertCount(expectedErrorType.size());
    }

    @Test
    public void testBitrateChangedAnalyticsEvent() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final StringBuilder eventLog = new StringBuilder();

        MockInternalPlaybackSessionConstructorArgs args = new MockInternalPlaybackSessionConstructorArgs();
        InternalPlaybackSession internalPlaybackSession = new InternalPlaybackSession(args.create()) {
            @Override
            protected IBufferingAnalyticsHandler newAnalyticsHandler(ISession session, String playbackSessionId, ITimeProvider timeProvider) {
                return new MockAnalyticsHandler() {
                    private final List<String> monitoredEvents = Arrays.asList("Playback.Created",
                                                                               "Playback.Started",
                                                                               "Playback.BitrateChanged");
                    @Override
                    public void onAnalytics(JSONObject jsonObject) {
                        String eventType = jsonObject.optString("EventType", null);
                        if(monitoredEvents.contains(eventType)) {
                            eventLog.append("["+eventType);
                            if(jsonObject.has("Bitrate")) {
                                eventLog.append(", "+jsonObject.opt("Bitrate"));
                            }
                            eventLog.append("]");
                        }
                    }
                };
            }
        };

        Assert.assertEquals(0, eventLog.length());

        List<IEnigmaPlayerListener> playerListeners = new ArrayList<>();
        IEnigmaPlayer enigmaPlayer = new MockEnigmaPlayer() {
            @Override
            public boolean addListener(IEnigmaPlayerListener playerListener) {
                return playerListeners.add(playerListener);
            }

            @Override
            public boolean removeListener(IEnigmaPlayerListener playerListener) {
                return playerListeners.remove(playerListener);
            }
        };
        internalPlaybackSession.onStart(enigmaPlayer);

        Assert.assertEquals("[Playback.Created]", eventLog.toString());

        internalPlaybackSession.setSelectedVideoTrack(new MockVideoTrack(72634));

        Assert.assertEquals("[Playback.Created]", eventLog.toString());

        for(IEnigmaPlayerListener playerListener : playerListeners) {
            playerListener.onStateChanged(EnigmaPlayerState.LOADED, EnigmaPlayerState.PLAYING);
        }

        Assert.assertEquals("[Playback.Created][Playback.Started, 72634]", eventLog.toString());

        internalPlaybackSession.setSelectedVideoTrack(new MockVideoTrack(99352));
        Assert.assertEquals("[Playback.Created][Playback.Started, 72634][Playback.BitrateChanged, 99352]", eventLog.toString());

        internalPlaybackSession.setSelectedVideoTrack(new MockVideoTrack(99352));
        Assert.assertEquals("[Playback.Created][Playback.Started, 72634][Playback.BitrateChanged, 99352]", eventLog.toString());

        internalPlaybackSession.setSelectedVideoTrack(new MockVideoTrack(123));
        Assert.assertEquals("[Playback.Created][Playback.Started, 72634][Playback.BitrateChanged, 99352][Playback.BitrateChanged, 123]", eventLog.toString());
    }

    private static class MockEnigmaPlayer implements IEnigmaPlayer {
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
        public boolean addListener(IEnigmaPlayerListener playerListener) {
            return false;
        }

        @Override
        public boolean removeListener(IEnigmaPlayerListener playerListener) {
            return false;
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
        private final Collection<JobInfo> jobs = Collections.synchronizedCollection(new ArrayList<>());

        private static class JobInfo {
            private final StackTraceElement[] creationStackTrace;
            private volatile boolean started = false;
            private volatile Thread jobThread;

            public JobInfo(StackTraceElement[] creationStackTrace) {
                this.creationStackTrace = creationStackTrace;
            }

            public void start() {
                if(started) {
                    throw new RuntimeException("Already started!");
                }
                started = true;
                jobThread = Thread.currentThread();
            }

            @Override
            public String toString() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Started: "+started+"\n");
                for(int i = 3; i < creationStackTrace.length; ++i) {
                    stringBuilder.append("Creation>> "+creationStackTrace[i]+"\n");
                }
                if(started && jobThread != null) {
                    stringBuilder.append("\tCurrent thread pos: \n");
                    for(StackTraceElement element : jobThread.getStackTrace()) {
                        stringBuilder.append("JobThread>> "+element+"\n");
                    }
                }
                return stringBuilder.toString();
            }
        }

        private Future submitToExecutorService(Runnable runnable) {
            final JobInfo jobInfo = new JobInfo(Thread.currentThread().getStackTrace());
            jobs.add(jobInfo);

            return executorService.submit(new Runnable() {
                @Override
                public void run() {
                    jobInfo.start();
                    try {
                        runnable.run();
                    } finally {
                        jobs.remove(jobInfo);
                    }
                }
            });
        }

        @Override
        public ITask newTask(final Runnable runnable) {
            return new ITask() {
                private Future future;

                @Override
                public void start() throws IllegalStateException {
                    onTaskStart(this);
                    this.future = submitToExecutorService(runnable);
                }

                @Override
                public void cancel(long joinMillis) throws IllegalStateException {
                    onTaskCancel(this);
                    if(future != null) {
                        future.cancel(true);
                    }
                }

                @Override
                public void startDelayed(long delayMillis) {
                    onTaskStartDelayed(this);
                    future = submitToExecutorService(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(delayMillis);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            future = submitToExecutorService(runnable);
                        }
                    });
                }
            };
        }

        protected void onTaskStart(ITask task) {
        }

        protected void onTaskStartDelayed(ITask task) {
        }

        protected void onTaskCancel(ITask task) {
        }

        public void joinAllThreads() {
            executorService.shutdown();
            try {
                if(!executorService.awaitTermination(10000, TimeUnit.MILLISECONDS)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Failed to join "+jobs.size()+" treads: \n");
                    synchronized (jobs) {
                        for(JobInfo jobInfo : jobs) {
                            stringBuilder.append(jobInfo.toString()+"\n");
                        }
                    }
                    Assert.fail(stringBuilder.toString());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
