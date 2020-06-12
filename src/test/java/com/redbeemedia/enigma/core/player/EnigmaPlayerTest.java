package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.audio.MockAudioTrack;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.entitlement.EntitlementProvider;
import com.redbeemedia.enigma.core.epg.IEpg;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.MockEpgLocator;
import com.redbeemedia.enigma.core.epg.MockProgram;
import com.redbeemedia.enigma.core.epg.impl.MockEpg;
import com.redbeemedia.enigma.core.epg.response.MockEpgResponse;
import com.redbeemedia.enigma.core.error.EmptyResponseError;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playable.MockPlayable;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.controls.AssertiveControlResultHandler;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.player.track.MockPlayerImplementationTrack;
import com.redbeemedia.enigma.core.playrequest.BasePlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.playrequest.MockPlayRequest;
import com.redbeemedia.enigma.core.playrequest.MockPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.PlayRequest;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.subtitle.MockSubtitleTrack;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.testutil.InstanceOfMatcher;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.MockHandler;
import com.redbeemedia.enigma.core.video.IVideoTrack;
import com.redbeemedia.enigma.core.video.MockVideoTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EnigmaPlayerTest {
    @Test
    public void testPlayer() throws JSONException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        queuePlayResponse(mockHttpHandler, new MockPlayResponse());
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        final Flag loadCalled = new Flag();
        final Flag startCalled = new Flag();
        final Flag onErrorCalled = new Flag();
        final Flag useWithCalled = new Flag();
        final Counter installCalled = new Counter();
        IPlayerImplementation impl = new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                installCalled.count();
            }

            @Override
            public void load(ILoadRequest loadRequest, IPlayerImplementationControlResultHandler resultHandler) {
                loadCalled.setFlag();
                super.load(loadRequest, resultHandler);
            }

            @Override
            public void start(IPlayerImplementationControlResultHandler resultHandler) {
                startCalled.setFlag();
                super.start(resultHandler);
            }
        };
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), impl);
        Assert.assertFalse(loadCalled.isTrue());
        Assert.assertFalse(startCalled.isTrue());
        installCalled.assertOnce();
        enigmaPlayer.play(new MockPlayRequest().setPlayable(new MockPlayable("123") {
            @Override
            public void useWith(IPlayableHandler playableHandler) {
                useWithCalled.setFlag();
                super.useWith(playableHandler);
            }
        }).setResultHandler(new MockPlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                onErrorCalled.setFlag();
                error.printStackTrace();
                throw new RuntimeException(error.getClass().getSimpleName()+": "+error.getErrorCode());
            }
        }));
        Assert.assertTrue(useWithCalled.isTrue());
        Assert.assertFalse(onErrorCalled.isTrue());
        Assert.assertTrue(loadCalled.isTrue());
        Assert.assertTrue(startCalled.isTrue());
    }

    private static JSONObject createFormatJson(String mediaLocator, String format, String drmKey, Long liveDelay) throws JSONException {
        JSONObject mediaFormat = new JSONObject();
        mediaFormat.put("mediaLocator", mediaLocator);
        if(liveDelay != null) {
            mediaFormat.put("liveDelay", liveDelay.longValue());
        }
        mediaFormat.put("format", format);
        if(drmKey != null) {
            JSONObject drm = new JSONObject();
            JSONObject drmInfo = new JSONObject();
            drmInfo.put("licenseServerUrl", "www.license-server.example.com");
            drm.put(drmKey, drmInfo);
            mediaFormat.put("drm", drm);
        }

        return mediaFormat;
    }

    @Test
    public void testParseMediaFormat() throws JSONException {
        EnigmaMediaFormat DASH_UNENC = new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.NONE);
        EnigmaMediaFormat DASH_WIDEVINE = new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.WIDEVINE);
        EnigmaMediaFormat HLS_FAIRPLAY = new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.HLS, EnigmaMediaFormat.DrmTechnology.FAIRPLAY);

        JSONObject dashUnencMediaFormat = new JSONObject();
        dashUnencMediaFormat.put("format", "DASH");
        Assert.assertEquals(DASH_UNENC, EnigmaPlayer.parseMediaFormat(dashUnencMediaFormat));

        JSONObject dashEncMediaFormat = new JSONObject();
        dashEncMediaFormat.put("format", "DASH");
        {
            JSONObject drm = new JSONObject();
            drm.put("com.widevine.alpha", new JSONObject());
            dashEncMediaFormat.put("drm", drm);
        }
        Assert.assertEquals(DASH_WIDEVINE, EnigmaPlayer.parseMediaFormat(dashEncMediaFormat));

        JSONObject hlsEncMediaFormat = new JSONObject();
        hlsEncMediaFormat.put("format", "HLS");
        {
            JSONObject drm = new JSONObject();
            drm.put("com.apple.fairplay", new JSONObject());
            hlsEncMediaFormat.put("drm", drm);
        }
        Assert.assertEquals(HLS_FAIRPLAY, EnigmaPlayer.parseMediaFormat(hlsEncMediaFormat));
    }

    @Test
    public void testInstallSupportedFormats() throws JSONException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        {
            MockPlayResponse mockPlayResponse = new MockPlayResponse();
            mockPlayResponse.formats.clear();
            mockPlayResponse.addFormat("https://media.example.com?format=HLS", "HLS");
            mockPlayResponse.addFormat("https://media.example.com", "DASH");
            mockPlayResponse.addFormat("https://media.example.com", "DASH", EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey());
            mockPlayResponse.streamInfoData.live = false;
            queuePlayResponse(mockHttpHandler, mockPlayResponse);
        }
        {
            MockPlayResponse mockPlayResponse = new MockPlayResponse();
            mockPlayResponse.formats.clear();
            mockPlayResponse.addFormat("https://media.example.com", "DASH", EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey());
            mockPlayResponse.addFormat("https://media.example.com?format=HLS", "HLS");
            mockPlayResponse.streamInfoData.live = true;
            mockPlayResponse.streamInfoData.start = 1505574300000L;
            queuePlayResponse(mockHttpHandler, mockPlayResponse);
        }
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));
        final Flag installed = new Flag();
        final Counter playbackStartedCalls = new Counter();
        IPlayerImplementation playerImpl = new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                environment.setMediaFormatSupportSpec(new IMediaFormatSupportSpec() {
                    private final EnigmaMediaFormat DASH_UNENC = new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.NONE);
                    @Override
                    public boolean supports(EnigmaMediaFormat enigmaMediaFormat) {
                        return enigmaMediaFormat.equals(DASH_UNENC);
                    }
                });
                installed.setFlag();
            }

            @Override
            public void start(IPlayerImplementationControlResultHandler resultHandler) {
                playbackStartedCalls.count();
                super.start(resultHandler);
            }
        };
        installed.assertNotSet();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), playerImpl);
        installed.assertSet();
        enigmaPlayer.play(new MockPlayRequest("123").setResultHandler(new MockPlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                error.printStackTrace();
                Assert.fail(error.getClass().getSimpleName()+": "+error.getErrorCode());
            }
        }));
        playbackStartedCalls.assertOnce();
        final EnigmaError[] errorGotten = new EnigmaError[]{null};
        enigmaPlayer.play(new MockPlayRequest("7s4s4ts").setResultHandler(new MockPlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                errorGotten[0] = error;
            }
        }));
        playbackStartedCalls.assertOnce();
        Assert.assertNotEquals(null, errorGotten[0]);
        Assert.assertThat(errorGotten[0], new InstanceOfMatcher<>(NoSupportedMediaFormatsError.class));
    }

    @Test
    public void testListeners() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        final IPlayerImplementationListener[] playerImplementationListener = new IPlayerImplementationListener[]{null};
        final Flag installCalled = new Flag();
        IPlayerImplementation impl = new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                playerImplementationListener[0] = environment.getPlayerImplementationListener();
                installCalled.setFlag();
            }
        };
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), impl) {
            @Override
            protected ITimeProvider newTimeProvider(ISession session) {
                return new MockTimeProvider();
            }
        };
        installCalled.assertSet();
        Assert.assertNotEquals(null, playerImplementationListener[0]);
        Counter onPlaybackErrorCalled = new Counter();
        BaseEnigmaPlayerListener listener = new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackError(EnigmaError error) {
                Assert.assertThat(error, new InstanceOfMatcher<>(EmptyResponseError.class));
                onPlaybackErrorCalled.count();
            }
        };
        Assert.assertTrue(enigmaPlayer.addListener(listener));
        onPlaybackErrorCalled.assertNone();
        playerImplementationListener[0].onError(new EmptyResponseError("TEST!"));
        onPlaybackErrorCalled.assertCount(1);
        playerImplementationListener[0].onError(new EmptyResponseError("TEST 2!"));
        onPlaybackErrorCalled.assertCount(2);
        Assert.assertTrue(enigmaPlayer.removeListener(listener));
        playerImplementationListener[0].onError(new EmptyResponseError("TEST 3!"));
        onPlaybackErrorCalled.assertCount(2);
        Assert.assertFalse(enigmaPlayer.removeListener(listener));
    }

    @Test
    public void testCallbackHandler() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        final IPlayerImplementationListener[] playerImplementationListener = new IPlayerImplementationListener[]{null};
        final Flag installCalled = new Flag();

        MockHandler handler = new MockHandler();
        IPlayerImplementation impl = new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                playerImplementationListener[0] = environment.getPlayerImplementationListener();
                installCalled.setFlag();
            }
        };
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), impl) {
            @Override
            protected ITimeProvider newTimeProvider(ISession session) {
                return new MockTimeProvider();
            }
        };
        installCalled.assertSet();
        Assert.assertNotEquals(null, playerImplementationListener[0]);

        final Counter onErrorCalled = new Counter();
        IEnigmaPlayerListener listener = new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackError(EnigmaError error) {
                onErrorCalled.count();
            }
        };
        Assert.assertTrue(enigmaPlayer.addListener(listener));
        playerImplementationListener[0].onError(new UnexpectedError("Testing"));
        onErrorCalled.assertCount(1);

        enigmaPlayer.addListener(listener, handler);

        playerImplementationListener[0].onError(new UnexpectedError("Testing again"));
        Assert.assertEquals(1, handler.runnables.size());
        onErrorCalled.assertCount(2);
        handler.runnables.get(0).run();
        onErrorCalled.assertCount(3);

        playerImplementationListener[0].onError(new UnexpectedError("Testing third"));
        Assert.assertEquals(2, handler.runnables.size());

        enigmaPlayer.removeListener(listener);
        handler.runnables.get(1).run();
        onErrorCalled.assertCount(5);
    }

    @Test
    public void testStateChangeListener() throws JSONException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        {
            MockPlayResponse mockPlayResponse = new MockPlayResponse();
            queuePlayResponse(mockHttpHandler, mockPlayResponse);
        }

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                environment.setMediaFormatSupportSpec(enigmaMediaFormat -> true);
            }
        });

        final StringBuilder log = new StringBuilder();
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
                log.append("["+from+"->"+to+"]");
            }
        });
        MockPlayRequest mockPlayRequest = new MockPlayRequest();
        enigmaPlayer.play(mockPlayRequest);
        Assert.assertEquals("[IDLE->LOADING][LOADING->LOADED][LOADED->PLAYING]",log.toString());
    }

    @Test
    public void testOnReadyListenersSimple() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        final Counter onReadyCalled = new Counter();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                environment.addEnigmaPlayerReadyListener(enigmaPlayer1 -> onReadyCalled.count());
            }
        });
        onReadyCalled.assertOnce();
    }

    @Test
    public void testOnReadyListenersFail() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        final Counter onReadyCalled = new Counter();
        try {
            EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new MockPlayerImplementation() {
                @Override
                public void install(IEnigmaPlayerEnvironment environment) {
                    environment.addEnigmaPlayerReadyListener(enigmaPlayer1 -> onReadyCalled.count());
                }
            }) {
                @Override
                protected ITimeProvider newTimeProvider(ISession session) {
                    return new MockTimeProvider();
                }
            };
            Assert.fail("Expected exception to have been thrown");
        } catch (IllegalStateException e) {
            Assert.assertEquals("PlayerImplementation did not provide controls!",e.getMessage());
        }
        onReadyCalled.assertNone();
    }

    @Test
    public void testOnReadyListenersMultiple() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        final Counter onReadyCalled = new Counter();
        final Counter onReadyCalled2 = new Counter();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                environment.addEnigmaPlayerReadyListener(enigmaPlayer1 -> onReadyCalled.count());
                environment.addEnigmaPlayerReadyListener(enigmaPlayer1 -> onReadyCalled2.count());
            }
        });
        onReadyCalled.assertOnce();
        onReadyCalled2.assertOnce();
    }


    @Test
    public void testPlayableReachableFromPlaybackSession() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();

        for(int i = 0; i < 3; ++i) {
            queuePlayResponse(httpHandler, new MockPlayResponse());
        }

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler));
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation());

        IPlayable playable1 = new MockPlayable("asset1");
        IPlayable playable2 = new MockPlayable("asset2");

        final Counter onPlaybackSessionChangedCalled = new Counter();
        final IPlayable[] currentPlayable = new IPlayable[]{null};
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                onPlaybackSessionChangedCalled.count();
                Assert.assertEquals(currentPlayable[0], to.getPlayable());
            }
        });
        onPlaybackSessionChangedCalled.assertCount(0);

        currentPlayable[0] = playable1;
        enigmaPlayer.play(new MockPlayRequest().setPlayable(currentPlayable[0]));
        onPlaybackSessionChangedCalled.assertCount(1);

        currentPlayable[0] = playable2;
        enigmaPlayer.play(new MockPlayRequest().setPlayable(currentPlayable[0]));
        onPlaybackSessionChangedCalled.assertCount(2);
    }

    private void setupForTrackTests() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();

        for(int i = 0; i < 1; ++i) {
            queuePlayResponse(httpHandler, new MockPlayResponse());
        }

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler));
    }

    @Test
    public void testSelectedSubtitleSetCorrectly() throws JSONException {
        setupForTrackTests();
        final Counter sessionChanged = new Counter();
        final Counter subtitleChanged = new Counter();
        final Counter trackSentToImpl = new Counter();
        final IPlayerImplementationListener[] playerImplementationListener = new IPlayerImplementationListener[]{null};
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                playerImplementationListener[0] = environment.getPlayerImplementationListener();
            }

            @Override
            public void setSubtitleTrack(ISubtitleTrack track, IPlayerImplementationControlResultHandler resultHandler) {
                trackSentToImpl.count();
                if("duck".equals(track.getLanguageCode())) {
                    resultHandler.onRejected(RejectReason.inapplicable("Test"));
                } else {
                    resultHandler.onDone();
                }
            }
        });
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                sessionChanged.count();
                to.addListener(new BasePlaybackSessionListener() {
                    @Override
                    public void onSelectedSubtitleTrackChanged(ISubtitleTrack oldselectedTrack, ISubtitleTrack newSelectedTrack) {
                        Assert.assertEquals("cow",newSelectedTrack.getLanguageCode());
                        subtitleChanged.count();
                    }
                });
            }
        });
        sessionChanged.assertNone();
        enigmaPlayer.play(new MockPlayRequest());
        sessionChanged.assertCount(1);
        trackSentToImpl.assertNone();
        subtitleChanged.assertNone();
        enigmaPlayer.getControls().setSubtitleTrack(new MockSubtitleTrack("cow").asSubtitleTrack());
        subtitleChanged.assertCount(1);
        trackSentToImpl.assertCount(1);
        final Counter controlRejected = new Counter();
        enigmaPlayer.getControls().setSubtitleTrack(new MockSubtitleTrack("duck").asSubtitleTrack(), new DefaultControlResultHandler("test") {
            @Override
            public void onRejected(IRejectReason reason) {
                controlRejected.count();
            }
        });
        controlRejected.assertCount(1);
        trackSentToImpl.assertCount(2);
        subtitleChanged.assertCount(1);
    }

    @Test
    public void testTracksReturnedCorrectly() throws JSONException {
        setupForTrackTests();
        final IPlayerImplementationListener[] playerImplementationListener = new IPlayerImplementationListener[]{null};
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                playerImplementationListener[0] = environment.getPlayerImplementationListener();
            }
        });
        final Counter sessionChanged = new Counter();
        final Counter gotCorrectSubtitles = new Counter();
        final Counter gotCorrectAudio = new Counter();
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                sessionChanged.count();
                to.addListener(new BasePlaybackSessionListener() {
                    @Override
                    public void onSubtitleTracks(List<ISubtitleTrack> tracks) {
                        Assert.assertEquals(4, tracks.size());
                        Assert.assertEquals("eng", tracks.get(0).getLanguageCode());
                        Assert.assertEquals("swe", tracks.get(1).getLanguageCode());
                        Assert.assertEquals("deu", tracks.get(2).getLanguageCode());
                        Assert.assertEquals("nor", tracks.get(3).getLanguageCode());

                        gotCorrectSubtitles.count();
                    }

                    @Override
                    public void onAudioTracks(List<IAudioTrack> tracks) {
                        Assert.assertEquals(3, tracks.size());
                        Assert.assertEquals("sv", tracks.get(0).getLanguageCode());
                        Assert.assertEquals("dk", tracks.get(1).getLanguageCode());
                        Assert.assertEquals("en", tracks.get(2).getLanguageCode());

                        gotCorrectAudio.count();
                    }
                });
            }
        });
        sessionChanged.assertNone();
        enigmaPlayer.play(new MockPlayRequest());
        sessionChanged.assertOnce();
        Collection<IPlayerImplementationTrack> newTracks = new ArrayList<>();
        newTracks.add(new MockSubtitleTrack("eng"));
        newTracks.add(new MockPlayerImplementationTrack());
        newTracks.add(new MockSubtitleTrack("swe"));
        newTracks.add(new MockSubtitleTrack("deu"));
        newTracks.add(new MockPlayerImplementationTrack());
        newTracks.add(new MockPlayerImplementationTrack());
        newTracks.add(new MockAudioTrack("sv"));
        newTracks.add(new MockPlayerImplementationTrack());
        newTracks.add(new MockSubtitleTrack("nor"));
        newTracks.add(new MockAudioTrack("dk"));
        newTracks.add(new MockPlayerImplementationTrack());
        newTracks.add(new MockPlayerImplementationTrack());
        newTracks.add(new MockAudioTrack("en"));
        newTracks.add(new MockPlayerImplementationTrack());
        gotCorrectSubtitles.assertNone();
        gotCorrectAudio.assertNone();
        playerImplementationListener[0].onTracksChanged(newTracks);
        gotCorrectSubtitles.assertOnce();
        gotCorrectAudio.assertOnce();
    }

    @Test
    public void testSelectedAudioSetCorrectly() throws JSONException {
        setupForTrackTests();
        final Counter sessionChanged = new Counter();
        final Counter audioChanged = new Counter();
        final Counter trackSentToImpl = new Counter();
        final IPlayerImplementationListener[] playerImplementationListener = new IPlayerImplementationListener[]{null};
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                playerImplementationListener[0] = environment.getPlayerImplementationListener();
            }

            @Override
            public void setAudioTrack(IAudioTrack track, IPlayerImplementationControlResultHandler resultHandler) {
                trackSentToImpl.count();
                if("sv".equals(track.getLanguageCode())) {
                    resultHandler.onRejected(RejectReason.illegal("UnitTest"));
                } else {
                    resultHandler.onDone();
                }
            }
        });
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                sessionChanged.count();
                to.addListener(new BasePlaybackSessionListener() {
                    @Override
                    public void onSelectedAudioTrackChanged(IAudioTrack oldSelectedTrack, IAudioTrack newSelectedTrack) {
                        Assert.assertEquals("de",newSelectedTrack.getLanguageCode());
                        audioChanged.count();
                    }
                });
            }
        });
        sessionChanged.assertNone();
        enigmaPlayer.play(new MockPlayRequest());
        sessionChanged.assertCount(1);
        trackSentToImpl.assertNone();
        audioChanged.assertNone();
        enigmaPlayer.getControls().setAudioTrack(new MockAudioTrack("de").asAudioTrack());
        audioChanged.assertCount(1);
        trackSentToImpl.assertCount(1);
        final Counter controlRejected = new Counter();
        enigmaPlayer.getControls().setAudioTrack(new MockAudioTrack("sv").asAudioTrack(), new DefaultControlResultHandler("test") {
            @Override
            public void onRejected(IRejectReason reason) {
                controlRejected.count();
            }
        });
        controlRejected.assertCount(1);
        trackSentToImpl.assertCount(2);
        audioChanged.assertCount(1);
    }

    @Test
    public void testPauseDisabledIfTimeshiftNotEnabled() throws JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        final JSONObject mock = new JSONObject();

        final Counter pauseInImplementationCalled = new Counter();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void pause(IPlayerImplementationControlResultHandler resultHandler) {
                pauseInImplementationCalled.count();
                super.pause(resultHandler);
            }
        }) {
            @Override
            protected IPlaybackSessionFactory newPlaybackSessionFactory(ITimeProvider timeProvider, IEpg epg) {
                return new MockPlaybackSessionFactory() {
                    @Override
                    public IInternalPlaybackSession newInternalPlaybackSession() {
                        MockInternalPlaybackSession internalPlaybackSession = new MockInternalPlaybackSession(true);
                        internalPlaybackSession.setContractRestrictions(EnigmaContractRestrictions.createWithDefaults(mock));
                        return internalPlaybackSession;
                    }
                };
            }
        };

        enigmaPlayer.play(new MockPlayRequest());
        pauseInImplementationCalled.assertNone();


        AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
        enigmaPlayer.getControls().pause(controlResultHandler);
        controlResultHandler.assertOnDoneCalled();
        pauseInImplementationCalled.assertOnce();

        mock.put("timeshiftEnabled", false);
        enigmaPlayer.play(new MockPlayRequest());
        pauseInImplementationCalled.assertCount(1);

        controlResultHandler = new AssertiveControlResultHandler() {
            @Override
            public void onRejected(IRejectReason reason) {
                Assert.assertEquals(RejectReasonType.CONTRACT_RESTRICTION_LIMITATION, reason.getType());
                super.onRejected(reason);
            }
        };
        enigmaPlayer.getControls().pause(controlResultHandler);
        controlResultHandler.assertOnRejectedCalled();
        pauseInImplementationCalled.assertCount(1);
    }

    @Test
    public void testNextProgram() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();
        List<IProgram> programs = new ArrayList<>();
        {
            programs.add(new MockProgram("program1", 0, 1000));
            programs.add(new MockProgram("program2", 1000, 2000));
            programs.add(new MockProgram("program3", 2000, 3000));
            //gap
            programs.add(new MockProgram("program4", 4000, 5000));
        }
        MockEpgResponse epgResponse = new MockEpgResponse(123L, 5000, programs);
        MockEpgLocator mockEpgLocator = new MockEpgLocator().setEpg(new MockEpg().setEpgResponse(epgResponse));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setEpgLocator(mockEpgLocator));
        MockPlayResponse playResponseMessage = new MockPlayResponse();
        playResponseMessage.streamInfoData.setToChannelLiveStream("channel0");
        playResponseMessage.streamInfoData.start = 123L;

        {
            //Verify the mock response will actually have streamPrograms
            StreamInfo streamInfo = new StreamInfo(playResponseMessage.streamInfoData.toJsonObject());
            Assert.assertTrue(streamInfo.hasStreamPrograms());
        }

        queuePlayResponse(httpHandler, playResponseMessage);

        final List<IPlayerImplementationControls.ISeekPosition> seekPositions = new ArrayList<>();

        class testNextProgram_MockPlayerImplementation extends MockPlayerImplementation {
            private ITimelinePositionFactory timelinePositionFactory;
            private long position = 0;
            private long startBound = 0;

            @Override
            public void seekTo(ISeekPosition seekPosition, IPlayerImplementationControlResultHandler resultHandler) {
                seekPositions.add(seekPosition);
                resultHandler.onDone();
            }

            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                this.timelinePositionFactory = environment.getTimelinePositionFactory();
            }

            @Override
            public ITimelinePosition getCurrentStartBound() {
                return timelinePositionFactory.newPosition(startBound);
            }

            @Override
            public ITimelinePosition getCurrentPosition() {
                return timelinePositionFactory.newPosition(position);
            }
        }

        testNextProgram_MockPlayerImplementation playerImplementation = new testNextProgram_MockPlayerImplementation();

        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), playerImplementation);

        enigmaPlayer.play(new PlayRequest(new MockPlayable("program1"), new BasePlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                Assert.fail(error.getTrace());
            }
        }));
        Assert.assertEquals(EnigmaPlayerState.PLAYING, enigmaPlayer.getState());
        int expectedSeeks = 1;
        Assert.assertEquals(expectedSeeks, seekPositions.size());

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().nextProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program2
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(1000-123, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 1000-123; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().nextProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program3
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(2000-123, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 2000-123; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().nextProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of the gap
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(3000-123, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 3000-123; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().nextProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program4
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(4000-123, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 4000-123; //Update position to seeked

        { //Seek! But this time we are at the end of the stream so we can't!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().nextProgram(controlResultHandler);
            controlResultHandler.assertOnRejectedCalled();
            Assert.assertEquals(expectedSeeks, seekPositions.size());
        }
    }

    @Test
    public void testPreviousProgram() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();
        List<IProgram> programs = new ArrayList<>();
        {
            programs.add(new MockProgram("program1", 0, 1000));
            programs.add(new MockProgram("program2", 1000, 2000));
            programs.add(new MockProgram("program3", 2000, 3000));
            //gap
            programs.add(new MockProgram("program4", 4000, 5000));
        }
        MockEpgResponse epgResponse = new MockEpgResponse(123L, 5000, programs);
        MockEpgLocator mockEpgLocator = new MockEpgLocator().setEpg(new MockEpg().setEpgResponse(epgResponse));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setEpgLocator(mockEpgLocator));
        MockPlayResponse playResponseMessage = new MockPlayResponse();
        playResponseMessage.streamInfoData.setToChannelLiveStream("channel0");
        playResponseMessage.streamInfoData.start = 123L;

        {
            //Verify the mock response will actually have streamPrograms
            StreamInfo streamInfo = new StreamInfo(playResponseMessage.streamInfoData.toJsonObject());
            Assert.assertTrue(streamInfo.hasStreamPrograms());
        }

        queuePlayResponse(httpHandler, playResponseMessage);

        final List<IPlayerImplementationControls.ISeekPosition> seekPositions = new ArrayList<>();

        class testPreviousProgram_MockPlayerImplementation extends MockPlayerImplementation {
            private ITimelinePositionFactory timelinePositionFactory;
            private long position = 0;
            private long startBound = 0;

            @Override
            public void seekTo(ISeekPosition seekPosition, IPlayerImplementationControlResultHandler resultHandler) {
                seekPositions.add(seekPosition);
                resultHandler.onDone();
            }

            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                this.timelinePositionFactory = environment.getTimelinePositionFactory();
            }

            @Override
            public ITimelinePosition getCurrentStartBound() {
                return timelinePositionFactory.newPosition(startBound);
            }

            @Override
            public ITimelinePosition getCurrentPosition() {
                return timelinePositionFactory.newPosition(position);
            }
        }

        testPreviousProgram_MockPlayerImplementation playerImplementation = new testPreviousProgram_MockPlayerImplementation();

        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), playerImplementation);

        enigmaPlayer.play(new PlayRequest(new MockPlayable("program1"), new BasePlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                Assert.fail(error.getTrace());
            }
        }));
        Assert.assertEquals(EnigmaPlayerState.PLAYING, enigmaPlayer.getState());
        int expectedSeeks = 1;
        Assert.assertEquals(expectedSeeks, seekPositions.size());

        //Start at program4
        playerImplementation.position = 4500-123;

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().previousProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of the gap
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(3000-123, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 3000-123; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().previousProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program3
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(2000-123, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 2000-123; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().previousProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program2
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(1000-123, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 1000-123; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().previousProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program1
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(0, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 0; //Update position to seeked
    }

    @Test
    public void testLiveDelay() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();
        MockPlayResponse mockPlayResponse = new MockPlayResponse();
        mockPlayResponse.formats.clear();
        mockPlayResponse.addFormat("https://media.example.com", "DASH", EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey(), 9483);
        mockPlayResponse.streamInfoData.live = true;
        queuePlayResponse(httpHandler, mockPlayResponse);
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler));

        final Counter loadCalled = new Counter();
        IPlayerImplementation playerImplementation = new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                environment.setMediaFormatSupportSpec(new IMediaFormatSupportSpec() {
                    @Override
                    public boolean supports(EnigmaMediaFormat enigmaMediaFormat) {
                        return true;
                    }
                });
            }

            @Override
            public void load(ILoadRequest loadRequest, IPlayerImplementationControlResultHandler resultHandler) {
                Duration liveDelay = loadRequest.getLiveDelay();
                Assert.assertNotNull(liveDelay);
                Assert.assertEquals(9483, liveDelay.inWholeUnits(Duration.Unit.MILLISECONDS));
                loadCalled.count();
            }
        };
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), playerImplementation);

        loadCalled.assertNone();
        enigmaPlayer.play(new PlayRequest(new MockPlayable("program1"), new BasePlayResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                Assert.fail(error.getTrace());
            }
        }));

        loadCalled.assertOnce();
    }


    @Test
    public void testBitrateChangedPropagated() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final Counter newInternalPlaybackSessionCalled = new Counter();
        final Counter setSelectedVideoTrackCalls = new Counter();
        MockPlayerImplementation playerImplementation = new MockPlayerImplementation();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), playerImplementation) {
            @Override
            protected IPlaybackSessionFactory newPlaybackSessionFactory(ITimeProvider timeProvider, IEpg epg) {
                return new MockPlaybackSessionFactory() {
                    @Override
                    public IInternalPlaybackSession newInternalPlaybackSession() {
                        newInternalPlaybackSessionCalled.count();
                        return new MockInternalPlaybackSession(true) {
                            @Override
                            public void setSelectedVideoTrack(IVideoTrack track) {
                                int counts = setSelectedVideoTrackCalls.getCounts();
                                if(counts == 0) {
                                    Assert.assertEquals(1524, track.getBitrate());
                                } else if(counts == 1) {
                                    Assert.assertEquals(8833800, track.getBitrate());
                                } else if(counts == 2) {
                                    Assert.assertEquals(100000, track.getBitrate());
                                }
                                setSelectedVideoTrackCalls.count();
                            }
                        };
                    }
                };
            }
        };

        newInternalPlaybackSessionCalled.assertExpected();
        enigmaPlayer.play(new MockPlayRequest());
        newInternalPlaybackSessionCalled.addToExpected(1);
        newInternalPlaybackSessionCalled.assertExpected();

        IPlayerImplementationListener playerImplementationListener = playerImplementation.getPlayerImplementationListener();
        playerImplementationListener.onVideoTrackSelectionChanged(new MockVideoTrack(1524));
        playerImplementationListener.onVideoTrackSelectionChanged(new MockVideoTrack(8833800));
        playerImplementationListener.onVideoTrackSelectionChanged(new MockVideoTrack(100000));

        setSelectedVideoTrackCalls.assertCount(3);
    }

    public static class EnigmaPlayerWithMockedTimeProvider extends EnigmaPlayer {
        public EnigmaPlayerWithMockedTimeProvider(ISession session, IPlayerImplementation playerImplementation) {
            super(session, playerImplementation);
        }

        @Override
        protected ITimeProvider newTimeProvider(ISession session) {
            return new MockTimeProvider();
        }

        @Override
        protected ITaskFactory getPlayTaskFactory() {
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
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void cancel(long joinMillis) throws TaskException {

                        }
                    };
                }
            };
        }
    }

    private static void queuePlayResponse(MockHttpHandler mockHttpHandler, MockPlayResponse playResponse) throws JSONException {
        JSONObject response = new JSONObject();
        JSONArray formatArray = new JSONArray();
        for(MockPlayResponse.MockFormat format : playResponse.formats) {
            formatArray.put(createFormatJson(format.mediaLocator, format.format, format.drmKey, format.liveDelay));
        }
        response.put("formats", formatArray);

        MockPlayResponse.MockStreamInfoData streamInfoData = playResponse.streamInfoData;
        if(streamInfoData != null) {
            response.put("streamInfo", streamInfoData.toJsonObject());
        }
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
    }

    private static class MockPlayResponse {
        public List<MockFormat> formats = new ArrayList<>(Arrays.asList(new MockFormat("https://media.example.com?format=HLS","HLS"), new MockFormat("https://media.example.com","DASH")));
        public MockStreamInfoData streamInfoData = new MockStreamInfoData();

        public void addFormat(String mediaLocator, String format) {
            formats.add(new MockFormat(mediaLocator, format));
        }

        public void addFormat(String mediaLocator, String format, String drmKey) {
            formats.add(new MockFormat(mediaLocator, format, drmKey));
        }

        public void addFormat(String mediaLocator, String format, String drmKey, long liveDelay) {
            formats.add(new MockFormat(mediaLocator, format, drmKey).setLiveDelay(liveDelay));
        }

        private static class MockFormat {
            public String mediaLocator;
            public String format;
            public String drmKey;
            public Long liveDelay = null;

            public MockFormat(String format) {
                this("https://media.example.com?format="+format, format);
            }

            public MockFormat(String mediaLocator, String format) {
                this(mediaLocator, format, null);
            }

            public MockFormat(String mediaLocator, String format, String drmKey) {
                this.mediaLocator = mediaLocator;
                this.format = format;
                this.drmKey = drmKey;
            }

            public MockFormat setLiveDelay(long liveDelay) {
                this.liveDelay = liveDelay;
                return this;
            }
        }

        private static class MockStreamInfoData {
            public Boolean live = null;
            public Long start = null;
            public String channelId = null;

            public void setToChannelLiveStream(String channelId) {
                this.live = true;
                this.start = 16136136163L;
                this.channelId = channelId;
            }

            public JSONObject toJsonObject() throws JSONException {
                JSONObject streamInfo = new JSONObject();
                if(this.live != null) {
                    streamInfo.put("live", this.live);
                }
                if(this.start != null) {
                    streamInfo.put("start", this.start);
                }
                if(this.channelId != null) {
                    streamInfo.put("channelId", this.channelId);
                }
                return streamInfo;
            }
        }
    }
}
