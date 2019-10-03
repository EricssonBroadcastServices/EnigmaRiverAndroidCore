package com.redbeemedia.enigma.core.player;

import android.util.Log;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.drm.DrmInfoFactory;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.epg.IEpg;
import com.redbeemedia.enigma.core.epg.request.EpgRequest;
import com.redbeemedia.enigma.core.epg.request.IEpgRequest;
import com.redbeemedia.enigma.core.epg.response.IEpgResponse;
import com.redbeemedia.enigma.core.epg.response.IEpgResponseHandler;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.ServerTimeoutError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.entitlement.IEntitlementProvider;
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
import java.util.UUID;

/*package-protected*/ class DefaultPlaybackSessionFactory implements IPlaybackSessionFactory {
    private final ITimeProvider timeProvider;
    private final IEpg epg;
    private final IEntitlementProvider entitlementProvider;

    public DefaultPlaybackSessionFactory(ITimeProvider timeProvider, IEpg epg, IEntitlementProvider entitlementProvider) {
        this.timeProvider = timeProvider;
        this.epg = epg;
        this.entitlementProvider = entitlementProvider;
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

                    String playbackSessionId = jsonObject.optString("playSessionId", UUID.randomUUID().toString());
                    StreamInfo streamInfo = new StreamInfo(jsonObject.optJSONObject("streamInfo"));
                    EnigmaContractRestrictions contractRestrictions = EnigmaContractRestrictions.createWithDefaults(jsonObject.optJSONObject("contractRestrictions"));
                    IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(manifestUrl);

                    IProcessStep<IStreamPrograms> nextStep = new ProcessStep<IStreamPrograms>() {
                        @Override
                        protected void execute(IStreamPrograms streamPrograms) {
                            IInternalPlaybackSession playbackSession = new InternalPlaybackSession(new InternalPlaybackSession.ConstructorArgs(session, playbackSessionId, timeProvider, streamInfo, streamPrograms, playbackSessionInfo, contractRestrictions, entitlementProvider));
                            playerConnector.deliverPlaybackSession(playbackSession);

                            playerConnector.loadIntoPlayerImplementation(manifestUrl, playResultHandler, jsonObject, playbackProperties);
                        }
                    };

                    if(streamInfo.hasStreamPrograms()) {
                        long end = streamInfo.hasEnd() ? streamInfo.getEnd(Duration.Unit.MILLISECONDS) : (streamInfo.getStart(Duration.Unit.MILLISECONDS)+Duration.days(1).inWholeUnits(Duration.Unit.MILLISECONDS));
                        IEpgRequest request = new EpgRequest(streamInfo.getChannelId(), streamInfo.getStart(Duration.Unit.MILLISECONDS), end);
                        epg.getPrograms(request, new IEpgResponseHandler() {
                            @Override
                            public void onSuccess(IEpgResponse epgResponse) {
                                nextStep.continueProcess(new StreamPrograms(epgResponse));
                            }

                            @Override
                            public void onError(Error error) {
                                Log.d("EnigmaPlayer", "Could not fetch epg-data for "+streamInfo.getChannelId());
                                Log.d("EnigmaPlayer", error.getTrace());
                                nextStep.continueProcess(null);
                            }
                        });
                    } else {
                        nextStep.continueProcess(null);
                    }
                } else {
                    onError(new NoSupportedMediaFormatsError("Could not find a media format supported by the current player implementation."));
                }
            }
        });
    }

    private interface IProcessStep<T> {
        void continueProcess(T data);
    }

    private static abstract class ProcessStep<T> implements IProcessStep<T> {
        private boolean hasContinued = false;

        @Override
        public final synchronized void continueProcess(T data) {
            if(hasContinued) {
                throw new RuntimeException("continueProcess called twice");
            } else {
                hasContinued = true;
                execute(data);
            }
        }

        protected abstract void execute(T data);
    }
}
