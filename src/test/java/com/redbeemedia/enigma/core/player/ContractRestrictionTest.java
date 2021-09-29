package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.entitlement.IEntitlementProvider;
import com.redbeemedia.enigma.core.entitlement.MockEntitlementProvider;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.controls.AssertiveControlResultHandler;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.MockPlayRequest;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.json.JsonArrayBuilder;
import com.redbeemedia.enigma.core.testutil.json.JsonObjectBuilder;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.video.ISpriteRepository;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.regex.Pattern;

public class ContractRestrictionTest {

    @Test
    public void testRestrictions() throws JSONException {
        JSONObject jsonWithValues = new JSONObject();
        jsonWithValues.put("airplayEnabled", true);
        jsonWithValues.put("ffEnabled", true);
        jsonWithValues.put("maxBitrate", 10);
        jsonWithValues.put("maxResHeight", 5);
        jsonWithValues.put("minBitrate", 100);
        jsonWithValues.put("rwEnabled", true);
        jsonWithValues.put("timeshiftEnabled", true);


        EnigmaContractRestrictions enigmaContractRestrictions = EnigmaContractRestrictions.createWithDefaults(jsonWithValues);

        testContractRestriction(enigmaContractRestrictions, ContractRestriction.FASTFORWARD_ENABLED, true);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.MAX_BITRATE, 10);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.MAX_RES_HEIGHT, 5);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.MIN_BITRATE, 100);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.REWIND_ENABLED, true);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.TIMESHIFT_ENABLED, true);


        JSONObject jsonWithDifferentValues = new JSONObject();
        jsonWithDifferentValues.put("ffEnabled", false);
        jsonWithDifferentValues.put("airplayEnabled", false);
        jsonWithDifferentValues.put("maxBitrate", 123);
        jsonWithDifferentValues.put("timeshiftEnabled", false);
        jsonWithDifferentValues.put("maxResHeight", 7);
        jsonWithDifferentValues.put("rwEnabled", false);
        jsonWithDifferentValues.put("minBitrate", 200);
        enigmaContractRestrictions = EnigmaContractRestrictions.createWithDefaults(jsonWithDifferentValues);

        testContractRestriction(enigmaContractRestrictions, ContractRestriction.FASTFORWARD_ENABLED, false);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.MAX_BITRATE, 123);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.MAX_RES_HEIGHT, 7);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.MIN_BITRATE, 200);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.REWIND_ENABLED, false);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.TIMESHIFT_ENABLED, false);



        //Test defaults
        enigmaContractRestrictions = EnigmaContractRestrictions.createWithDefaults(new JSONObject());
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.FASTFORWARD_ENABLED, true);
        testContractRestrictionHasNoDefault(enigmaContractRestrictions, ContractRestriction.MAX_BITRATE);
        testContractRestrictionHasNoDefault(enigmaContractRestrictions, ContractRestriction.MAX_RES_HEIGHT);
        testContractRestrictionHasNoDefault(enigmaContractRestrictions, ContractRestriction.MIN_BITRATE);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.REWIND_ENABLED, true);
        testContractRestriction(enigmaContractRestrictions, ContractRestriction.TIMESHIFT_ENABLED, true);
    }

    private static <T> void testContractRestriction(IContractRestrictions contractRestrictions, IContractRestriction<T> contractRestriction, T expected) {
        if(expected == null) {
            throw new IllegalArgumentException();
        }
        T value = contractRestrictions.getValue(contractRestriction, null);
        Assert.assertEquals(expected, value);
    }

    private static <T> void testContractRestrictionHasNoDefault(IContractRestrictions contractRestrictions, IContractRestriction<T> contractRestriction) {
        Assert.assertNull(contractRestrictions.getValue(contractRestriction, null));
    }

    @Test
    public void testEnigmaPlayerFollowsFastForwardEnabledRestriction() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        JSONObject mock = new JSONObject();
        EnigmaPlayer enigmaPlayer = newPlayerWithMockRestrictions(mock, new MockPlayerImplementation() {
            private ITimelinePositionFactory timelinePositionFactory;
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                this.timelinePositionFactory = environment.getTimelinePositionFactory();
            }

            @Override
            public ITimelinePosition getCurrentPosition() {
                return timelinePositionFactory.newPosition(1000);
            }
        });
        final long[] seekPos = new long[1];
        final Counter onPlaybackSessionChangedCalled = new Counter();
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                int timesCalledBefore = onPlaybackSessionChangedCalled.getCounts();

                boolean expectedValue = timesCalledBefore == 0 ? true : false;
                testContractRestriction(to.getContractRestrictions(), ContractRestriction.FASTFORWARD_ENABLED, expectedValue);

                AssertiveControlResultHandler assertiveControlResultHandler = new AssertiveControlResultHandler();
                enigmaPlayer.getControls().seekTo(seekPos[0], assertiveControlResultHandler);
                if(timesCalledBefore == 1) {
                    assertiveControlResultHandler.assertOnRejectedCalled();
                } else {
                    assertiveControlResultHandler.assertOnDoneCalled();
                }

                onPlaybackSessionChangedCalled.count();
            }
        });

        seekPos[0] = 2000;
        mock.put("ffEnabled", true);
        onPlaybackSessionChangedCalled.assertNone();
        enigmaPlayer.play(new MockPlayRequest());
        onPlaybackSessionChangedCalled.assertOnce();


        mock.put("ffEnabled", false);
        onPlaybackSessionChangedCalled.assertCount(1);
        enigmaPlayer.play(new MockPlayRequest());
        onPlaybackSessionChangedCalled.assertCount(2);

        seekPos[0] = 0;
        enigmaPlayer.play(new MockPlayRequest());
        onPlaybackSessionChangedCalled.assertCount(3);
    }

    @Test
    public void testEnigmaPlayerFollowsRewindEnabledRestriction() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        JSONObject mock = new JSONObject();
        EnigmaPlayer enigmaPlayer = newPlayerWithMockRestrictions(mock, new MockPlayerImplementation() {
            private ITimelinePositionFactory timelinePositionFactory;
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                this.timelinePositionFactory = environment.getTimelinePositionFactory();
            }

            @Override
            public ITimelinePosition getCurrentPosition() {
                return timelinePositionFactory.newPosition(3000);
            }
        });
        final long[] seekPos = new long[1];
        final Counter onPlaybackSessionChangedCalled = new Counter();
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                int timesCalledBefore = onPlaybackSessionChangedCalled.getCounts();

                boolean expectedValue = timesCalledBefore == 0 ? true : false;
                testContractRestriction(to.getContractRestrictions(), ContractRestriction.REWIND_ENABLED, expectedValue);

                AssertiveControlResultHandler assertiveControlResultHandler = new AssertiveControlResultHandler();
                enigmaPlayer.getControls().seekTo(seekPos[0], assertiveControlResultHandler);
                if(timesCalledBefore == 1) {
                    assertiveControlResultHandler.assertOnRejectedCalled();
                } else {
                    assertiveControlResultHandler.assertOnDoneCalled();
                }

                onPlaybackSessionChangedCalled.count();
            }
        });

        seekPos[0] = 2000;
        mock.put("rwEnabled", true);
        onPlaybackSessionChangedCalled.assertNone();
        enigmaPlayer.play(new MockPlayRequest());
        onPlaybackSessionChangedCalled.assertOnce();

        mock.put("rwEnabled", false);
        onPlaybackSessionChangedCalled.assertCount(1);
        enigmaPlayer.play(new MockPlayRequest());
        onPlaybackSessionChangedCalled.assertCount(2);

        seekPos[0] = 4000;
        enigmaPlayer.play(new MockPlayRequest());
        onPlaybackSessionChangedCalled.assertCount(3);
    }

    @Test
    public void testMaxBitrate() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();
        httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), "{\"formats\": [{\"format\": \"DASH\", \"mediaLocator\": \"http://example.com/mock.mpd\"}]}");
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setTaskFactoryProvider(new ExecuteImmediatelyTaskFactoryProvider()));


        final Counter loadCalled = new Counter();

        JSONObject mock = new JSONObject();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedPlaybackSession(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void load(ILoadRequest loadRequest, IPlayerImplementationControlResultHandler resultHandler) {
                Integer maxBitrate = loadRequest.getMaxBitrate();
                Assert.assertNull(maxBitrate);
                loadCalled.count();
                super.load(loadRequest, resultHandler);
            }
        });
        loadCalled.assertNone();
        enigmaPlayer.play(new MockPlayRequest());
        loadCalled.assertCount(1);


        JsonObjectBuilder playResponse = new JsonObjectBuilder();
        {
            JsonArrayBuilder formats = playResponse.putArray("formats");
            formats.addObject()
                    .put("format", "DASH")
                    .put("mediaLocator", "http://example.com/mock.mpd");
            JsonObjectBuilder contractRestrictions = playResponse.putObject("contractRestrictions");
            contractRestrictions.put("maxBitrate", 10000);
        }
        httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), playResponse.toString());
        enigmaPlayer = new EnigmaPlayerWithMockedPlaybackSession(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void load(ILoadRequest loadRequest, IPlayerImplementationControlResultHandler resultHandler) {
                Integer maxBitrate = loadRequest.getMaxBitrate();
                Assert.assertEquals(Integer.valueOf(10000), maxBitrate);
                loadCalled.count();
                super.load(loadRequest, resultHandler);
            }
        });
        loadCalled.assertCount(1);
        enigmaPlayer.play(new MockPlayRequest());
        loadCalled.assertCount(2);
    }

    @Test
    public void testMaxResolutionHeight() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();
        httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), "{\"formats\": [{\"format\": \"DASH\", \"mediaLocator\": \"http://example.com/mock.mpd\"}]}");
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setTaskFactoryProvider(new ExecuteImmediatelyTaskFactoryProvider()));

        final Counter loadCalled = new Counter();

        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedPlaybackSession(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void load(ILoadRequest loadRequest, IPlayerImplementationControlResultHandler resultHandler) {
                Integer maxResoultionHeight = loadRequest.getMaxResoultionHeight();
                Assert.assertNull(maxResoultionHeight);
                loadCalled.count();
                super.load(loadRequest, resultHandler);
            }
        });
        loadCalled.assertNone();
        enigmaPlayer.play(new MockPlayRequest());
        loadCalled.assertCount(1);

        JsonObjectBuilder playResponse = new JsonObjectBuilder();
        {
            JsonArrayBuilder formats = playResponse.putArray("formats");
            formats.addObject()
                    .put("format", "DASH")
                    .put("mediaLocator", "http://example.com/mock.mpd");
            JsonObjectBuilder contractRestrictions = playResponse.putObject("contractRestrictions");
            contractRestrictions.put("maxResHeight", 720);
        }
        httpHandler.queueResponseOk(Pattern.compile(".*/entitlement/.*/play"), playResponse.toString());
        enigmaPlayer = new EnigmaPlayerWithMockedPlaybackSession(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void load(ILoadRequest loadRequest, IPlayerImplementationControlResultHandler resultHandler) {
                Integer maxResoultionHeight = loadRequest.getMaxResoultionHeight();
                Assert.assertEquals(Integer.valueOf(720), maxResoultionHeight);
                loadCalled.count();
                super.load(loadRequest, resultHandler);
            }
        });
        loadCalled.assertCount(1);
        enigmaPlayer.play(new MockPlayRequest());
        loadCalled.assertCount(2);
    }

    private static EnigmaPlayer newPlayerWithMockRestrictions(final JSONObject contractRestrictions, IPlayerImplementation playerImplementation) {
        return new EnigmaPlayerTest.EnigmaPlayerWithMockedTimeProvider(new MockSession(), playerImplementation) {
            @Override
            protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler, ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection, ISpriteRepository spriteRepository) {
                return new MockPlaybackStartAction(playRequest, playerConnection) {
                    @Override
                    protected IInternalPlaybackSession newInternalPlaybackSession() {
                        MockInternalPlaybackSession internalPlaybackSession = new MockInternalPlaybackSession(false);
                        internalPlaybackSession.setContractRestrictions(EnigmaContractRestrictions.createWithDefaults(contractRestrictions));
                        return internalPlaybackSession;
                    }
                };
            }

            @Override
            protected ITaskFactoryProvider getTaskFactoryProvider() {
                return new ITaskFactoryProvider() {
                    @Override
                    public ITaskFactory getTaskFactory() {
                        return getMockTaskFactory();
                    }

                    @Override
                    public ITaskFactory getMainThreadTaskFactory() {
                        return getMockTaskFactory();
                    }
                };
            }

            private ITaskFactory getMockTaskFactory() {
                return new ITaskFactory() {
                    @Override
                    public ITask newTask(final Runnable runnable) {
                        return new ITask() {
                            @Override
                            public void start() throws TaskException {
                                runnable.run();
                            }

                            @Override
                            public void startDelayed(long delayMillis) throws TaskException {
                                // Ignore
                            }

                            @Override
                            public void cancel(long joinMillis) throws TaskException {

                            }
                        };
                    }
                };
            }
        };
    }

    private static class ExecuteImmediatelyTaskFactoryProvider implements ITaskFactoryProvider {

        @Override
        public ITaskFactory getTaskFactory() {
            return new ITaskFactory() {
                @Override
                public ITask newTask(Runnable runnable) {
                    return new ITask() {
                        @Override
                        public void start() throws TaskException {
                            runnable.run();
                        }

                        @Override
                        public void startDelayed(long delayMillis) throws TaskException {
                            //Ignore
                        }

                        @Override
                        public void cancel(long joinMillis) throws TaskException {
                            //Ignore
                        }
                    };
                }
            };
        }

        @Override
        public ITaskFactory getMainThreadTaskFactory() {
            return getTaskFactory();
        }
    }

    private static class EnigmaPlayerWithMockedPlaybackSession extends EnigmaPlayer {
        public EnigmaPlayerWithMockedPlaybackSession(ISession session, IPlayerImplementation playerImplementation) {
            super(session, playerImplementation);
        }

        @Override
        protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler, ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection, ISpriteRepository spriteRepository) {
            return new DefaultPlaybackStartAction(session, businessUnit,timeProvider, playRequest, callbackHandler, taskFactoryProvider, playerImplementationControls, playerConnection, spriteRepository, new HashSet<>()) {
                @Override
                protected IInternalPlaybackSession newPlaybackSession(InternalPlaybackSession.ConstructorArgs constructorArgs) {
                    return new MockInternalPlaybackSession(false);
                }

                @Override
                protected IEntitlementProvider newEntitlementProvider() {
                    return new MockEntitlementProvider();
                }
            };
        }

        @Override
        protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayer.EnigmaPlayerLifecycle lifecycle) {
            return new MockTimeProvider();
        }
    }
}
