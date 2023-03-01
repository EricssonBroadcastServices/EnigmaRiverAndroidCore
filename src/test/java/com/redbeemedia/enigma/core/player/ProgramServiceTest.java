package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.entitlement.EntitlementData;
import com.redbeemedia.enigma.core.entitlement.EntitlementStatus;
import com.redbeemedia.enigma.core.entitlement.IEntitlementProvider;
import com.redbeemedia.enigma.core.entitlement.IEntitlementRequest;
import com.redbeemedia.enigma.core.entitlement.IEntitlementResponseHandler;
import com.redbeemedia.enigma.core.entitlement.listener.BaseEntitlementListener;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.MockProgram;
import com.redbeemedia.enigma.core.epg.response.MockEpgResponse;
import com.redbeemedia.enigma.core.error.AssetBlockedError;
import com.redbeemedia.enigma.core.error.ConcurrentStreamsLimitReachedError;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.GeoBlockedError;
import com.redbeemedia.enigma.core.error.NotAvailableError;
import com.redbeemedia.enigma.core.error.NotEntitledError;
import com.redbeemedia.enigma.core.error.NotPublishedError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpTask;
import com.redbeemedia.enigma.core.http.MockHttpTask;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.task.MockTaskFactoryProvider;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.ISO8601Util;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ProgramServiceTest {

    @Test
    public void testEntitlementCallWhenProgramChange() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        JsonStreamInfo streamInfo = new MockStreamInfo(MockStreamInfo.Args.vod());
        List<IProgram> programs = new ArrayList<>();
        programs.add(new MockProgram("Program 1", 1000, 5000).setAssetId("asset_1"));
        programs.add(new MockProgram("Program 2", 5000, 9000).setAssetId("asset_2"));
        Duration asset3End = Duration.millis(10000).add(Duration.minutes(5));
        Duration asset4End = asset3End.add(Duration.hours(1));
        programs.add(new MockProgram("Program 3", 9000, asset3End.inWholeUnits(Duration.Unit.MILLISECONDS)).setAssetId("asset_3"));
        programs.add(new MockProgram("Program 4", asset3End.inWholeUnits(Duration.Unit.MILLISECONDS), asset4End.inWholeUnits(Duration.Unit.MILLISECONDS)).setAssetId("asset_4"));
        IStreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(1000, asset4End.inWholeUnits(Duration.Unit.MILLISECONDS), programs),false, 0L);
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        final Counter checkEntitlementCalled = new Counter();

        final String[] assetIdChecked = {null};
        IEntitlementProvider entitlementProvider = new IEntitlementProvider() {
            @Override
            public void checkEntitlement(IEntitlementRequest entitlementRequest, IEntitlementResponseHandler responseHandler) {
                assetIdChecked[0] = entitlementRequest.getAssetId();
                switch (checkEntitlementCalled.getCounts()) {
                    case 0: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.SUCCESS));
                    } break;
                    case 1: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.SUCCESS));
                    } break;
                    case 2: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.NOT_ENTITLED));
                    } break;
                    case 3: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.NOT_ENTITLED));
                    } break;
                    default: {
                        Assert.fail("Unexpected");
                    } break;
                }
                checkEntitlementCalled.count();
            }
        };
        IPlaybackSession playbackSession = new MockInternalPlaybackSession(true) {
            @Override
            public void addListener(IPlaybackSessionListener listener) {
                listener.onPlayingFromLiveChanged(true);
            }
        };
        final MockTimeProvider mockTimeProvider = new MockTimeProvider(42463683);
        ProgramService programService = new ProgramService(new MockSession(), streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession, new MockTaskFactoryProvider(), true) {
            @Override
            protected ITimeProvider createTimeProviderForCache() {
                return mockTimeProvider;
            }

        };
        final Counter onEntitlementChangedCalled = new Counter();
        programService.addEntitlementListener(new BaseEntitlementListener() {
            @Override
            public void onEntitlementChanged(EntitlementData oldData, EntitlementData newData) {
                switch (onEntitlementChangedCalled.getCounts()) {
                    case 0: {
                        Assert.assertEquals(EntitlementStatus.SUCCESS, newData.getStatus());
                    } break;
                    case 1: {
                        Assert.assertEquals(EntitlementStatus.NOT_ENTITLED, newData.getStatus());
                    } break;
                    default: {
                        Assert.fail("Unexpected call "+onEntitlementChangedCalled.getCounts());
                    } break;
                }
                onEntitlementChangedCalled.count();
            }
        });

        checkEntitlementCalled.setExpectedCounts(0);
        onEntitlementChangedCalled.setExpectedCounts(0);
        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();

        playbackSessionInfo.setCurrentPlaybackOffset(Duration.millis(10000));
        MockProgram toProgram = new MockProgram("To Program", 1000, 5000).setAssetId("asset_123456");
        programService.checkEntitlementForProgram(toProgram);
        checkEntitlementCalled.addToExpected(1);
        onEntitlementChangedCalled.addToExpected(1);
        Assert.assertEquals(assetIdChecked[0], "asset_123456");
        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();

        playbackSessionInfo.setCurrentPlaybackOffset(asset3End.subtract(Duration.seconds(20)));
        programService.checkEntitlementForProgram(null);
        Assert.assertEquals(assetIdChecked[0], "asset_1");
        checkEntitlementCalled.addToExpected(2);
        onEntitlementChangedCalled.addToExpected(0);

        //After 4 minutes (since cache time is currently 5 minutes) the cached response should be used.
        Duration timePassage = Duration.minutes(4);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));
        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(0);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();


        //After 30 seconds more the cached time should still be used.
        timePassage = Duration.seconds(30);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));

        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(0);
        onEntitlementChangedCalled.addToExpected(0);
    }

    @Test
    public void testEntitlementCall() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        JsonStreamInfo streamInfo = new MockStreamInfo(MockStreamInfo.Args.vod());
        List<IProgram> programs = new ArrayList<>();
        programs.add(new MockProgram("Program 1", 1000, 5000).setAssetId("asset_1"));
        programs.add(new MockProgram("Program 2", 5000, 9000).setAssetId("asset_2"));
        Duration asset3End = Duration.millis(10000).add(Duration.minutes(5));
        Duration asset4End = asset3End.add(Duration.hours(1));
        programs.add(new MockProgram("Program 3", 9000, asset3End.inWholeUnits(Duration.Unit.MILLISECONDS)).setAssetId("asset_3"));
        programs.add(new MockProgram("Program 4", asset3End.inWholeUnits(Duration.Unit.MILLISECONDS), asset4End.inWholeUnits(Duration.Unit.MILLISECONDS)).setAssetId("asset_4"));
        IStreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(1000, asset4End.inWholeUnits(Duration.Unit.MILLISECONDS), programs),false, 0L);
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        final Counter checkEntitlementCalled = new Counter();
        IEntitlementProvider entitlementProvider = new IEntitlementProvider() {
            @Override
            public void checkEntitlement(IEntitlementRequest entitlementRequest, IEntitlementResponseHandler responseHandler) {
                switch (checkEntitlementCalled.getCounts()) {
                    case 0: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.SUCCESS));
                    } break;
                    case 1: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.SUCCESS));
                    } break;
                    case 2: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.NOT_ENTITLED));
                    } break;
                    case 3: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.NOT_ENTITLED));
                    } break;
                    default: {
                        Assert.fail("Unexpected");
                    } break;
                }
                checkEntitlementCalled.count();
            }
        };
        IPlaybackSession playbackSession = new MockInternalPlaybackSession(true) {
            @Override
            public void addListener(IPlaybackSessionListener listener) {
                listener.onPlayingFromLiveChanged(true);
            }
        };
        final MockTimeProvider mockTimeProvider = new MockTimeProvider(42463683);
        ProgramService programService = new ProgramService(new MockSession(), streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession, new MockTaskFactoryProvider(), true) {
            @Override
            protected ITimeProvider createTimeProviderForCache() {
                return mockTimeProvider;
            }

        };
        final Counter onEntitlementChangedCalled = new Counter();
        programService.addEntitlementListener(new BaseEntitlementListener() {
            @Override
            public void onEntitlementChanged(EntitlementData oldData, EntitlementData newData) {
                switch (onEntitlementChangedCalled.getCounts()) {
                    case 0: {
                        Assert.assertEquals(EntitlementStatus.SUCCESS, newData.getStatus());
                    } break;
                    case 1: {
                        Assert.assertEquals(EntitlementStatus.NOT_ENTITLED, newData.getStatus());
                    } break;
                    default: {
                        Assert.fail("Unexpected call "+onEntitlementChangedCalled.getCounts());
                    } break;
                }
                onEntitlementChangedCalled.count();
            }
        });

        checkEntitlementCalled.setExpectedCounts(0);
        onEntitlementChangedCalled.setExpectedCounts(0);
        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();


        playbackSessionInfo.setCurrentPlaybackOffset(Duration.millis(10000));
        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(1);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();

        playbackSessionInfo.setCurrentPlaybackOffset(asset3End.subtract(Duration.seconds(20)));
        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(2);
        onEntitlementChangedCalled.addToExpected(0);

        //After 4 minutes (since cache time is currently 5 minutes) the cached response should be used.
        Duration timePassage = Duration.minutes(4);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));
        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(0);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();


        //After 30 seconds more the cached time should still be used.
        timePassage = Duration.seconds(30);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));

        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(0);
        onEntitlementChangedCalled.addToExpected(0);
    }

    @Test
    public void testEntitlementCallForLive() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        JsonStreamInfo streamInfo = new MockStreamInfo(MockStreamInfo.Args.liveStream());
        List<IProgram> programs = new ArrayList<>();
        programs.add(new MockProgram("Program 1", 1000, 5000).setAssetId("asset_1"));
        programs.add(new MockProgram("Program 2", 5000, 9000).setAssetId("asset_2"));
        Duration asset3End = Duration.millis(10000).add(Duration.minutes(5));
        Duration asset4End = asset3End.add(Duration.hours(1));
        programs.add(new MockProgram("Program 3", 9000, asset3End.inWholeUnits(Duration.Unit.MILLISECONDS)).setAssetId("asset_3"));
        programs.add(new MockProgram("Program 4", asset3End.inWholeUnits(Duration.Unit.MILLISECONDS), asset4End.inWholeUnits(Duration.Unit.MILLISECONDS)).setAssetId("asset_4"));
        IStreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(1000, asset4End.inWholeUnits(Duration.Unit.MILLISECONDS), programs),false, 0L);
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        final Counter checkEntitlementCalled = new Counter();
        IEntitlementProvider entitlementProvider = new IEntitlementProvider() {
            @Override
            public void checkEntitlement(IEntitlementRequest entitlementRequest, IEntitlementResponseHandler responseHandler) {
                switch (checkEntitlementCalled.getCounts()) {
                    case 0: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.SUCCESS));
                    } break;
                    case 1: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.SUCCESS));
                    } break;
                    case 2: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.NOT_ENTITLED));
                    } break;
                    case 3: {
                        responseHandler.onResponse(new EntitlementData(EntitlementStatus.NOT_ENTITLED));
                    } break;
                    default: {
                        Assert.fail("Unexpected");
                    } break;
                }
                checkEntitlementCalled.count();
            }
        };
        IPlaybackSession playbackSession = new MockInternalPlaybackSession(true) {
            @Override
            public void addListener(IPlaybackSessionListener listener) {
                listener.onPlayingFromLiveChanged(true);
            }
        };
        final MockTimeProvider mockTimeProvider = new MockTimeProvider(42463683);
        ProgramService programService = new ProgramService(new MockSession(), streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession, new MockTaskFactoryProvider(), true) {
            @Override
            protected ITimeProvider createTimeProviderForCache() {
                return mockTimeProvider;
            }

        };
        final Counter onEntitlementChangedCalled = new Counter();
        programService.addEntitlementListener(new BaseEntitlementListener() {
            @Override
            public void onEntitlementChanged(EntitlementData oldData, EntitlementData newData) {
                switch (onEntitlementChangedCalled.getCounts()) {
                    case 0: {
                        Assert.assertEquals(EntitlementStatus.SUCCESS, newData.getStatus());
                    } break;
                    case 1: {
                        Assert.assertEquals(EntitlementStatus.NOT_ENTITLED, newData.getStatus());
                    } break;
                    default: {
                        Assert.fail("Unexpected call "+onEntitlementChangedCalled.getCounts());
                    } break;
                }
                onEntitlementChangedCalled.count();
            }
        });

        checkEntitlementCalled.setExpectedCounts(0);
        onEntitlementChangedCalled.setExpectedCounts(0);
        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();


        playbackSessionInfo.setCurrentPlaybackOffset(Duration.millis(10000));
        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(1);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();

        playbackSessionInfo.setCurrentPlaybackOffset(asset3End.subtract(Duration.seconds(20)));
        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(2);
        onEntitlementChangedCalled.addToExpected(0);

        //After 4 minutes (since cache time is currently 5 minutes) the cached response should be used.
        Duration timePassage = Duration.minutes(4);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));
        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(0);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();


        //After 30 seconds more the cached time should still be used.
        timePassage = Duration.seconds(30);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));

        programService.checkEntitlementForProgram(null);
        checkEntitlementCalled.addToExpected(0);
        onEntitlementChangedCalled.addToExpected(0);
    }

    @Test
    public void testEntitlementCallsIsCheckedUsingCorrectTime() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        playbackSessionInfo.setCurrentPlaybackOffset(Duration.millis(45000));
        Duration channelStart = Duration.days(365*30).add(Duration.hours(11));
        final Duration fuzz = Duration.seconds(45);
        final String expectedTimeCheckDate = ISO8601Util.newWriter(TimeZone.getTimeZone("UTC")).toIso8601(playbackSessionInfo.getCurrentPlaybackOffset().add(fuzz).add(Duration.millis(channelStart.inWholeUnits(Duration.Unit.MILLISECONDS))).inWholeUnits(Duration.Unit.MILLISECONDS));
        Assert.assertEquals("1999-12-25T11:01:30Z", expectedTimeCheckDate);
        JsonStreamInfo streamInfo = new MockStreamInfo(MockStreamInfo.Args.liveStream().setChannelId("channelZero").setStart(channelStart));
        List<IProgram> programs = new ArrayList<>();
        programs.add(new MockProgram("Program 1", 1000, 50000).setAssetId("asset_1"));
        programs.add(new MockProgram("Program 2", 50000, 100000).setAssetId("asset_2"));
        IStreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(1000, 100000, programs),false, 0L);
        final Counter httpCallMade = new Counter();
        IEntitlementProvider entitlementProvider = new IEntitlementProvider() {
            private final Counter callCounter = new Counter();
            @Override
            public void checkEntitlement(IEntitlementRequest entitlementRequest, IEntitlementResponseHandler responseHandler) {
                try {
                    entitlementRequest.doHttpCall(new IHttpHandler() {
                        @Override
                        public IHttpTask doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
                            callCounter.count();
                            if(callCounter.getCounts() == 1) {
                                Assert.assertTrue(url.toString().contains("https://mock.unittests.example.com/v2/customer/mockCu/businessunit/mockBu/entitlement/asset_1/entitle"));
                            } else if(callCounter.getCounts() == 2) {
                                Assert.assertEquals("time="+expectedTimeCheckDate, url.getQuery());
                            } else {
                                Assert.fail(url.toString());
                            }
                            httpCallMade.count();
                            return new MockHttpTask();
                        }

                        @Override
                        public void doHttpBlocking(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) throws InterruptedException {
                            Assert.fail("unexpected usage");
                        }
                    }, new IHttpHandler.IHttpResponseHandler() {
                        @Override
                        public void onResponse(HttpStatus httpStatus) {
                        }

                        @Override
                        public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                        }

                        @Override
                        public void onException(Exception e) {
                            e.printStackTrace();
                            Assert.fail(e.getMessage());
                        }
                    });
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        IPlaybackSession playbackSession = new MockInternalPlaybackSession(true) {
            @Override
            public void addListener(IPlaybackSessionListener listener) {
                listener.onPlayingFromLiveChanged(true);
            }
        };
        final MockTimeProvider mockTimeProvider = new MockTimeProvider(42463683);
        ProgramService programService = new ProgramService(new MockSession(), streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession, new MockTaskFactoryProvider(), true) {
            @Override
            protected ITimeProvider createTimeProviderForCache() {
                return mockTimeProvider;
            }

        };
        httpCallMade.setExpectedCounts(0);
        httpCallMade.assertExpected();


        programService.checkEntitlementForProgram(null);
        httpCallMade.setExpectedCounts(1);

        httpCallMade.assertExpected();
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
            ProgramService.handleEntitlementStatus(comChannel, status);
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
        ProgramService.handleEntitlementStatus(comChannel, null);
        onPlaybackErrorCalledForNull.assertOnce();

        //Assert all types listed in expectedErrorType-map checked
        errorTypesChecked.assertCount(expectedErrorType.size());
    }
}
