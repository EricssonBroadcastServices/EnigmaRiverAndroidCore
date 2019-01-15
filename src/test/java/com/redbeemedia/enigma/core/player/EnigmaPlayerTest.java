package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.testutil.Flag;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class EnigmaPlayerTest {
    @Test
    public void testPlayer() throws JSONException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        JSONObject response = new JSONObject();
        response.put("mediaLocator", "https://media.example.com");
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), response.toString());
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        final Flag startPlaybackCalled = new Flag();
        final Flag onErrorCalled = new Flag();
        final Flag useWithCalled = new Flag();
        final Flag installCalled = new Flag();
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new IPlayerImplementation() {
            @Override
            public void install(IEnigmaPlayerEnvironment environment) {
                installCalled.setFlag();
            }

            @Override
            public void startPlayback(String url) {
                startPlaybackCalled.setFlag();
            }

            @Override
            public void release() {
            }
        });
        Assert.assertFalse(startPlaybackCalled.isTrue());
        Assert.assertTrue(installCalled.isTrue());
        enigmaPlayer.play(new IPlayRequest() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onError(Error error) {
                onErrorCalled.setFlag();
                throw new RuntimeException(error.getMessage());
            }

            @Override
            public IPlayable getPlayable() {
                return new IPlayable() {
                    @Override
                    public void useWith(IPlayableHandler playableHandler) {
                        useWithCalled.setFlag();
                        playableHandler.startUsingAssetId("123");
                    }
                };
            }
        });
        Assert.assertTrue(useWithCalled.isTrue());
        Assert.assertFalse(onErrorCalled.isTrue());
        Assert.assertTrue(startPlaybackCalled.isTrue());
    }
}
