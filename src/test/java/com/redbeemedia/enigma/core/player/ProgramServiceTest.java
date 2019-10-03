package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.MockProgram;
import com.redbeemedia.enigma.core.epg.response.MockEpgResponse;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.entitlement.EntitlementData;
import com.redbeemedia.enigma.core.entitlement.EntitlementStatus;
import com.redbeemedia.enigma.core.entitlement.IEntitlementProvider;
import com.redbeemedia.enigma.core.entitlement.IEntitlementRequest;
import com.redbeemedia.enigma.core.entitlement.IEntitlementResponseHandler;
import com.redbeemedia.enigma.core.entitlement.listener.BaseEntitlementListener;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ProgramServiceTest {
    @Test
    public void testCache() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        StreamInfo streamInfo = new MockStreamInfo(MockStreamInfo.Args.liveStream());
        List<IProgram> programs = new ArrayList<>();
        programs.add(new MockProgram("Program 1", 1000, 5000).setAssetId("asset_1"));
        programs.add(new MockProgram("Program 2", 5000, 9000).setAssetId("asset_2"));
        Duration asset3End = Duration.millis(10000).add(Duration.minutes(5));
        Duration asset4End = asset3End.add(Duration.hours(1));
        programs.add(new MockProgram("Program 3", 9000, asset3End.inWholeUnits(Duration.Unit.MILLISECONDS)).setAssetId("asset_3"));
        programs.add(new MockProgram("Program 4", asset3End.inWholeUnits(Duration.Unit.MILLISECONDS), asset4End.inWholeUnits(Duration.Unit.MILLISECONDS)).setAssetId("asset_4"));
        IStreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(1000, 20000, programs));
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
        ProgramService programService = new ProgramService(new MockSession(), streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession) {
            @Override
            protected ITimeProvider createTimeProviderForCache() {
                return mockTimeProvider;
            }

            @Override
            protected Duration generateFuzzyOffset(Duration min, Duration max) {
                Assert.assertEquals("Values in reset of unit tests assumes these values",Duration.seconds(30), min);
                Assert.assertEquals("Values in reset of unit tests assumes these values", Duration.minutes(2), max);
                return min.add(max).multiply(0.5f);//Mean
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
        programService.checkEntitlement();
        checkEntitlementCalled.addToExpected(1);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();

        playbackSessionInfo.setCurrentPlaybackOffset(asset3End.subtract(Duration.seconds(20)));
        programService.checkEntitlement();
        checkEntitlementCalled.addToExpected(2);
        onEntitlementChangedCalled.addToExpected(0);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();

        //After 4 minutes (since cache time is currently 5 minutes) the cached response should be used.
        Duration timePassage = Duration.minutes(4);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));
        programService.checkEntitlement();
        checkEntitlementCalled.addToExpected(0);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();


        //After 30 seconds more the cached time should still be used.
        timePassage = Duration.seconds(30);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));

        programService.checkEntitlement();
        checkEntitlementCalled.addToExpected(0);
        onEntitlementChangedCalled.addToExpected(0);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();

        //After 3 minutes the cache is no longer used. But the entitlement has not changed.
        timePassage = Duration.minutes(3);
        mockTimeProvider.addTime(timePassage.inWholeUnits(Duration.Unit.MILLISECONDS));
        playbackSessionInfo.setCurrentPlaybackOffset(playbackSessionInfo.getCurrentPlaybackOffset().add(timePassage));

        programService.checkEntitlement();
        checkEntitlementCalled.addToExpected(1);
        onEntitlementChangedCalled.addToExpected(0);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();
    }


    @Test
    public void testFallbackToChannelIfGapInEpg() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        StreamInfo streamInfo = new MockStreamInfo(MockStreamInfo.Args.liveStream().setChannelId("channelZero"));
        List<IProgram> programs = new ArrayList<>();
        programs.add(new MockProgram("Program 1", 1000, 100000).setAssetId("asset_1"));
        IStreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(1000, 100000, programs));
        MockPlaybackSessionInfo playbackSessionInfo = new MockPlaybackSessionInfo();
        final Counter checkEntitlementCalled = new Counter();
        final EntitlementStatus[] channelZeroEntitlement = new EntitlementStatus[]{EntitlementStatus.SUCCESS};
        IEntitlementProvider entitlementProvider = new IEntitlementProvider() {
            @Override
            public void checkEntitlement(IEntitlementRequest entitlementRequest, IEntitlementResponseHandler responseHandler) {
                String assetId = entitlementRequest.getAssetId();
                if("asset_1".equals(assetId)) {
                    responseHandler.onResponse(new EntitlementData(EntitlementStatus.GAP_IN_EPG));
                } else if ("channelZero".equals(assetId)) {
                    responseHandler.onResponse(new EntitlementData(channelZeroEntitlement[0]));
                } else {
                    responseHandler.onResponse(new EntitlementData(EntitlementStatus.ANONYMOUS_IP_BLOCKED));
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
        ProgramService programService = new ProgramService(new MockSession(), streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession) {
            @Override
            protected ITimeProvider createTimeProviderForCache() {
                return mockTimeProvider;
            }

            @Override
            protected Duration generateFuzzyOffset(Duration min, Duration max) {
                return min.add(max).multiply(0.5f);//Mean
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
                        Assert.assertEquals(EntitlementStatus.GEO_BLOCKED, newData.getStatus());
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


        playbackSessionInfo.setCurrentPlaybackOffset(Duration.millis(2000));
        programService.checkEntitlement();
        checkEntitlementCalled.addToExpected(2);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();

        channelZeroEntitlement[0] = EntitlementStatus.GEO_BLOCKED;
        programService.checkEntitlement();
        checkEntitlementCalled.addToExpected(2);
        onEntitlementChangedCalled.addToExpected(1);

        checkEntitlementCalled.assertExpected();
        onEntitlementChangedCalled.assertExpected();
    }
}
