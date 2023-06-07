package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.google.android.exoplayer2.ui.SubtitleView;
import com.redbeemedia.enigma.core.ads.AdDetector;
import com.redbeemedia.enigma.core.ads.ExposureAdMetadata;
import com.redbeemedia.enigma.core.ads.IAdDetector;
import com.redbeemedia.enigma.core.ads.IAdMetadata;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.MockAnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.MockAnalyticsReporter;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.marker.IMarkerPointsDetector;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.SimpleTimeline;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.MockTaskFactoryProvider;
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
import com.redbeemedia.enigma.core.virtualui.IVirtualControls;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InternalPlaybackSessionTest {
    @Test
    public void testLifecycle() {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();

        final Counter onStartCalled = new Counter();
        final Counter onStopCalled = new Counter();

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));


        InternalPlaybackSession playbackSession = new InternalPlaybackSession(new MockInternalPlaybackSessionConstructorArgs().create());
        playbackSession.addInternalListener(new MockInternalPlaybackSessionListener() {
            @Override
            public void onStart(OnStartArgs args) {
                onStartCalled.count();
            }

            @Override
            public void onStop(OnStopArgs args) {
                onStopCalled.count();
            }
        });
        onStartCalled.assertExpected();
        onStopCalled.assertExpected();

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
        int expectedAddListenerCalled = 0;
        addListenerCalled.assertCount(expectedAddListenerCalled);
        removeListenerCalled.assertNone();

        playbackSession.onStart(mockEnigmaPlayer);
        onStartCalled.setExpectedCounts(1);
        onStartCalled.assertExpected();
        onStopCalled.assertExpected();
        expectedAddListenerCalled += 1;
        addListenerCalled.assertCount(expectedAddListenerCalled);
        removeListenerCalled.assertNone();

        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"));
        playbackSession.onStop(mockEnigmaPlayer);
        onStopCalled.setExpectedCounts(1);
        onStopCalled.assertExpected();
        addListenerCalled.assertCount(expectedAddListenerCalled);
        int expectedRemoveListenerCalled = expectedAddListenerCalled;
        removeListenerCalled.assertCount(expectedRemoveListenerCalled);
    }

    @Test
    public void testLifecycleCallbackOrder() {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();

        final StringBuilder log = new StringBuilder();

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        InternalPlaybackSession internalPlaybackSession = new InternalPlaybackSession(new MockInternalPlaybackSessionConstructorArgs().create());
        class LoggingListener extends MockInternalPlaybackSessionListener {
            private final String name;

            public LoggingListener(String name) {
                this.name = name;
            }

            @Override
            public void onStart(OnStartArgs args) {
                log.append("[Start "+name+"]");
            }

            @Override
            public void onStop(OnStopArgs args) {
                log.append("[Stop "+name+"]");
            }
        }
        internalPlaybackSession.addInternalListener(new LoggingListener("1"));
        internalPlaybackSession.addInternalListener(new LoggingListener("2"));
        internalPlaybackSession.addInternalListener(new LoggingListener("3"));
        Assert.assertEquals("", log.toString());

        MockEnigmaPlayer mockEnigmaPlayer = new MockEnigmaPlayer();
        internalPlaybackSession.onStart(mockEnigmaPlayer);

        Assert.assertEquals("[Start 1][Start 2][Start 3]", log.toString());

        internalPlaybackSession.onStop(mockEnigmaPlayer);

        Assert.assertEquals("[Start 1][Start 2][Start 3][Stop 3][Stop 2][Stop 1]", log.toString());
    }

    @Test
    public void testThreadJoins() throws InterruptedException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        TestTaskFactory taskFactory = new TestTaskFactory();
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setTaskFactoryProvider(new MockTaskFactoryProvider().setTaskFactory(taskFactory)).setHttpHandler(mockHttpHandler));
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
        JsonStreamInfo nonLiveStreamInfo = new MockStreamInfo(MockStreamInfo.Args.vod());
        JsonStreamInfo liveStreamInfo = new MockStreamInfo(MockStreamInfo.Args.liveStream());

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
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(timeProvider, jsonObject -> analyticsEvents.add(jsonObject),0l);
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        OpenContainer<IVideoTrack> selectedVideoTrack = new OpenContainer<>(null);
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, JsonStreamInfo.createForNull(), selectedVideoTrack);
        analytics.setPlayerImplementationControls(new MockIPlayerImplementationControls());

        IStateMachine<EnigmaPlayerState> stateMachine = EnigmaStateMachineFactory.create();
        stateMachine.addListener((from, to) -> analytics.onStateChanged(from, to));

        int expectedEvents = 0;
        timeProvider.addTime(100);
        stateMachine.setState(EnigmaPlayerState.LOADING);
        Assert.assertEquals(expectedEvents,analyticsEvents.size());


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
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(timeProvider, jsonObject -> analyticsEvents.add(jsonObject),0l);
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        JsonStreamInfo streamInfo = new JsonStreamInfo(new JSONObject("{\"live\" : true, \"static\" : false, \"start\" : 8765432}"));
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, streamInfo, new OpenContainer<>(null));
        analytics.setPlayerImplementationControls(new MockIPlayerImplementationControls());

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
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(timeProvider, jsonObject -> analyticsEvents.add(jsonObject),0l);
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        JsonStreamInfo streamInfo = new JsonStreamInfo(new JSONObject("{\"live\" : false, \"start\" : 8765432}"));
        InternalPlaybackSession.EnigmaPlayerListenerForAnalytics analytics = new InternalPlaybackSession.EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, streamInfo, new OpenContainer<>(null));

        analytics.setPlayerImplementationControls(new MockIPlayerImplementationControls());
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
    public void testBitrateChangedAnalyticsEvent() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final StringBuilder eventLog = new StringBuilder();

        MockInternalPlaybackSessionConstructorArgs args = new MockInternalPlaybackSessionConstructorArgs();
        args.setAnalyticsReporter(new AnalyticsReporter(new MockTimeProvider(), new MockAnalyticsHandler() {
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
        },0l));
        InternalPlaybackSession internalPlaybackSession = new InternalPlaybackSession(args.create());

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
        internalPlaybackSession.setPlayerImplementationControls(new MockIPlayerImplementationControls());

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


    @Test
    public void testBasicAnalyticsEvents() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final StringBuilder eventLog = new StringBuilder();

        MockInternalPlaybackSessionConstructorArgs args = new MockInternalPlaybackSessionConstructorArgs();
        args.setPlaybackSessionInfo(new MockPlaybackSessionInfo() {
            @Override
            public String getAssetId() {
                return "testBasicAnalyticsEventsAsset";
            }
        });
        args.setAnalyticsReporter(new MockAnalyticsReporter() {

            @Override
            public void deviceInfo(String cdnProvider) {
                eventLog.append("[DeviceInfo]");
            }

            @Override
            public void playbackCreated(String assetId) {
                eventLog.append("[PlaybackCreated("+assetId+")]");
            }

            @Override
            public void playbackHandshakeStarted(String assetId) {
                eventLog.append("[PlaybackHandshake("+assetId+")]");
            }

            @Override
            public void playbackPlayerReady(long offsetTime, String playerImplementationTechnology, String playerImplementationTechnologyVersion) {
                eventLog.append("[PlayerReady]");
            }

            @Override
            public void playbackAborted(long offsetTime) {
                eventLog.append("[PlaybackAborted]");
            }
        });
        InternalPlaybackSession internalPlaybackSession = new InternalPlaybackSession(args.create());

        IStateMachine<EnigmaPlayerState> stateMachine = EnigmaStateMachineFactory.create();
        EnigmaPlayerCollector enigmaPlayerListeners = new EnigmaPlayerCollector();
        stateMachine.addListener((from, to) -> enigmaPlayerListeners.onStateChanged(from, to));
        MockEnigmaPlayer mockEnigmaPlayer = new MockEnigmaPlayer() {
            @Override
            public boolean addListener(IEnigmaPlayerListener playerListener) {
                enigmaPlayerListeners.addListener(playerListener);
                return true;
            }

            @Override
            public boolean addListener(IEnigmaPlayerListener playerListener, Handler handler) {
                return addListener(playerListener);
            }
        };
        stateMachine.setState(EnigmaPlayerState.IDLE);

        StringBuilder expectedEventLog = new StringBuilder();

        Assert.assertEquals(expectedEventLog.toString(), eventLog.toString());

        internalPlaybackSession.onStart(mockEnigmaPlayer);
        expectedEventLog.append("[DeviceInfo][PlaybackCreated(testBasicAnalyticsEventsAsset)][PlaybackHandshake(testBasicAnalyticsEventsAsset)]");

        Assert.assertEquals(expectedEventLog.toString(), eventLog.toString());

        stateMachine.setState(EnigmaPlayerState.LOADING);

        Assert.assertEquals(expectedEventLog.toString(), eventLog.toString());

        stateMachine.setState(EnigmaPlayerState.LOADED);
        expectedEventLog.append("[PlayerReady]");

        Assert.assertEquals(expectedEventLog.toString(), eventLog.toString());

        internalPlaybackSession.onStop(mockEnigmaPlayer);
        expectedEventLog.append("[PlaybackAborted]");

        Assert.assertEquals(expectedEventLog.toString(), eventLog.toString());
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
        public IAdDetector getAdDetector() {
            ITimeline timeline = new SimpleTimeline();
            MockHttpHandler mockHttpHandler = new MockHttpHandler();
            DefaultTimelinePositionFactoryTest.TimeLinePositionCreator timeFactory = new DefaultTimelinePositionFactoryTest.TimeLinePositionCreator();
            return new AdDetector(mockHttpHandler, timeline,timeFactory);
        }

        @Override
        public IMarkerPointsDetector getMarkerPointsDetector() {
            return null;
        }

        @Override
        public ITimeline getTimeline() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDefaultSession(ISession session) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLiveStream() {
            return false;
        }

        @Override
        public void release() {
        }

        @Override
        public boolean isAdBeingPlayed() {
            return false;
        }

        @Override
        public IVirtualControls getVirtualControls() {
            return null;
        }

        @Override
        public void setVirtualControls(IVirtualControls virtualControls) {

        }

        @Override
        public IAnalyticsReporter getCurrentAnalyticsReporter() {
            return null;
        }

        @Override
        public void setStickyPlayer(boolean isStickyPlayer) {

        }

        @Override
        public boolean isStickyPlayer() {
            return false;
        }

        @Override
        public boolean isCurrentStreamTypeAudioOnly() {
            return false;
        }

        @Override
        public SubtitleView getPlayerSubtitleView() {
            return null;
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
