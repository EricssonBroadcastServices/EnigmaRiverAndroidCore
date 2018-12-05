package com.redbeemedia.enigma.core.player;

import android.net.Uri;
import android.util.Pair;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.ExposureHttpError;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;
import com.redbeemedia.enigma.core.json.JsonResponseHandler;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.ISession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class EnigmaPlayer implements IEnigmaPlayer {
    private ISession session;
    private IPlayerImplementation playerImplementation;
    private IDrmPlayerImplementation drmImplementation;

    public <T extends IPlayerImplementation & IDrmPlayerImplementation> EnigmaPlayer(final ISession session, final T player) {
        this(session, player, player);
    }

    public EnigmaPlayer(ISession session, IPlayerImplementation playerImplementation,IDrmPlayerImplementation drmImplementation) {
        this.session = session;
        this.playerImplementation = playerImplementation;
        this.drmImplementation = drmImplementation;
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
                //TODO invalid assetID error
                throw new RuntimeException(e);
            }
            JSONObject apiRequestBody = new JSONObject(); //TODO get capabilities from playerImplementation
            try {
//                apiRequestBody.put("drm", "UNENCRYPTED");
                apiRequestBody.put("drm", "CENC");
                apiRequestBody.put("format", "DASH");
            } catch (JSONException e) {
                playRequest.onError(Error.UNEXPECTED_ERROR);
                return;
            }
            AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("POST", session, apiRequestBody);
            EnigmaRiverContext.getHttpHandler().doHttp(url, apiCall, new PlayResponseHandler() {
                @Override
                protected void onError(Error error) {
                    playRequest.onError(error);
                }

                @Override
                protected void onSuccess(JSONObject jsonObject) throws JSONException {
                    ///TODO: getString optString
                    String manifestUrl = jsonObject.getString("mediaLocator");
//                    playerImplementation.startPlayback(manifestUrl);

                    //TODO; check format
                    JSONObject configObject = jsonObject.getJSONObject("cencConfig");
//
//                    //TODO anuny poxel licenseUrl
                    String certificateUrl = configObject.optString("com.widevine.alpha");
                    String playToken = jsonObject.getString("playToken");
//
                    String licenseWithToken = Uri.parse(certificateUrl)
                        .buildUpon()
                        .appendQueryParameter("token", "Bearer " + playToken)
                        .build().toString();

//                    //TODO need to call onStarted on the playRequest at some point.
//                    //TODO here we also need to create a playback-session
                    String[] keyRequestPropertiesArray = createDrmKeyRequestPropertiesArray(playToken);
                    drmImplementation.startPlaybackWithDrm(manifestUrl, licenseWithToken, keyRequestPropertiesArray);
                }
            });
        }

        @Override
        public void startUsingUrl(URL url) {
            //TODO handle callbacks to IPlayRequest
            playerImplementation.startPlayback(url.toString());
        }

        /*
        TODO: do we need
         */

        private String[] createDrmKeyRequestPropertiesArray(String playToken) {
            //TODO:do we need to check getSessionToken
            if (playToken!= null) {
                if (!playToken.isEmpty()) {
                    return new String[]{"Authorization", "Bearer " + playToken};
                }
            }

            return new String[]{};
        }
    }

    private abstract static class PlayResponseHandler extends JsonResponseHandler {
        private JsonErrorMessageHandler jsonErrorMessageHandler = new JsonErrorMessageHandler();

        public PlayResponseHandler() {
            handleErrorCode(400, Error.UNEXPECTED_ERROR);
            handleErrorCodeAndErrorMessage(401, "NO_SESSION_TOKEN", Error.UNEXPECTED_ERROR);
            handleErrorCodeAndErrorMessage(401, "INVALID_SESSION_TOKEN", Error.INVALID_SESSION);

            handleErrorCodeAndErrorMessage(403, "FORBIDDEN", Error.TODO); //TODO use 'incorrect bU' error
            handleErrorCodeAndErrorMessage(403, "NOT_ENTITLED", Error.NOT_ENTITLED);
            handleErrorCodeAndErrorMessage(403, "DEVICE_BLOCKED", Error.DEVICE_BLOCKED);
            handleErrorCodeAndErrorMessage(403, "GEO_BLOCKED", Error.GEO_BLOCKED);
            handleErrorCodeAndErrorMessage(403, "ANONYMOUS_IP_BLOCKED", Error.ANONYMOUS_IP_BLOCKED);
            handleErrorCodeAndErrorMessage(403, "LICENSE_EXPIRED", Error.EXPIRED_ASSET);
            handleErrorCodeAndErrorMessage(403, "NOT_AVAILABLE_IN_FORMAT", Error.UNEXPECTED_ERROR); //TODO When the playerImplementation provides capabilities, we can request those. If there are none available that we can play, we should send some kind of "supported formats for playerimplementation"
            handleErrorCodeAndErrorMessage(403, "NOT_ENABLED", Error.NOT_ENABLED);
            //TODO maybe a lot of these errors should be more generic, and then it's up to the
            //app developer to check if the asset CAN be played before a play-call.
            handleErrorCodeAndErrorMessage(403, "CONCURRENT_STREAMS_LIMIT_REACHED", Error.TOO_MANY_CONCURRENT_STREAMS);
            handleErrorCodeAndErrorMessage(403, "CONCURRENT_STREAMS_TVOD_LIMIT_REACHED", Error.TOO_MANY_CONCURRENT_TVODS);
            handleErrorCodeAndErrorMessage(403, "CONCURRENT_STREAMS_SVOD_LIMIT_REACHED", Error.TOO_MANY_CONCURRENT_SVODS);

            //TODO change error for UNKNOWN_BUSINESS_UNIT to "invalid businessUnit" or something
            handleErrorCodeAndErrorMessage(404, "UNKNOWN_BUSINESS_UNIT",Error.TODO);
            //TODO change error for UNKNOWN_ASSET to "unknown asset" or something
            handleErrorCodeAndErrorMessage(404, "UNKNOWN_ASSET",Error.TODO);

            handleErrorCode(422, Error.UNEXPECTED_ERROR);
        }

        private void handleErrorCodeAndErrorMessage(int httpCode, String errorMessage, Error errorToUse) {
            jsonErrorMessageHandler.handleErrorCodeAndErrorMessage(httpCode, errorMessage, errorToUse);
            handleErrorCode(httpCode, jsonErrorMessageHandler);
        }

        private class JsonErrorMessageHandler implements IHttpCodeHandler {
            private Map<Pair<Integer,String>, Error> errorMap = new HashMap<>();
            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                try {
                    JSONObject errorJson = JsonInputStreamParser.obtain().parse(inputStream);
                    ExposureHttpError exposureHttpError = new ExposureHttpError(errorJson);
                    Error errorToUse = errorMap.get(Pair.create(exposureHttpError.getHttpCode(), exposureHttpError.getMessage()));
                    if(errorToUse != null) {
                        onError(errorToUse);
                    } else {
                        onError(Error.UNEXPECTED_ERROR);
                    }
                } catch (JSONException e) {
                    onError(Error.UNEXPECTED_ERROR);
                }
            }

            private void handleErrorCodeAndErrorMessage(int httpCode, String errorMessage, Error errorToUse) {
                errorMap.put(Pair.create(httpCode, errorMessage), errorToUse);
            }
        }

    }
}
