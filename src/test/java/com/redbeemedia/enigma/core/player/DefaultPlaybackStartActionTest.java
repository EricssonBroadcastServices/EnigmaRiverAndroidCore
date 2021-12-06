package com.redbeemedia.enigma.core.player;

import android.os.Parcel;

import com.redbeemedia.enigma.core.analytics.AnalyticsException;
import com.redbeemedia.enigma.core.analytics.AnalyticsPlayResponseData;
import com.redbeemedia.enigma.core.analytics.IBufferingAnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.MockAnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.MockAnalyticsReporter;
import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.entitlement.IEntitlementProvider;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playable.MockPlayable;
import com.redbeemedia.enigma.core.playable.UrlPlayable;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.playrequest.BasePlayResultHandler;
import com.redbeemedia.enigma.core.ads.DefaultAdInsertionParameters;
import com.redbeemedia.enigma.core.ads.IAdInsertionFactory;
import com.redbeemedia.enigma.core.ads.IAdInsertionParameters;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.MockPlayRequest;
import com.redbeemedia.enigma.core.playrequest.MockPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.PlayRequest;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.session.Session;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.MockTaskFactoryProvider;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.task.TestTaskFactory;
import com.redbeemedia.enigma.core.task.TestTaskFactoryProvider;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.ReflectionUtil;
import com.redbeemedia.enigma.core.testutil.json.JsonObjectBuilder;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.video.ISpriteRepository;
import com.redbeemedia.enigma.core.video.SpriteDataMock;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DefaultPlaybackStartActionTest {

    @Test
    public void testAnalyticsAdded() {
        TestTaskFactory testTaskFactory = new TestTaskFactory(100);
        MockHttpHandler httpHandler = new MockHttpHandler();
        {
            JsonObjectBuilder playResponse = new JsonObjectBuilder();
            playResponse.putArray("formats").addObject()
                    .put("format", "DASH")
                    .put("mediaLocator", "http://example.com/manifest.mpd");
            httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), playResponse.toString());
        }
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization()
                .setHttpHandler(httpHandler)
                .setTaskFactoryProvider(new MockTaskFactoryProvider()
                        .setTaskFactory(testTaskFactory)
                        .setMainThreadTaskFactory(testTaskFactory)
                )
        );

        MockPlayRequest playRequest = new MockPlayRequest();
        playRequest.setPlayable(new MockPlayable("MockyMockMock"));

        final Counter deliverPlaybackSessionCalled = new Counter();
        final Counter analyticsOnStartCalled = new Counter();
        final Counter analyticsOnStopCalled = new Counter();

        DefaultPlaybackStartAction playbackStartAction = new DefaultPlaybackStartAction(
                new MockSession(),
                new MockSession().getBusinessUnit(),
                new MockTimeProvider(),
                playRequest,
                null,
                EnigmaRiverContext.getTaskFactoryProvider(),
                new MockPlayerImplementation(),
                new MockPlaybackStartAction.MockEnigmaPlayerCallbacks() {
                    @Override
                    public void deliverPlaybackSession(IInternalPlaybackSession internalPlaybackSession) {
                        deliverPlaybackSessionCalled.count();
                        MockEnigmaPlayer mockEnigmaPlayer = new MockEnigmaPlayer();
                        internalPlaybackSession.onStart(mockEnigmaPlayer);
                        try {
                            EnigmaRiverContext.getTaskFactoryProvider().getTaskFactory().newTask(new Runnable() {
                                @Override
                                public void run() {
                                    internalPlaybackSession.onStop(mockEnigmaPlayer);
                                }
                            }).startDelayed(1000);
                        } catch (TaskException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new SpriteDataMock.MockSpriteRepository(),
                new HashSet<>()) {
            @Override
            protected Analytics createAnalytics(ISession session, String playbackSessionId, ITimeProvider timeProvider, ITaskFactory taskFactory, AnalyticsPlayResponseData analyticsPlayResponseData) {
                return new Analytics(new MockAnalyticsReporter(), new IInternalPlaybackSessionListener() {
                    @Override
                    public void onStart(OnStartArgs args) {
                        analyticsOnStartCalled.count();
                    }

                    @Override
                    public void onStop(OnStopArgs args) {
                        analyticsOnStopCalled.count();
                    }
                });
            }
        };

        deliverPlaybackSessionCalled.assertNone();
        analyticsOnStartCalled.assertNone();
        analyticsOnStopCalled.assertNone();

        playbackStartAction.start();
        testTaskFactory.letTimePass(100);

        deliverPlaybackSessionCalled.assertOnce();
        analyticsOnStartCalled.assertOnce();
        analyticsOnStopCalled.assertNone();

        testTaskFactory.letTimePass(5000);

        deliverPlaybackSessionCalled.assertOnce();
        analyticsOnStartCalled.assertOnce();
        analyticsOnStopCalled.assertOnce();
    }

    @Test
    public void testSendDataCalledAtPlaybackSessionStop() {
        TestTaskFactory testTaskFactory = new TestTaskFactory(100);
        MockHttpHandler httpHandler = new MockHttpHandler();
        {
            JsonObjectBuilder playResponse = new JsonObjectBuilder();
            playResponse.putArray("formats").addObject()
                    .put("format", "DASH")
                    .put("mediaLocator", "http://example.com/manifest.mpd");
            httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), playResponse.toString());
        }
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization()
                .setHttpHandler(httpHandler)
                .setTaskFactoryProvider(new MockTaskFactoryProvider()
                        .setTaskFactory(testTaskFactory)
                        .setMainThreadTaskFactory(testTaskFactory)
                )
        );

        final Counter deliverPlaybackSessionCalled = new Counter();
        final Counter sendDataCalled = new Counter();

        MockPlayRequest playRequest = new MockPlayRequest();
        playRequest.setPlayable(new MockPlayable("MockyMockMock"));

        final MockEnigmaPlayer mockEnigmaPlayer = new MockEnigmaPlayer();
        final IInternalPlaybackSession[] currentPlaybackSession = new IInternalPlaybackSession[]{null};

        DefaultPlaybackStartAction playbackStartAction = new DefaultPlaybackStartAction(
                new MockSession(),
                new MockSession().getBusinessUnit(),
                new MockTimeProvider(),
                playRequest,
                null,
                EnigmaRiverContext.getTaskFactoryProvider(),
                new MockPlayerImplementation(),
                new MockPlaybackStartAction.MockEnigmaPlayerCallbacks() {
                    @Override
                    public void deliverPlaybackSession(IInternalPlaybackSession internalPlaybackSession) {
                        deliverPlaybackSessionCalled.count();
                        currentPlaybackSession[0] = internalPlaybackSession;
                        internalPlaybackSession.onStart(mockEnigmaPlayer);
                    }
                },
                new SpriteDataMock.MockSpriteRepository(),
                new HashSet<>()) {
            @Override
            protected IBufferingAnalyticsHandler newAnalyticsHandler(ISession session, String playbackSessionId, ITimeProvider timeProvider, AnalyticsPlayResponseData analyticsPlayResponseData) {
                return new MockAnalyticsHandler() {
                    @Override
                    public void sendData() throws AnalyticsException, InterruptedException {
                        sendDataCalled.count();
                    }
                };
            }

            @Override
            protected Runnable newAnalyticsHandlerRunnable(IBufferingAnalyticsHandler analyticsHandler, long sleepTime) {
                return () -> {};
            }
        };

        testTaskFactory.letTimePass(10000);
        sendDataCalled.assertNone();
        deliverPlaybackSessionCalled.assertNone();

        playbackStartAction.start();
        testTaskFactory.letTimePass(100);

        Assert.assertNotNull(currentPlaybackSession[0]);
        deliverPlaybackSessionCalled.assertOnce();
        sendDataCalled.assertExpected();

        testTaskFactory.letTimePass(3000);

        deliverPlaybackSessionCalled.assertOnce();
        sendDataCalled.assertExpected();
        Assert.assertNotNull(currentPlaybackSession[0]);

        currentPlaybackSession[0].onStop(mockEnigmaPlayer);
        sendDataCalled.addToExpected(1);
        testTaskFactory.letTimePass(1000);

        sendDataCalled.assertExpected();
    }

    @Test
    public void testComponentsCreatedForAssetPlayable() {
        TestTaskFactory testTaskFactory = new TestTaskFactory(100);
        MockHttpHandler httpHandler = new MockHttpHandler();
        {
            JsonObjectBuilder playResponse = new JsonObjectBuilder();
            playResponse.putArray("formats").addObject()
                    .put("format", "DASH")
                    .put("mediaLocator", "http://example.com/manifest.mpd");
            httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), playResponse.toString());
        }
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization()
                .setHttpHandler(httpHandler)
                .setTaskFactoryProvider(new MockTaskFactoryProvider()
                        .setTaskFactory(testTaskFactory)
                        .setMainThreadTaskFactory(testTaskFactory)
                )
        );


        MockPlayRequest playRequest = new MockPlayRequest();
        playRequest.setPlayable(new MockPlayable("MockyMockMock"));

        final Counter createAnalyticsCalled = new Counter();
        final Counter createProgramServiceCalled = new Counter();

        final List<IInternalPlaybackSessionListener> addedListeners = new ArrayList<>();
        final IInternalPlaybackSessionListener[] analyticsListener = new IInternalPlaybackSessionListener[]{null};
        final IInternalPlaybackSessionListener[] programService = new IInternalPlaybackSessionListener[]{null};

        DefaultPlaybackStartAction playbackStartAction = new DefaultPlaybackStartAction(
                new MockSession(),
                new MockSession().getBusinessUnit(),
                new MockTimeProvider(),
                playRequest,
                null,
                EnigmaRiverContext.getTaskFactoryProvider(),
                new MockPlayerImplementation(),
                new MockPlaybackStartAction.MockEnigmaPlayerCallbacks(),
                new SpriteDataMock.MockSpriteRepository(),
                new HashSet<>()) {
            @Override
            protected IInternalPlaybackSession newPlaybackSession(InternalPlaybackSession.ConstructorArgs constructorArgs) {
                MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(true) {
                    @Override
                    public void addInternalListener(IInternalPlaybackSessionListener listener) {
                        addedListeners.add(listener);
                        super.addInternalListener(listener);
                    }
                };
                return playbackSession;
            }

            @Override
            protected Analytics createAnalytics(ISession session, String playbackSessionId, ITimeProvider timeProvider, ITaskFactory taskFactory, AnalyticsPlayResponseData analyticsPlayResponseData) {
                createAnalyticsCalled.count();
                Analytics analytics = super.createAnalytics(session, playbackSessionId, timeProvider, taskFactory, analyticsPlayResponseData);
                analyticsListener[0] =  analytics.getInternalPlaybackSessionListener();
                return analytics;
            }

            @Override
            protected IInternalPlaybackSessionListener createProgramService(ISession session, IStreamInfo streamInfo, IStreamPrograms streamPrograms, IPlaybackSessionInfo playbackSessionInfo, IEntitlementProvider entitlementProvider, IPlaybackSession playbackSession, ITaskFactoryProvider taskFactoryProvider) {
                createProgramServiceCalled.count();
                programService[0] = super.createProgramService(session, streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession, taskFactoryProvider);
                return programService[0];
            }
        };

        testTaskFactory.letTimePass(10000);

        createProgramServiceCalled.assertNone();
        createAnalyticsCalled.assertNone();
        Assert.assertEquals(0, addedListeners.size());

        playbackStartAction.start();
        testTaskFactory.letTimePass(100);

        createAnalyticsCalled.assertOnce();
        createProgramServiceCalled.assertOnce();
        Assert.assertNotNull(analyticsListener[0]);
        Assert.assertNotNull(programService[0]);
        Assert.assertEquals(2, addedListeners.size());
        Assert.assertTrue(addedListeners.contains(analyticsListener[0]));
        Assert.assertTrue(addedListeners.contains(programService[0]));
    }

    @Test
    public void testOnStartedCalledOnceForAssetPlayable() {
        testOnStartCalledOnce(new MockPlayable("MockAsset"));
    }

    @Test
    public void testOnStartedCalledOnceForUrlPlayable() throws MalformedURLException {
        testOnStartCalledOnce(new UrlPlayable("http://example.com/external.mpd"));
    }

    @Test
    public void testOnStartedCalledOnceForDownloadPlayable() {
        testOnStartCalledOnce(new IPlayable() {
            @Override
            public void useWith(IPlayableHandler playableHandler) {
                playableHandler.startUsingDownloadData(new Object());
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
            }
        });
    }

    private static void testOnStartCalledOnce(IPlayable playable) {
        TestTaskFactoryProvider testTaskFactoryProvider = new TestTaskFactoryProvider(100);
        MockHttpHandler httpHandler = new MockHttpHandler();
        {
            JsonObjectBuilder playResponse = new JsonObjectBuilder();
            playResponse.putArray("formats").addObject()
                    .put("format", "DASH")
                    .put("mediaLocator", "http://example.com/manifest.mpd");
            httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), playResponse.toString());
        }
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization()
                .setHttpHandler(httpHandler)
                .setTaskFactoryProvider(testTaskFactoryProvider)
        );

        final Counter onStartedCalled = new Counter();

        IPlayResultHandler resultHandler = new BasePlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                error.printStackTrace();
                Assert.fail(error.toString());
            }

            @Override
            public void onStarted(IPlaybackSession playbackSession) {
                onStartedCalled.count();
                super.onStarted(playbackSession);
            }
        };
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerTest.EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation()) {
            @Override
            protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler, ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection, ISpriteRepository spriteRepository) {
                return new DefaultPlaybackStartAction(
                        session,
                        businessUnit,
                        timeProvider,
                        playRequest,
                        callbackHandler,
                        taskFactoryProvider,
                        playerImplementationControls,
                        playerConnection,
                        spriteRepository,
                        new HashSet<>()) {
                    @Override
                    protected Analytics createAnalytics(ISession session, String playbackSessionId, ITimeProvider timeProvider, ITaskFactory taskFactory, AnalyticsPlayResponseData analyticsPlayResponseData) {
                        return new Analytics(new IgnoringAnalyticsReporter(), new MockInternalPlaybackSessionListener());
                    }

                    @Override
                    protected IInternalPlaybackSessionListener createProgramService(ISession session, IStreamInfo streamInfo, IStreamPrograms streamPrograms, IPlaybackSessionInfo playbackSessionInfo, IEntitlementProvider entitlementProvider, IPlaybackSession playbackSession, ITaskFactoryProvider taskFactoryProvider) {
                        return new MockInternalPlaybackSessionListener();
                    }
                };
            }
        };

        onStartedCalled.assertNone();

        enigmaPlayer.play(new MockPlayRequest().setPlayable(playable).setResultHandler(resultHandler));
        testTaskFactoryProvider.letTimePass(1000);

        onStartedCalled.assertOnce();
    }

    @Test
    public void testCorrectSessionSelected() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        ISession sessionFromEnigmaPlayer = new Session("mockID_1", new BusinessUnit("CU", "BU"),"123");
        ISession playRequestSession = new Session("PlayMock", new BusinessUnit("PlayRequest", "Biz"),"123");

        DefaultPlaybackStartAction playbackStartAction = new DefaultPlaybackStartAction(
                sessionFromEnigmaPlayer,
                sessionFromEnigmaPlayer.getBusinessUnit(),
                new MockTimeProvider(),
                new PlayRequest(new MockPlayable("MockAsset"), new MockPlayResultHandler()),
                null,
                new MockTaskFactoryProvider(),
                new MockPlayerImplementation(),
                new MockPlaybackStartAction.MockEnigmaPlayerCallbacks(),
                new SpriteDataMock.MockSpriteRepository(), new HashSet<>());

        ISession sessionValue = ReflectionUtil.getDeclaredField(playbackStartAction, ISession.class, "session");
        IBusinessUnit businessUnitValue = ReflectionUtil.getDeclaredField(playbackStartAction, IBusinessUnit.class, "businessUnit");

        Assert.assertSame(sessionFromEnigmaPlayer, sessionValue);
        Assert.assertSame(sessionFromEnigmaPlayer.getBusinessUnit(), businessUnitValue);

        playbackStartAction = new DefaultPlaybackStartAction(
                null,
                sessionFromEnigmaPlayer.getBusinessUnit(),
                new MockTimeProvider(),
                new PlayRequest(new MockPlayable("MockAsset"), new MockPlayResultHandler()),
                null,
                new MockTaskFactoryProvider(),
                new MockPlayerImplementation(),
                new MockPlaybackStartAction.MockEnigmaPlayerCallbacks(),
                new SpriteDataMock.MockSpriteRepository(), new HashSet<>());

        sessionValue = ReflectionUtil.getDeclaredField(playbackStartAction, ISession.class, "session");
        businessUnitValue = ReflectionUtil.getDeclaredField(playbackStartAction, IBusinessUnit.class, "businessUnit");

        Assert.assertNull(sessionValue);
        Assert.assertSame(sessionFromEnigmaPlayer.getBusinessUnit(), businessUnitValue);


        playbackStartAction = new DefaultPlaybackStartAction(
                null,
                sessionFromEnigmaPlayer.getBusinessUnit(),
                new MockTimeProvider(),
                new PlayRequest(playRequestSession, new MockPlayable("MockAsset"), new MockPlayResultHandler()),
                null,
                new MockTaskFactoryProvider(),
                new MockPlayerImplementation(),
                new MockPlaybackStartAction.MockEnigmaPlayerCallbacks(),
                new SpriteDataMock.MockSpriteRepository(), new HashSet<>());

        sessionValue = ReflectionUtil.getDeclaredField(playbackStartAction, ISession.class, "session");
        businessUnitValue = ReflectionUtil.getDeclaredField(playbackStartAction, IBusinessUnit.class, "businessUnit");

        Assert.assertSame(playRequestSession, sessionValue);
        Assert.assertSame(playRequestSession.getBusinessUnit(), businessUnitValue);

        playbackStartAction = new DefaultPlaybackStartAction(
                sessionFromEnigmaPlayer,
                sessionFromEnigmaPlayer.getBusinessUnit(),
                new MockTimeProvider(),
                new PlayRequest(playRequestSession, new MockPlayable("MockAsset"), new MockPlayResultHandler()),
                null,
                new MockTaskFactoryProvider(),
                new MockPlayerImplementation(),
                new MockPlaybackStartAction.MockEnigmaPlayerCallbacks(),
                new SpriteDataMock.MockSpriteRepository(), new HashSet<>());

        sessionValue = ReflectionUtil.getDeclaredField(playbackStartAction, ISession.class, "session");
        businessUnitValue = ReflectionUtil.getDeclaredField(playbackStartAction, IBusinessUnit.class, "businessUnit");

        Assert.assertSame(playRequestSession, sessionValue);
        Assert.assertSame(playRequestSession.getBusinessUnit(), businessUnitValue);
    }

    @Test
    public void testAdInsertionParameters() throws JSONException, MalformedURLException {

        MockHttpHandler httpHandler = new MockHttpHandler();
        {
            JsonObjectBuilder playResponse = new JsonObjectBuilder();
            playResponse.putArray("formats").addObject()
                    .put("format", "DASH")
                    .put("mediaLocator", "http://example.com/manifest.mpd");
            httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), playResponse.toString());
            httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), playResponse.toString());
        }
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization()
                .setHttpHandler(httpHandler)
        );

        MockPlayRequest playRequest = new MockPlayRequest();
        playRequest.setPlayable(new MockPlayable("MockyMockMock"));
        IAdInsertionParameters adParameters = new MockAdParameters();

        DefaultPlaybackStartAction playbackStartAction = new DefaultPlaybackStartAction(
                new MockSession(), new MockSession().getBusinessUnit(), new MockTimeProvider(), playRequest,null,
                EnigmaRiverContext.getTaskFactoryProvider(), new MockPlayerImplementation(), new MockPlaybackStartAction.MockEnigmaPlayerCallbacks(),
                new SpriteDataMock.MockSpriteRepository(),
                new HashSet<>()) {
            @Override
            protected IAdInsertionParameters buildAdInsertionParameters(IPlayRequest playRequest) {
                return adParameters;
            }
        };

        // Execute the query
        playbackStartAction.startUsingAssetId("foo");
        // Fetch the play request query from the request url
        String query = new URL(new JSONObject(httpHandler.getLog().get(2)).getString("url")).getQuery();
        // Check that the mock parameters are included.
        Assert.assertTrue(query.contains("foo=bar&baz=42"));

        httpHandler.clearLog();

        // --- Test without IAdInsertionFactory in context

        // DefaultPlaybackStartAction does not override `buildAdInsertionParameters`
        playbackStartAction = new DefaultPlaybackStartAction(
                new MockSession(), new MockSession().getBusinessUnit(), new MockTimeProvider(), playRequest,null,
                EnigmaRiverContext.getTaskFactoryProvider(), new MockPlayerImplementation(), new MockPlaybackStartAction.MockEnigmaPlayerCallbacks(),
                new SpriteDataMock.MockSpriteRepository(),
                new HashSet<>());
        // Execute the query
        playbackStartAction.startUsingAssetId("foo");
        // Fetch the query from the request url
        query = new URL(new JSONObject(httpHandler.getLog().get(2)).getString("url")).getQuery();
        Assert.assertNull(query);

        httpHandler.clearLog();

        DefaultAdInsertionParameters defaultAdInsertionParameters = new DefaultAdInsertionParameters("42", "65536", false, "wtf", null, "", false);

        // Test with an `IAdInsertionParameters` returning a `DefaultAdInsertionParameters`
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization()
                .setHttpHandler(httpHandler).setAdInsertionFactory(new IAdInsertionFactory() {
                    @Override
                    public IAdInsertionParameters createParameters(IPlayRequest request) {
                        return defaultAdInsertionParameters;
                    }
                })
        );
        // Execute the query
        playbackStartAction.startUsingAssetId("foo");
        query = new URL(new JSONObject(httpHandler.getLog().get(2)).getString("url")).getQuery();

        for (Map.Entry<String, ?> kvp : defaultAdInsertionParameters.getParameters().entrySet()) {
            if (kvp.getValue() != null) {
                // Make sure each property is included
                Assert.assertTrue(query.contains(kvp.getKey() + "=" + kvp.getValue()));
            } else {
                // If property was null, it should not be included
                Assert.assertFalse(query.contains(kvp.getKey()));
            }
        }
    }

    private class MockAdParameters implements IAdInsertionParameters {

        @Override
        public Map<String, ?> getParameters() {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("foo", "bar");
            parameters.put("baz", "42");
            return parameters;
        }

        String asQueryString() {
            UrlPath path = new UrlPath("?");
            return path.appendQueryStringParameters(getParameters()).toString();
        }

    }

}
