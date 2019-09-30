package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.drm.DrmInfoFactory;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.ServerTimeoutError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/*package-protected*/ class DefaultPlaybackSessionFactory implements IPlaybackSessionFactory {
    private final ITimeProvider timeProvider;

    public DefaultPlaybackSessionFactory(ITimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public void startAsset(ISession session, IPlaybackProperties playbackProperties, IPlayResultHandler playResultHandler, String assetId, IEnigmaPlayerCallbacks playerConnector) {
        if(!timeProvider.isReady(Duration.seconds(30))) {
            playResultHandler.onError(new ServerTimeoutError("Could not start time service"));
            return;
        }
        URL url;
        try {
            url = session.getBusinessUnit().getApiBaseUrl("v2").append("entitlement").append(assetId).append("play").toURL();
        } catch (MalformedURLException e) {
            playResultHandler.onError(new InvalidAssetError(assetId, new UnexpectedError(e)));
            return;
        }
        AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("GET", session);
        EnigmaRiverContext.getHttpHandler().doHttp(url, apiCall, new PlayResponseHandler(assetId) {
            @Override
            protected void onError(Error error) {
                playResultHandler.onError(error);
                playerConnector.setStateIfCurrentStartAction(EnigmaPlayerState.IDLE);
            }

            @Override
            protected void onSuccess(JSONObject jsonObject) throws JSONException {
                String requestId = jsonObject.optString("requestId");
                String playToken = jsonObject.optString("playToken");
                JSONArray formats = jsonObject.getJSONArray("formats");
                JSONObject usableMediaFormat = playerConnector.getUsableMediaFormat(formats);
                if (usableMediaFormat != null) {
                    JSONObject drms = usableMediaFormat.optJSONObject("drm");
                    if (drms != null) {
                        JSONObject drmTypeInfo = drms.optJSONObject(EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey());
                        String licenseUrl = drmTypeInfo.getString("licenseServerUrl");
                        IDrmInfo drmInfo = DrmInfoFactory.createWidevineDrmInfo(licenseUrl, playToken, requestId);
                        playerConnector.setDrmInfo(drmInfo);
                    }
                    String manifestUrl = usableMediaFormat.getString("mediaLocator");
                    IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(manifestUrl);
                    IInternalPlaybackSession playbackSession = new InternalPlaybackSession(InternalPlaybackSession.ConstructorArgs.of(session, jsonObject, playbackSessionInfo, timeProvider));
                    playerConnector.deliverPlaybackSession(playbackSession);

                    playerConnector.loadIntoPlayerImplementation(manifestUrl, playResultHandler, jsonObject, playbackProperties);
                } else {
                    onError(new NoSupportedMediaFormatsError("Could not find a media format supported by the current player implementation."));
                }
            }
        });
    }
}
