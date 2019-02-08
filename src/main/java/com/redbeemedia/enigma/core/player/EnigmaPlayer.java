package com.redbeemedia.enigma.core.player;

import android.app.Activity;
import android.net.Uri;
import android.util.Pair;

import com.redbeemedia.enigma.core.activity.AbstractActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.drm.DrmInfoFactory;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.drm.IDrmProvider;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.ExposureHttpError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat.StreamFormat;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat.DrmTechnology;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;
import com.redbeemedia.enigma.core.json.JsonObjectResponseHandler;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.ISession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class EnigmaPlayer implements IEnigmaPlayer {
    private static final EnigmaMediaFormat[] FORMAT_PREFERENCE_ORDER = new EnigmaMediaFormat[]{new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.WIDEVINE),
                                                                                               new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.NONE)};

    private ISession session;
    private IPlayerImplementation playerImplementation;
    private EnigmaPlayerEnvironment environment = new EnigmaPlayerEnvironment();
    private WeakReference<Activity> weakActivity = new WeakReference<>(null);
    private IActivityLifecycleListener activityLifecycleListener;

    public EnigmaPlayer(ISession session, IPlayerImplementation playerImplementation) {
        this.session = session;
        this.playerImplementation = playerImplementation;
        this.playerImplementation.install(environment);
        this.activityLifecycleListener = new AbstractActivityLifecycleListener() {
            @Override
            public void onDestroy() {
                playerImplementation.release();
            }
        };
    }

    public EnigmaPlayer setActivity(Activity activity) {
        IActivityLifecycleManager lifecycleManager = EnigmaRiverContext.getActivityLifecycleManager();
        Activity oldActivity = weakActivity.get();
        if(oldActivity != null) {
            lifecycleManager.remove(oldActivity, activityLifecycleListener);
        }
        lifecycleManager.add(activity, activityLifecycleListener);
        weakActivity = new WeakReference<>(activity);
        //TODO should we allow switching activities?
        return this;
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
                url = session.getApiBaseUrl("v2").append("entitlement").append(assetId).append("play").toURL();
            } catch (MalformedURLException e) {
                //TODO invalid assetID error
                throw new RuntimeException(e);
            }
            AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("GET", session);
            EnigmaRiverContext.getHttpHandler().doHttp(url, apiCall, new PlayResponseHandler() {
                @Override
                protected void onError(Error error) {
                    playRequest.onError(error);
                }

                @Override
                protected void onSuccess(JSONObject jsonObject) throws JSONException {
                    String playToken = jsonObject.optString("playToken");
                    JSONArray formats = jsonObject.getJSONArray("formats");
                    JSONObject usableMediaFormat = getUsableMediaFormat(formats, environment.formatSupportSpec);
                    if (usableMediaFormat != null) {
                        JSONObject drms = usableMediaFormat.optJSONObject("drm");
                        if (drms != null) {
                            JSONObject drmTypeInfo = drms.optJSONObject(DrmTechnology.WIDEVINE.getKey());
                            String licenseUrl = drmTypeInfo.getString("licenseServerUrl");
                            String licenseWithToken = Uri.parse(licenseUrl)
                                    .buildUpon()
                                    .appendQueryParameter("token", "Bearer " + playToken)
                                    .build().toString();
                            IDrmInfo drmInfo = DrmInfoFactory.createWidevineDrmInfo(licenseWithToken, playToken);
                            environment.setDrmInfo(drmInfo);
                        }
                        String manifestUrl = usableMediaFormat.getString("mediaLocator");
                        //TODO need to call onStarted on the playRequest at some point.
                        //TODO here we also need to create a playback-session
                        playerImplementation.startPlayback(manifestUrl);
                    } else {
                        //TODO handle better?
                        onError(Error.NO_SUPPORTED_MEDIAFORMAT_FOUND);
                    }
                }
            });
        }

        @Override
        public void startUsingUrl(URL url) {
            //TODO handle callbacks to IPlayRequest
            playerImplementation.startPlayback(url.toString());
        }
    }

    private abstract static class PlayResponseHandler extends JsonObjectResponseHandler {
        private JsonErrorMessageHandler jsonErrorMessageHandler = new JsonErrorMessageHandler();

        public PlayResponseHandler() {
            handleErrorCode(400, Error.UNEXPECTED_ERROR);
            handleErrorCodeAndErrorMessage(401, "NO_SESSION_TOKEN", Error.UNEXPECTED_ERROR);
            handleErrorCodeAndErrorMessage(401, "INVALID_SESSION_TOKEN", Error.INVALID_SESSION);

            handleErrorCodeAndErrorMessage(403, "FORBIDDEN", Error.UNKNOWN_BUSINESS_UNIT);
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

            handleErrorCodeAndErrorMessage(404, "UNKNOWN_BUSINESS_UNIT",Error.UNKNOWN_BUSINESS_UNIT);
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

    private static JSONObject getUsableMediaFormat(JSONArray formats, IMediaFormatSupportSpec formatSupportSpec) throws JSONException {
        Map<EnigmaMediaFormat, JSONObject> foundFormats = new HashMap<>();
        for(int i = 0; i < formats.length(); ++i) {
            JSONObject mediaFormat = formats.getJSONObject(i);
            EnigmaMediaFormat enigmaMediaFormat = parseMediaFormat(mediaFormat);
            if(enigmaMediaFormat != null) {
                if(formatSupportSpec.supports(enigmaMediaFormat)) {
                    foundFormats.put(enigmaMediaFormat, mediaFormat);
                }
            }
        }
        for(EnigmaMediaFormat format : FORMAT_PREFERENCE_ORDER) {
            JSONObject object = foundFormats.get(format);
            if(object != null) {
                return object;
            }
        }
        return null;//If the format is not in FORMAT_PREFERENCE_ORDER we don't support it.
    }

    /*package-protected*/ static EnigmaMediaFormat parseMediaFormat(JSONObject mediaFormat) throws JSONException {
        String streamFormatName = mediaFormat.getString("format");
        StreamFormat streamFormat = null;
        DrmTechnology drmTechnology = null;

        if("DASH".equals(streamFormatName)) {
            streamFormat = StreamFormat.DASH;
        } else if("HLS".equals(streamFormatName)) {
            streamFormat = StreamFormat.HLS;
        } else if("SMOOTHSTREAMING".equals(streamFormatName)) {
            streamFormat = StreamFormat.SMOOTHSTREAMING;
        }

        JSONObject drm = mediaFormat.optJSONObject("drm");
        if(drm != null) {
            for(DrmTechnology drmTech : DrmTechnology.values()) {
                if(drmTech == DrmTechnology.NONE) {
                    continue;
                }
                if(drm.has(drmTech.getKey())) {
                    drmTechnology = drmTech;
                    break;
                }
            }
        } else {
            drmTechnology = DrmTechnology.NONE;
        }

        if(streamFormat != null && drmTechnology != null) {
            return new EnigmaMediaFormat(streamFormat, drmTechnology);
        } else {
            return null;
        }
    }

    private class EnigmaPlayerEnvironment implements IEnigmaPlayerEnvironment, IDrmProvider {
        private IMediaFormatSupportSpec formatSupportSpec = new DefaultFormatSupportSpec();
        private IDrmInfo drmInfo;

        @Override
        public IDrmProvider getDrmProvider() {
            return this;
        }

        @Override
        public void setMediaFormatSupportSpec(IMediaFormatSupportSpec formatSupportSpec) {
            this.formatSupportSpec = formatSupportSpec;
        }

        public void setDrmInfo(IDrmInfo drmInfo) {
            this.drmInfo = drmInfo;
        }

        @Override
        public IDrmInfo getDrmInfo() {
            return drmInfo;
        }
    }

    private static class DefaultFormatSupportSpec implements IMediaFormatSupportSpec {
        @Override
        public boolean supports(EnigmaMediaFormat enigmaMediaFormat) {
            return enigmaMediaFormat != null && enigmaMediaFormat.equals(StreamFormat.DASH, DrmTechnology.NONE);
        }
    }
}