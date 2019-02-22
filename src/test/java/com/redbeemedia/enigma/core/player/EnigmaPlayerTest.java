package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.PlayRequest;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.testutil.InstanceOfMatcher;
import com.redbeemedia.enigma.core.util.MockHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

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

        final Flag startPlaybackCalled = new Flag();
        final Flag onErrorCalled = new Flag();
        final Flag useWithCalled = new Flag();
        final Counter installCalled = new Counter();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                installCalled.count();
            }

            @Override
            public void startPlayback(String url) {
                startPlaybackCalled.setFlag();
            }
        });
        Assert.assertFalse(startPlaybackCalled.isTrue());
        installCalled.assertCount(1);
        enigmaPlayer.play(new IPlayRequest() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onError(Error error) {
                onErrorCalled.setFlag();
                throw new RuntimeException("Error:"+error.getErrorCode());
            }

            @Override
            public IPlayable getPlayable() {
                return new MockPlayable("123") {
                    @Override
                    public void useWith(IPlayableHandler playableHandler) {
                        useWithCalled.setFlag();
                        super.useWith(playableHandler);
                    }
                };
            }
        });
        Assert.assertTrue(useWithCalled.isTrue());
        Assert.assertFalse(onErrorCalled.isTrue());
        Assert.assertTrue(startPlaybackCalled.isTrue());
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
            mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
        }
        {
            JSONObject response = new JSONObject();
            JSONArray formatArray = new JSONArray();
            formatArray.put(createFormatJson("https://media.example.com", "DASH", EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey()));
            formatArray.put(createFormatJson("https://media.example.com?format=HLS", "HLS"));
            response.put("formats", formatArray);
            mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
        }
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));
        final Flag installed = new Flag();
        final Counter playbackStartedCalls = new Counter();
        IPlayerImplementation playerImpl = new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
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
            public void startPlayback(String url) {
                playbackStartedCalls.count();
            }
        };
        installed.assertNotSet();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), playerImpl);
        installed.assertSet();
        enigmaPlayer.play(new PlayRequest(new MockPlayable("123")) {
            @Override
            public void onStarted() {
            }

            @Override
            public void onError(Error error) {
                Assert.fail("Error: "+error.getErrorCode());
            }
        });
        playbackStartedCalls.assertCount(1);
        final Error[] errorGotten = new Error[]{null};
        enigmaPlayer.play(new PlayRequest(new MockPlayable("7s4s4ts")) {
            @Override
            public void onStarted() {
            }

            @Override
            public void onError(Error error) {
                errorGotten[0] = error;
            }
        });
        playbackStartedCalls.assertCount(1);
        Assert.assertNotEquals(null, errorGotten[0]);
        Assert.assertThat(errorGotten[0], new InstanceOfMatcher<>(NoSupportedMediaFormatsError.class));
    }

    @Test
    public void testListeners() {
        final IPlayerImplementationListener[] playerImplementationListener = new IPlayerImplementationListener[]{null};
        final Flag installCalled = new Flag();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                playerImplementationListener[0] = environment.getPlayerImplementationListener();
                installCalled.setFlag();
            }
        });
        installCalled.assertSet();
        Assert.assertNotEquals(null, playerImplementationListener[0]);
        Counter onPlaybackErrorCalled = new Counter();
        BaseEnigmaPlayerListener listener = new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackError(Error error) {
                onPlaybackErrorCalled.count();
            }
        };
        Assert.assertTrue(enigmaPlayer.addListener(listener));
        onPlaybackErrorCalled.assertNone();
        playerImplementationListener[0].onError(new UnexpectedError("TEST!"));
        onPlaybackErrorCalled.assertCount(1);
        playerImplementationListener[0].onError(new UnexpectedError("TEST 2!"));
        onPlaybackErrorCalled.assertCount(2);
        Assert.assertTrue(enigmaPlayer.removeListener(listener));
        playerImplementationListener[0].onError(new UnexpectedError("TEST 3!"));
        onPlaybackErrorCalled.assertCount(2);
        Assert.assertFalse(enigmaPlayer.removeListener(listener));
    }

    @Test
    public void testCallbackHandler() {
        final IPlayerImplementationListener[] playerImplementationListener = new IPlayerImplementationListener[]{null};
        final Flag installCalled = new Flag();

        MockHandler handler = new MockHandler();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new MockPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                playerImplementationListener[0] = environment.getPlayerImplementationListener();
                installCalled.setFlag();
            }
        });
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

        enigmaPlayer.setCallbackHandler(handler);

        playerImplementationListener[0].onError(new UnexpectedError("Testing again"));
        Assert.assertEquals(1, handler.runnables.size());
        onErrorCalled.assertCount(1);
        handler.runnables.get(0).run();
        onErrorCalled.assertCount(2);

        playerImplementationListener[0].onError(new UnexpectedError("Testing third"));
        Assert.assertEquals(2, handler.runnables.size());

        enigmaPlayer.removeListener(listener);
        handler.runnables.get(1).run();
        onErrorCalled.assertCount(2);
    }

    private static class MockPlayable implements IPlayable {
        private String assetId;

        public MockPlayable(String assetId) {
            this.assetId = assetId;
        }

        @Override
        public void useWith(IPlayableHandler playableHandler) {
            playableHandler.startUsingAssetId(assetId);
        }
    }
}
