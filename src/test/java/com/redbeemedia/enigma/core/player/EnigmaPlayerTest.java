package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.audio.MockAudioTrack;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.EmptyResponseError;
import com.redbeemedia.enigma.core.error.Error;
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
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.player.track.MockPlayerImplementationTrack;
import com.redbeemedia.enigma.core.playrequest.MockPlayRequest;
import com.redbeemedia.enigma.core.playrequest.MockPlayResultHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.subtitle.MockSubtitleTrack;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.testutil.InstanceOfMatcher;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;
import com.redbeemedia.enigma.core.util.MockHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnigmaPlayerTest {
    @Test
    public void testPlayer() throws JSONException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        JSONObject response = new JSONObject();
        JSONArray formatArray = new JSONArray();
        formatArray.put(createFormatJson("https://media.example.com?format=HLS","HLS"));
        formatArray.put(createFormatJson("https://media.example.com","DASH"));
        response.put("formats", formatArray);
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
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
            public void load(String url, IPlayerImplementationControlResultHandler resultHandler) {
                loadCalled.setFlag();
                super.load(url, resultHandler);
            }

            @Override
            public void start(IPlayerImplementationControlResultHandler resultHandler) {
                startCalled.setFlag();
                super.start(resultHandler);
            }
        };
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), impl) {
            @Override
            protected ITimeProvider newTimeProvider(ISession session) {
                return new MockTimeProvider();
            }
        };
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
            public void onError(Error error) {
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

    private JSONObject createFormatJson(String mediaLocator, String format) throws JSONException {
        return createFormatJson(mediaLocator, format, null);
    }

    private JSONObject createFormatJson(String mediaLocator, String format, String drmKey) throws JSONException {
        JSONObject mediaFormat = new JSONObject();
        mediaFormat.put("mediaLocator", mediaLocator);
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
            JSONObject response = new JSONObject();
            JSONArray formatArray = new JSONArray();
            formatArray.put(createFormatJson("https://media.example.com?format=HLS", "HLS"));
            formatArray.put(createFormatJson("https://media.example.com", "DASH"));
            formatArray.put(createFormatJson("https://media.example.com", "DASH", EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey()));
            response.put("formats", formatArray);
            JSONObject streamInfo = new JSONObject();
            streamInfo.put("live", false);
            response.put("streamInfo", streamInfo);
            mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
        }
        {
            JSONObject response = new JSONObject();
            JSONArray formatArray = new JSONArray();
            formatArray.put(createFormatJson("https://media.example.com", "DASH", EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey()));
            formatArray.put(createFormatJson("https://media.example.com?format=HLS", "HLS"));
            response.put("formats", formatArray);
            JSONObject streamInfo = new JSONObject();
            streamInfo.put("live", true);
            streamInfo.put("start", 1505574300000L);
            response.put("streamInfo", streamInfo);
            mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
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
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), playerImpl) {
            @Override
            protected ITimeProvider newTimeProvider(ISession session) {
                return new MockTimeProvider();
            }
        };
        installed.assertSet();
        enigmaPlayer.play(new MockPlayRequest("123").setResultHandler(new MockPlayResultHandler() {
            @Override
            public void onError(Error error) {
                error.printStackTrace();
                Assert.fail(error.getClass().getSimpleName()+": "+error.getErrorCode());
            }
        }));
        playbackStartedCalls.assertOnce();
        final Error[] errorGotten = new Error[]{null};
        enigmaPlayer.play(new MockPlayRequest("7s4s4ts").setResultHandler(new MockPlayResultHandler() {
            @Override
            public void onError(Error error) {
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
            public void onPlaybackError(Error error) {
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
            public void onPlaybackError(Error error) {
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
        JSONObject response = new JSONObject();
        {
            JSONArray formatArray = new JSONArray();
            formatArray.put(createFormatJson("https://media.example.com", "DASH"));
            response.put("formats", formatArray);
            JSONObject streamInfo = new JSONObject();
            streamInfo.put("live", true);
            streamInfo.put("start", 1505574300000L);
            response.put("streamInfo", streamInfo);
        }
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                environment.setMediaFormatSupportSpec(enigmaMediaFormat -> true);
            }
        }) {
            @Override
            protected ITimeProvider newTimeProvider(ISession session) {
                return new MockTimeProvider();
            }
        };

        final StringBuilder log = new StringBuilder();
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
                log.append("["+from+"->"+to+"]");
            }
        });
        MockPlayRequest mockPlayRequest = new MockPlayRequest();
        enigmaPlayer.play(mockPlayRequest);
        Assert.assertEquals("[IDLE->LOADING][LOADING->LOADED][LOADED->PLAYING][PLAYING->LOADED]",log.toString());
    }

    @Test
    public void testOnReadyListenersSimple() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        final Counter onReadyCalled = new Counter();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                super.install(environment);
                environment.addEnigmaPlayerReadyListener(enigmaPlayer1 -> onReadyCalled.count());
            }
        }) {
            @Override
            protected ITimeProvider newTimeProvider(ISession session) {
                return new MockTimeProvider();
            }
        };
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

        JSONObject response = new JSONObject();
        JSONArray formatArray = new JSONArray();
        formatArray.put(createFormatJson("https://media.example.com","DASH"));
        response.put("formats", formatArray);
        for(int i = 0; i < 3; ++i) {
            httpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
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

        JSONObject response = new JSONObject();
        JSONArray formatArray = new JSONArray();
        formatArray.put(createFormatJson("https://media.example.com","DASH"));
        response.put("formats", formatArray);
        for(int i = 0; i < 1; ++i) {
            httpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
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

    private static class EnigmaPlayerWithMockedTimeProvider extends EnigmaPlayer {
        public EnigmaPlayerWithMockedTimeProvider(ISession session, IPlayerImplementation playerImplementation) {
            super(session, playerImplementation);
        }

        @Override
        protected ITimeProvider newTimeProvider(ISession session) {
            return new MockTimeProvider();
        }
    }
}
