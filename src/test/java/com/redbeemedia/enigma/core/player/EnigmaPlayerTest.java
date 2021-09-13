package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.analytics.AnalyticsPlayResponseData;
import com.redbeemedia.enigma.core.analytics.MockAnalyticsReporter;
import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.audio.MockAudioTrack;
import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
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
import com.redbeemedia.enigma.core.lifecycle.BaseLifecycleListener;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playable.MockPlayable;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.controls.AssertiveControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.player.track.MockPlayerImplementationTrack;
import com.redbeemedia.enigma.core.playrequest.BasePlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.playrequest.MockPlayRequest;
import com.redbeemedia.enigma.core.playrequest.MockPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.PlayRequest;
import com.redbeemedia.enigma.core.playrequest.PlaybackProperties;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.session.Session;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.subtitle.MockSubtitleTrack;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.MainThreadTaskFactory;
import com.redbeemedia.enigma.core.task.MockTaskFactoryProvider;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.task.TestTaskFactory;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.testutil.InstanceOfMatcher;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.MockHandler;
import com.redbeemedia.enigma.core.video.ISpriteRepository;
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
import java.util.regex.Pattern;

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
        EnigmaMediaFormat DASH_UNENC = EnigmaMediaFormat.DASH().unenc();
        EnigmaMediaFormat DASH_WIDEVINE = EnigmaMediaFormat.DASH().widevine();
        EnigmaMediaFormat HLS_FAIRPLAY = EnigmaMediaFormat.HLS().fairplay();

        JSONObject dashUnencMediaFormat = new JSONObject();
        dashUnencMediaFormat.put("format", "DASH");
        Assert.assertEquals(DASH_UNENC, EnigmaMediaFormat.parseMediaFormat(dashUnencMediaFormat));

        JSONObject dashEncMediaFormat = new JSONObject();
        dashEncMediaFormat.put("format", "DASH");
        {
            JSONObject drm = new JSONObject();
            drm.put("com.widevine.alpha", new JSONObject());
            dashEncMediaFormat.put("drm", drm);
        }
        Assert.assertEquals(DASH_WIDEVINE, EnigmaMediaFormat.parseMediaFormat(dashEncMediaFormat));

        JSONObject hlsEncMediaFormat = new JSONObject();
        hlsEncMediaFormat.put("format", "HLS");
        {
            JSONObject drm = new JSONObject();
            drm.put("com.apple.fairplay", new JSONObject());
            hlsEncMediaFormat.put("drm", drm);
        }
        Assert.assertEquals(HLS_FAIRPLAY, EnigmaMediaFormat.parseMediaFormat(hlsEncMediaFormat));
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
                    private final EnigmaMediaFormat DASH_UNENC = EnigmaMediaFormat.DASH().unenc();
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
            protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayer.EnigmaPlayerLifecycle lifecycle) {
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
            protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayer.EnigmaPlayerLifecycle lifecycle) {
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
                protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayer.EnigmaPlayerLifecycle lifecycle) {
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
            protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler,ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection, ISpriteRepository spriteRepository) {
                return new MockPlaybackStartAction(playRequest, playerConnection) {
                    @Override
                    protected IInternalPlaybackSession newInternalPlaybackSession() {
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
        MockEpgResponse epgResponse = new MockEpgResponse(0L, 5000, programs);
        MockEpgLocator mockEpgLocator = new MockEpgLocator().setEpg(new MockEpg().setEpgResponse(epgResponse));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setEpgLocator(mockEpgLocator));
        MockPlayResponse playResponseMessage = new MockPlayResponse();
        playResponseMessage.streamInfoData.setToChannelLiveStream("channel0");
        playResponseMessage.streamInfoData.start = 0L;

        {
            //Verify the mock response will actually have streamPrograms
            JsonStreamInfo streamInfo = new JsonStreamInfo(playResponseMessage.streamInfoData.toJsonObject());
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
        Assert.assertEquals(1000, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 1000; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().nextProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program3
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(2000, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 2000; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().nextProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of the gap
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(3000, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 3000; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().nextProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program4
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(4000, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 4000; //Update position to seeked

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
            programs.add(new MockProgram("program1", 0, 10000));
            programs.add(new MockProgram("program2", 10000, 20000));
            programs.add(new MockProgram("program3", 20000, 30000));
            //gap
            programs.add(new MockProgram("program4", 40000, 50000));
        }
        MockEpgResponse epgResponse = new MockEpgResponse(1L, 50000, programs);
        MockEpgLocator mockEpgLocator = new MockEpgLocator().setEpg(new MockEpg().setEpgResponse(epgResponse));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setEpgLocator(mockEpgLocator));
        MockPlayResponse playResponseMessage = new MockPlayResponse();
        playResponseMessage.streamInfoData.setToChannelLiveStream("channel0");
        playResponseMessage.streamInfoData.start = 1L;

        {
            //Verify the mock response will actually have streamPrograms
            JsonStreamInfo streamInfo = new JsonStreamInfo(playResponseMessage.streamInfoData.toJsonObject());
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
        playerImplementation.position = 45000-1;

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().previousProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of the gap
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(30000-1, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 30000-1; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().previousProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program3
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(20000-1, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 20000-1; //Update position to seeked

        { //Seek!
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().previousProgram(controlResultHandler);
            controlResultHandler.assertOnDoneCalled();
            expectedSeeks++;
        }

        //assert we are at start of program2
        Assert.assertEquals(expectedSeeks, seekPositions.size());
        Assert.assertEquals(10000-1, ((IPlayerImplementationControls.TimelineRelativePosition) seekPositions.get(expectedSeeks-1)).getMillis());
        playerImplementation.position = 10000-1; //Update position to seeked

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
            protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler, ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection, ISpriteRepository spriteRepository) {
                return new MockPlaybackStartAction(playRequest, playerConnection) {
                    @Override
                    protected IInternalPlaybackSession newInternalPlaybackSession() {
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

    @Test
    public void testMediaFormatPreferences() throws JSONException {
        JSONObject playResponse = new JSONObject();
        playResponse.put("requestId", "mockRequest");
        playResponse.put("playToken", "mockToken");
        playResponse.put("formats", new JSONArray(Arrays.asList(
                createFormatJson("http://example.com/dash-unenc-manifest", "DASH", null, null),
                createFormatJson("http://example.com/dash-widewine-manifest", "DASH", EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey(), null),
                createFormatJson("http://example.com/hls-unenc-manifest", "HLS", null, null),
                createFormatJson("http://example.com/hls-fairplay-manifest", "HLS", EnigmaMediaFormat.DrmTechnology.FAIRPLAY.getKey(), null),
                createFormatJson("http://example.com/unknown-manifest", "UNKNOWN", EnigmaMediaFormat.DrmTechnology.PLAYREADY.getKey(), null)
        )));

        MockHttpHandler httpHandler = new MockHttpHandler();
        Pattern urlPattern = Pattern.compile(".*entitlement/.*/play.*");
        httpHandler.queueResponse(urlPattern, new HttpStatus(200, "OK"), playResponse.toString());
        httpHandler.queueResponse(urlPattern, new HttpStatus(200, "OK"), playResponse.toString());
        httpHandler.queueResponse(urlPattern, new HttpStatus(200, "OK"), playResponse.toString());

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler));

        final Counter loadCalled = new Counter();
        final String[] lastLoadedUrl = new String[]{null};

        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                environment.setMediaFormatSupportSpec(enigmaMediaFormat -> true); //Support all formats
            }

            @Override
            public void load(ILoadRequest loadRequest, IPlayerImplementationControlResultHandler resultHandler) {
                lastLoadedUrl[0] = ((IStreamLoadRequest) loadRequest).getUrl();
                loadCalled.count();
                super.load(loadRequest, resultHandler);
            }
        });

        loadCalled.assertExpected();

        enigmaPlayer.play(new MockPlayRequest("mockAsset"));
        loadCalled.addToExpected(1);

        loadCalled.assertExpected();
        Assert.assertEquals(lastLoadedUrl[0], "http://example.com/dash-widewine-manifest"); //Default

        enigmaPlayer.setMediaFormatPreference(
                EnigmaMediaFormat.HLS().widevine(),
                EnigmaMediaFormat.HLS().unenc());

        enigmaPlayer.play(new MockPlayRequest("mockMock"));
        loadCalled.addToExpected(1);

        loadCalled.assertExpected();
        Assert.assertEquals(lastLoadedUrl[0], "http://example.com/hls-unenc-manifest");

        IPlaybackProperties playbackProperties = new PlaybackProperties()
                .setMediaFormatPreference(
                        EnigmaMediaFormat.DASH().playready(),
                        EnigmaMediaFormat.DASH().fairplay(),
                        EnigmaMediaFormat.HLS().fairplay(),
                        EnigmaMediaFormat.DASH().widevine(),
                        EnigmaMediaFormat.DASH().unenc()
                );
        enigmaPlayer.play(new MockPlayRequest("fakeAsset").setPlaybackProperties(playbackProperties));
        loadCalled.addToExpected(1);

        loadCalled.assertExpected();
        Assert.assertEquals(lastLoadedUrl[0], "http://example.com/hls-fairplay-manifest");
    }

    @Test
    public void testDefaultSession() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        class TestSession extends Session {
            private final IBusinessUnit businessUnit;
            public TestSession(String sessionToken, IBusinessUnit businessUnit) {
                super(sessionToken, businessUnit);
                this.businessUnit = businessUnit;
            }

            @Override
            public IBusinessUnit getBusinessUnit() {
                return businessUnit;
            }

            @Override
            public String toString() {
                return getSessionToken()+" "+businessUnit.getCustomerName()+"/"+businessUnit.getName();
            }
        }

        final Counter newPlaybackStartActionCalled = new Counter();

        IBusinessUnit businessUnit = new BusinessUnit("JUnit", "EnigmaPlayerTest");
        IBusinessUnit updatedBusinessUnit = new BusinessUnit("New", "AndImproved");
        TestSession initialSession = new TestSession("initial", businessUnit);
        TestSession playRequestSession = new TestSession("forPlay", new BusinessUnit("X","Y"));
        TestSession updatedSession = new TestSession("updated", updatedBusinessUnit);

        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(initialSession, new MockPlayerImplementation()) {
            @Override
            protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler ,ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection, ISpriteRepository spriteRepository) {
                if(newPlaybackStartActionCalled.getCounts() == 0) {
                    Assert.assertEquals(initialSession, session);
                    Assert.assertEquals(initialSession.getBusinessUnit(), businessUnit);
                    Assert.assertNull(playRequest.getSession());
                } else if(newPlaybackStartActionCalled.getCounts() == 1) {
                    Assert.assertEquals(initialSession, session);
                    Assert.assertEquals(initialSession.getBusinessUnit(), businessUnit);
                    Assert.assertEquals(playRequestSession, playRequest.getSession());
                } else if(newPlaybackStartActionCalled.getCounts() == 2) {
                    Assert.assertEquals(initialSession, session);
                    Assert.assertEquals(initialSession.getBusinessUnit(), businessUnit);
                    Assert.assertNull(playRequest.getSession());
                } else if(newPlaybackStartActionCalled.getCounts() == 3) {
                    Assert.assertEquals(updatedSession, session);
                    Assert.assertEquals(updatedSession.getBusinessUnit(), businessUnit);
                    Assert.assertNull(playRequest.getSession());
                } else if(newPlaybackStartActionCalled.getCounts() == 4) {
                    Assert.assertNull(session);
                    Assert.assertEquals(updatedSession.getBusinessUnit(), businessUnit);
                    Assert.assertNull(playRequest.getSession());
                } else if(newPlaybackStartActionCalled.getCounts() == 5) {
                    Assert.assertNull(session);
                    Assert.assertEquals(updatedSession.getBusinessUnit(), businessUnit);
                    Assert.assertEquals(playRequestSession, playRequest.getSession());
                }

                newPlaybackStartActionCalled.count();
                return new MockPlaybackStartAction(playRequest, playerConnection);
            }
        };

        newPlaybackStartActionCalled.assertNone();

        enigmaPlayer.play(new MockPlayRequest()); //Call 0

        newPlaybackStartActionCalled.assertOnce();

        enigmaPlayer.play(new MockPlayRequest().setSession(playRequestSession)); //Call 1

        newPlaybackStartActionCalled.assertCount(2);

        enigmaPlayer.play(new MockPlayRequest()); //Call 2

        newPlaybackStartActionCalled.assertCount(3);

        enigmaPlayer.setDefaultSession(updatedSession);
        enigmaPlayer.play(new MockPlayRequest()); //Call 3

        newPlaybackStartActionCalled.assertCount(4);

        enigmaPlayer.setDefaultSession(null);
        enigmaPlayer.play(new MockPlayRequest()); //Call 4

        newPlaybackStartActionCalled.assertCount(5);

        enigmaPlayer.play(new MockPlayRequest().setSession(playRequestSession)); //Call 5

        newPlaybackStartActionCalled.assertCount(6);
    }

    @Test
    public void testReleaseIdempotent() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final Counter onStartCalled = new Counter();
        final Counter onStopCalled = new Counter();

        IBusinessUnit businessUnit = new BusinessUnit("Test", "ReleaseTest");
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(businessUnit, new MockPlayerImplementation()) {
            @Override
            protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayerLifecycle lifecycle) {
                lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                    @Override
                    public void onStart(Void aVoid) {
                        onStartCalled.count();
                    }

                    @Override
                    public void onStop(Void aVoid) {
                        onStopCalled.count();
                    }
                });
                return super.newTimeProvider(businessUnit, lifecycle);
            }
        };

        onStartCalled.assertOnce();
        onStopCalled.assertNone();

        enigmaPlayer.release();

        onStartCalled.assertOnce();
        onStopCalled.assertOnce();

        enigmaPlayer.release();

        onStartCalled.assertOnce();
        onStopCalled.assertOnce();
    }

    @Test
    public void testReleaseStepsExceptionInPlayerImplementation() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final Counter onStopCalled = new Counter();

        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(
                new BusinessUnit("DHD", "UA4"),
                new MockPlayerImplementation() {
                    @Override
                    public void release() {
                        throw new RuntimeException("PlayerImplementation");
                    }
                }
        ) {
            @Override
            protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayerLifecycle lifecycle) {
                lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                    @Override
                    public void onStart(Void aVoid) {
                    }

                    @Override
                    public void onStop(Void aVoid) {
                        onStopCalled.count();
                    }
                });
                return super.newTimeProvider(businessUnit, lifecycle);
            }
        };

        onStopCalled.assertNone();

        try {
            enigmaPlayer.release();
            Assert.fail("Expected exception");
        } catch (Exception e) {
            Assert.assertEquals(RuntimeException.class, e.getClass());
            Assert.assertEquals(0, e.getSuppressed() != null ? e.getSuppressed().length : 0);
            Assert.assertEquals("PlayerImplementation", e.getMessage());
        }

        onStopCalled.assertOnce();
    }

    @Test
    public void testReleaseStepsExceptionInLifecycleListener() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final Counter onStopCalled = new Counter();

        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(
                new BusinessUnit("DHD", "UA4"),
                new MockPlayerImplementation()
        ) {
            @Override
            protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayerLifecycle lifecycle) {
                lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                    @Override
                    public void onStart(Void aVoid) {
                    }

                    @Override
                    public void onStop(Void aVoid) {
                        onStopCalled.count();
                    }
                });
                lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                    @Override
                    public void onStart(Void aVoid) {
                    }

                    @Override
                    public void onStop(Void aVoid) {
                        throw new RuntimeException("Listener");
                    }
                });
                lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                    @Override
                    public void onStart(Void aVoid) {
                    }

                    @Override
                    public void onStop(Void aVoid) {
                        onStopCalled.count();
                    }
                });
                return super.newTimeProvider(businessUnit, lifecycle);
            }
        };

        onStopCalled.assertNone();

        try {
            enigmaPlayer.release();
            Assert.fail("Expected exception");
        } catch (Exception e) {
            Assert.assertEquals(RuntimeException.class, e.getClass());
            Assert.assertEquals(0, e.getSuppressed() != null ? e.getSuppressed().length : 0);
            Assert.assertEquals("Listener", e.getMessage());
        }

        onStopCalled.assertCount(2);
    }

    @Test
    public void testReleaseStepsMultipleExceptions() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final Counter onStopCalled = new Counter();

        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(
                new BusinessUnit("DHD", "UA4"),
                new MockPlayerImplementation() {
                    @Override
                    public void release() {
                        throw new RuntimeException("PlayerImplementation");
                    }
                }
        ) {
            @Override
            protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayerLifecycle lifecycle) {
                lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                    @Override
                    public void onStart(Void aVoid) {
                    }

                    @Override
                    public void onStop(Void aVoid) {
                        onStopCalled.count();
                        throw new RuntimeException("Listener");
                    }
                });
                lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                    @Override
                    public void onStart(Void aVoid) {
                    }

                    @Override
                    public void onStop(Void aVoid) {
                        onStopCalled.count();
                        throw new RuntimeException("ListenerTwo");
                    }
                });
                lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                    @Override
                    public void onStart(Void aVoid) {
                    }

                    @Override
                    public void onStop(Void aVoid) {
                        onStopCalled.count();
                    }
                });
                return super.newTimeProvider(businessUnit, lifecycle);
            }
        };

        onStopCalled.assertNone();

        try {
            enigmaPlayer.release();
            Assert.fail("Expected exception");
        } catch (Exception e) {
            Collection<Throwable> allExceptions = getAllExceptions(e, new ArrayList<>());
            Counter playerImplementationException = new Counter();
            Counter listenerException = new Counter();
            Counter listenerTwoException = new Counter();
            for(Throwable throwable : allExceptions) {
                if("PlayerImplementation".equals(throwable.getMessage())) {
                    playerImplementationException.count();
                }
                if("Listener".equals(throwable.getMessage())) {
                    listenerException.count();
                }
                if("ListenerTwo".equals(throwable.getMessage())) {
                    listenerTwoException.count();
                }
            }

            playerImplementationException.assertOnce();
            listenerException.assertOnce();
            listenerTwoException.assertOnce();
        }

        onStopCalled.assertCount(3);

        enigmaPlayer.release(); //Should have no effect

        onStopCalled.assertCount(3);
    }

    @Test
    public void testTimelineRepeaterShutOff() {
        TestTaskFactory testTaskFactory = new TestTaskFactory(33);
        ITaskFactoryProvider taskFactoryProvider = new MockTaskFactoryProvider() {
            @Override
            public ITaskFactory getMainThreadTaskFactory() {
                return testTaskFactory;
            }

            @Override
            public ITaskFactory getTaskFactory() {
                return testTaskFactory;
            }
        };;

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setTaskFactoryProvider(taskFactoryProvider));

        final Counter getCurrentPositionCalled = new Counter();

        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new BusinessUnit("x6x", "sy4dhH"), new MockPlayerImplementation() {
            @Override
            public ITimelinePosition getCurrentPosition() {
                getCurrentPositionCalled.count();
                return super.getCurrentPosition();
            }
        });

        getCurrentPositionCalled.assertNone();

        enigmaPlayer.play(new MockPlayRequest().setPlayable(new MockPlayable().useDownloadData(new Object())));
        testTaskFactory.letTimePass(100);
        int count = getCurrentPositionCalled.getCounts();
        Assert.assertTrue(count > 0);

        testTaskFactory.letTimePass(1000);

        Assert.assertTrue(getCurrentPositionCalled.getCounts() > count);


        enigmaPlayer.release();

        count = getCurrentPositionCalled.getCounts();

        testTaskFactory.letTimePass(2000);

        getCurrentPositionCalled.assertCount(count);
    }

    @Test
    public void testCallbackHandlerUsedForPlayCall() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation());

        final Flag callbackHandlerPostCalled = new Flag();
        enigmaPlayer.setCallbackHandler(new MockHandler() {
            @Override
            public boolean post(Runnable runnable) {
                callbackHandlerPostCalled.setFlag();
                runnable.run();
                return true;
            }
        });

        callbackHandlerPostCalled.assertNotSet();

        enigmaPlayer.play(new MockPlayRequest().setPlayable(new MockPlayable().useUrl("http://example.com/fakey.mpdz")));

        callbackHandlerPostCalled.assertSet("Callback handler not used!");
    }

    @Test
    public void testLiveScrubCapping() {

        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        MockPlayerImplementation implementation = new MockPlayerImplementation() {

            @Override
            public void seekTo(ISeekPosition seekPosition, IPlayerImplementationControlResultHandler resultHandler) {
                // Set the currentPosition to whatever we get
                currentPosition = ((IPlayerImplementationControls.TimelineRelativePosition)seekPosition).getMillis();
                resultHandler.onDone();
            }
        };

        implementation.isLiveStream = true;
        EnigmaPlayer enigmaPlayer = EnigmaPlayerTest.EnigmaPlayerWithMockedTimeProvider.EnigmaPlayerWithMockedStartSession(new MockSession(), implementation, true);
        MockPlayRequest playRequest = new MockPlayRequest();
        playRequest.setPlayable(new MockPlayable("MockPlayable"));

        enigmaPlayer.play(playRequest);

        // Current position should be before the live position
        Assert.assertTrue(implementation.getCurrentPosition().before(implementation.getLivePosition()));
        // Fetch the mocked live position
        ITimelinePosition livePosition = implementation.getLivePosition();
        // Scrub to a point after live position
        enigmaPlayer.getControls().seekTo(implementation.livePosition + 100L);
        // The current position should be set to "live position"
        Assert.assertEquals(livePosition, implementation.getCurrentPosition());
        // Scrub to a valid point
        enigmaPlayer.getControls().seekTo(implementation.livePosition - 100L);
        // The current position should be set to the requested position
        Assert.assertEquals(implementation.getCurrentPosition(), livePosition.subtract(Duration.millis(100L)));
        // Make the component behave like a VOD
        implementation.isLiveStream = false;
        // Scrub to a point after live position
        enigmaPlayer.getControls().seekTo(implementation.livePosition + 100L);
        // Since it's no longer a live stream, scrubbing should be allowed. Confirm the position
        Assert.assertEquals(livePosition.add(Duration.millis(100L)), implementation.getCurrentPosition());
    }

    @Test
    public void testSetMaxVideoDimensions() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        final Counter setMaxVideoTrackDimensionsCalledOnImpl = new Counter();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void setMaxVideoTrackDimensions(int width, int height, IPlayerImplementationControlResultHandler controlResultHandler) {
                setMaxVideoTrackDimensionsCalledOnImpl.count();
                int counts = setMaxVideoTrackDimensionsCalledOnImpl.getCounts();
                if(counts == 1) {
                    Assert.assertEquals(100, width);
                    Assert.assertEquals(200, height);
                    controlResultHandler.onDone();
                } else if(counts == 2) {
                    controlResultHandler.onError(new UnexpectedError(new RuntimeException("Oh no!")));
                } else if(counts == 3) {
                    throw new RuntimeException("Uncaught");
                } else if(counts == 4) {
                    Assert.assertEquals(1512, width);
                    Assert.assertEquals(723, height);
                    controlResultHandler.onDone();
                } else {
                    Assert.fail("setMaxVideoTrackDimensions called more than expected ("+counts+" times)");
                }
            }
        });

        { //Test with success
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().setMaxVideoTrackDimensions(100, 200, controlResultHandler);
            setMaxVideoTrackDimensionsCalledOnImpl.addToExpected(1);

            controlResultHandler.assertOnDoneCalled();
            setMaxVideoTrackDimensionsCalledOnImpl.assertExpected();
        }

        { //Test with error
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().setMaxVideoTrackDimensions(100, 200, controlResultHandler);
            setMaxVideoTrackDimensionsCalledOnImpl.addToExpected(1);

            controlResultHandler.assertOnErrorCalled();
            setMaxVideoTrackDimensionsCalledOnImpl.assertExpected();
        }

        { //Test with uncaught error
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            enigmaPlayer.getControls().setMaxVideoTrackDimensions(100, 200, controlResultHandler);
            setMaxVideoTrackDimensionsCalledOnImpl.addToExpected(1);

            controlResultHandler.assertOnErrorCalled();
            setMaxVideoTrackDimensionsCalledOnImpl.assertExpected();
        }

        { //Test with video track
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler();
            MockVideoTrack videoTrack = new MockVideoTrack().setDimensions(1512, 723);
            enigmaPlayer.getControls().setMaxVideoTrackDimensions(videoTrack, controlResultHandler);
            setMaxVideoTrackDimensionsCalledOnImpl.addToExpected(1);

            controlResultHandler.assertOnDoneCalled();
            setMaxVideoTrackDimensionsCalledOnImpl.assertExpected();
        }

        { //Test with null video track
            AssertiveControlResultHandler controlResultHandler = new AssertiveControlResultHandler() {
                @Override
                public void onRejected(IRejectReason reason) {
                    Assert.assertEquals(IControlResultHandler.RejectReasonType.ILLEGAL_ARGUMENT, reason.getType());
                    super.onRejected(reason);
                }
            };
            enigmaPlayer.getControls().setMaxVideoTrackDimensions(null, controlResultHandler);

            controlResultHandler.assertOnRejectedCalled();
            setMaxVideoTrackDimensionsCalledOnImpl.assertExpected();
        }
    }

    private static Collection<Throwable> getAllExceptions(Throwable e, Collection<Throwable> result) {
        result.add(e);
        Throwable cause = e.getCause();
        if(cause != null) {
            getAllExceptions(cause, result);
        }
        Throwable[] suppressed = e.getSuppressed();
        if(suppressed != null) {
            for(Throwable supr : suppressed) {
                getAllExceptions(supr, result);
            }
        }
        return result;
    }

    public static class EnigmaPlayerWithMockedTimeProvider extends EnigmaPlayer {

        public static EnigmaPlayer EnigmaPlayerWithMockedStartSession(ISession session, IPlayerImplementation playerImplementation, Boolean isLive) {

            return new EnigmaPlayerTest.EnigmaPlayerWithMockedTimeProvider(session, playerImplementation) {
                @Override
                protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler, ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection, ISpriteRepository spriteRepository) {
                    return new MockPlaybackStartAction(playRequest, playerConnection) {
                        @Override
                        protected IInternalPlaybackSession newInternalPlaybackSession() {

                            return new MockInternalPlaybackSession(isLive);

                        }
                    };
                }
            };
        }

        public EnigmaPlayerWithMockedTimeProvider(ISession session, IPlayerImplementation playerImplementation) {
            super(session, playerImplementation);
        }

        @Override
        protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayer.EnigmaPlayerLifecycle lifecycle) {
            return new MockTimeProvider();
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
                    return new MainThreadTaskFactory();
                }
            };
        }

        private ITaskFactory getMockTaskFactory() {
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

        @Override
        protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler, ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection, ISpriteRepository videoSpriteRepository) {
            return new DefaultPlaybackStartAction(session, businessUnit, timeProvider, playRequest, callbackHandler, taskFactoryProvider, playerImplementationControls, playerConnection, videoSpriteRepository) {
                @Override
                protected Analytics createAnalytics(ISession session, String playbackSessionId, ITimeProvider timeProvider, ITaskFactory taskFactory, AnalyticsPlayResponseData analyticsPlayResponseData) {
                    return new Analytics(new MockAnalyticsReporter(), new MockInternalPlaybackSessionListener());
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