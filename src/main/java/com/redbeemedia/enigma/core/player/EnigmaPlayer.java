package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;
import com.redbeemedia.enigma.core.json.JsonResponseHandler;
import com.redbeemedia.enigma.core.session.ISession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class EnigmaPlayer implements IEnigmaPlayer {
    private ISession session;
    private IPlayerImplementation playerImplementation;

    public EnigmaPlayer(ISession session, IPlayerImplementation playerImplementation) {
        this.session = session;
        this.playerImplementation = playerImplementation;
    }

    @Override
    public void play(IPlayRequest playRequest) {
        IPlayable playable = playRequest.getPlayable();
        playable.useWith(new PlayableHandler(playRequest));
    }

    private class PlayableHandler implements IPlayableHandler {
        private IPlayRequest playRequest;

        public PlayableHandler(IPlayRequest playRequest) {
            this.playRequest = playRequest;
        }

        @Override
        public void startUsingAssetId(String assetId) {
            URL url = null;
            try {
                url = session.getApiBaseUrl().append("entitlement").append(assetId).append("play").toURL();
            } catch (MalformedURLException e) {
                //TODO invalid assetID
                throw new RuntimeException(e);
            }
            JSONObject apiRequestBody = new JSONObject(); //TODO get capabilities from playerImplementation
            try {
                apiRequestBody.put("drm", "UNENCRYPTED");
                apiRequestBody.put("format", "DASH");
            } catch (JSONException e) {
                playRequest.onError(Error.TODO); //TODO Internal error?
                return;
            }
            AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("POST", session, apiRequestBody);
            EnigmaRiverContext.getHttpHandler().doHttp(url, apiCall, new JsonResponseHandler() {
                {
                    IHttpCodeHandler errorHandler = new IHttpCodeHandler() {
                        @Override
                        public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                            try {
                                JSONObject jsonObject = JsonInputStreamParser.obtain().parse(inputStream);
                                //TODO
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    handleErrorCode(403, errorHandler);
                    handleErrorCode(400, errorHandler);
                }
                @Override
                protected void onError(Error error) {
                    throw new RuntimeException(error.getMessage()); //TODO
                }

                @Override
                protected void onSuccess(JSONObject jsonObject) throws JSONException {
                    String manifestUrl = jsonObject.getString("mediaLocator");
                    playerImplementation.startPlayback(manifestUrl);
                }
            });
        }

        @Override
        public void startUsingUrl(URL url) {
            //TODO handle callbacks to IPlayRequest
            playerImplementation.startPlayback(url.toString());
        }
    }
}
