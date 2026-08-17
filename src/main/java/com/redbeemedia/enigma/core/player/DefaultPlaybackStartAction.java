// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import static com.redbeemedia.enigma.core.analytics.OfflineAnalyticsHandler.OFFLINE_ANALYTICS_URL;
import static com.redbeemedia.enigma.core.analytics.OfflineAnalyticsHandler.UPDATE_FREQUENCY_5_MIN;
import static com.redbeemedia.enigma.core.player.AnalyticsUtil.setDeviceUtcTimeDifference;

import android.util.Log;

import com.redbeemedia.enigma.core.ads.ExposureAdMetadata;
import com.redbeemedia.enigma.core.ads.IAdDetector;
import com.redbeemedia.enigma.core.ads.IAdInsertionFactory;
import com.redbeemedia.enigma.core.ads.IAdInsertionParameters;
import com.redbeemedia.enigma.core.ads.IAdResourceLoader;
import com.redbeemedia.enigma.core.analytics.AnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.AnalyticsPlayResponseData;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.IBufferingAnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.OfflineAnalyticsHandler;
import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.drm.DrmInfoFactory;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.entitlement.EntitlementProvider;
import com.redbeemedia.enigma.core.entitlement.IEntitlementProvider;
import com.redbeemedia.enigma.core.epg.IEpg;
import com.redbeemedia.enigma.core.epg.request.EpgRequest;
import com.redbeemedia.enigma.core.epg.request.IEpgRequest;
import com.redbeemedia.enigma.core.epg.response.IEpgResponse;
import com.redbeemedia.enigma.core.epg.response.IEpgResponseHandler;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.NoInternetConnectionError;
import com.redbeemedia.enigma.core.error.NoSessionRejectionError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.ServerTimeoutError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.marker.IMarkerPointsDetector;
import com.redbeemedia.enigma.core.playable.IAssetPlayable;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.playrequest.AdobePrimetime;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.ProxyCallback;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.function.IConsumer;
import com.redbeemedia.enigma.core.util.function.IConsumerTwo;
import com.redbeemedia.enigma.core.video.ISpriteRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/*package-protected*/ class DefaultPlaybackStartAction implements IPlaybackStartAction, IPlayableHandler {
    private static final String TAG = "StartAction";

    private final ISession session;
    private final IBusinessUnit businessUnit;
    private final IPlayRequest playRequest;
    private final ITaskFactoryProvider taskFactoryProvider;
    private final IPlayerImplementationControls playerImplementationControls;
    private final IEnigmaPlayerCallbacks playerConnector;
    private final ITimeProvider timeProvider;
    private final IPlayResultHandler callback;
    private IAdDetector adDetector;
    private long deviceUtcTimeDifference;
    private IMarkerPointsDetector markerPointsDetector;
    private final ISpriteRepository spriteRepository;
    private Set<EnigmaMediaFormat> supportedFormats;
    protected static final AtomicBoolean IN_PROGRESS = new AtomicBoolean(false);

    public DefaultPlaybackStartAction(ISession defaultSession, IBusinessUnit defaultBusinessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler, ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IEnigmaPlayerCallbacks playerConnector, ISpriteRepository spriteRepository, Set<EnigmaMediaFormat> supportedFormats) {
        ISession playRequestSession = playRequest.getSession();
        this.session = playRequestSession != null ? playRequestSession : defaultSession;
        this.businessUnit = playRequestSession != null ? playRequestSession.getBusinessUnit() : defaultBusinessUnit;
        this.timeProvider = timeProvider;
        this.playRequest = playRequest;
        this.taskFactoryProvider = taskFactoryProvider;
        this.playerImplementationControls = playerImplementationControls;
        this.playerConnector = playerConnector;
        this.spriteRepository = spriteRepository;
        this.callback = ProxyCallback.useCallbackHandlerIfPresent(callbackHandler, IPlayResultHandler.class, playRequest.getResultHandler());
        this.supportedFormats = supportedFormats;
    }

    public void setAdDetector(IAdDetector adDetector) {
        this.adDetector = adDetector;
    }

    @Override
    public void setMarkerPointsDetector(IMarkerPointsDetector markerPointsDetector) {
        this.markerPointsDetector = markerPointsDetector;
    }

    @Override
    public void start() {
        ITask task = taskFactoryProvider.getTaskFactory().newTask(() -> {
            startBlocking();
        });
        try {
            task.start();
        } catch (TaskException e) {
            Log.e(TAG, "Failed to start task", e);
            getStartActionResultHandler().onError(new UnexpectedError(e));
            return;
        }
    }

    private void startBlocking() {
        try {
            playRequest.getPlayable().useWith(this);
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException while trying to use Playable", e);
            getStartActionResultHandler().onError(new UnexpectedError(e));
        }
    }

    @Override
    public void startUsingAssetId(String assetId) {
        if (!IN_PROGRESS.compareAndSet(false, true)) {
            Log.w("WARN", "Called play asset multiple times in short span, ignoring latest call");
            return;
        }

        setDeviceUtcTimeDifference((onDeviceTime) -> deviceUtcTimeDifference = onDeviceTime);
        { //Check requirements
            if (session == null) {
                IN_PROGRESS.set(false);
                getStartActionResultHandler().onError(new NoSessionRejectionError());
                return;
            }

            if (!EnigmaRiverContext.getNetworkMonitor().hasInternetAccess()) {
                // retry again in 5 seconds
                try {
                    Thread.sleep(5100);
                } catch (InterruptedException e) {
                    getStartActionResultHandler().onError(new NoInternetConnectionError());
                    IN_PROGRESS.set(false);
                    return;
                }
                if (!EnigmaRiverContext.getNetworkMonitor().hasInternetAccess()) {
                    Log.w("WARN", "Cannot access network");
                }
            }

            if (!timeProvider.isReady(Duration.seconds(30))) {
                IN_PROGRESS.set(false);
                getStartActionResultHandler().onError(new ServerTimeoutError("Could not start time service"));
                return;
            }
        }
        // make cue-points request
        contentAssetRequest(assetId);

        IAdInsertionParameters adInsertionParameters = buildAdInsertionParameters(playRequest);
        Map<String, ?> adParams = null;
        if(adInsertionParameters != null) {
            adParams = adInsertionParameters.getParameters();
        }

        AdobePrimetime adobePrimeTime = playRequest.getPlaybackProperties().getAdobePrimetime();
        String adobePrimeTimeToken = (adobePrimeTime != null) ? adobePrimeTime.token : null;


        PlayRequest pr = new PlayRequest(
                session,
                assetId,
                adParams,
                adobePrimeTimeToken,
                supportedFormats,
                playRequest.getPlaybackProperties().getMaterialProfile()
        );

        try {
            pr.call(
                    (IConsumer<EnigmaError>) enigmaError -> {
                        IN_PROGRESS.set(false);
                        getStartActionResultHandler().onError(enigmaError);

                        playerImplementationControls.stop(new IPlayerImplementationControlResultHandler() {
                            @Override
                            public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
                            }

                            @Override
                            public void onCancelled() {
                            }

                            @Override
                            public void onError(EnigmaError error) {
                            }

                            @Override
                            public void onDone() {
                                setStateIfCurrentStartAction(EnigmaPlayerState.IDLE);
                            }
                        });
                    },
                    (IConsumerTwo<JSONObject, IConsumer<EnigmaError>>) (jsonObject, onError) -> {
                        try {
                            String requestId = jsonObject.optString("requestId");
                            String playToken = jsonObject.optString("playToken");
                            String assetId1 = jsonObject.optString("assetId");
                            JSONArray formats = jsonObject.getJSONArray("formats");
                            JSONArray spritesJson = jsonObject.optJSONArray("sprites");
                            boolean audioOnly = jsonObject.optBoolean("audioOnly", false);
                            MediaType mediaType = audioOnly ? MediaType.AUDIO : MediaType.VIDEO;
                            JSONObject usableMediaFormat = playerConnector.getUsableMediaFormat(formats);
                            if (usableMediaFormat != null) {
                                JSONObject epgObject = jsonObject.optJSONObject("epg");
                                boolean epgEnabled = false;
                                boolean entitlementCheck = false;
                                if (epgObject != null) {
                                    epgEnabled = epgObject.optBoolean("enabled", false);
                                    entitlementCheck = epgObject.optBoolean("entitlementCheck", false);
                                }

                                JSONObject drms = usableMediaFormat.optJSONObject("drm");
                                final IDrmInfo[] drmInfo = new IDrmInfo[]{null};
                                final String streamingTechnology = usableMediaFormat.optString("format");
                                if ("DASH".equals(streamingTechnology) && drms != null) {
                                    JSONObject drmTypeInfo = drms.optJSONObject(EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey());
                                    String licenseUrl = drmTypeInfo.getString("licenseServerUrl");
                                    drmInfo[0] = DrmInfoFactory.createWidevineDrmInfo(licenseUrl, playToken, requestId);
                                }

                                final AnalyticsPlayResponseData analyticsInformation = new AnalyticsPlayResponseData(jsonObject, streamingTechnology, session);
                                String manifestUrl = usableMediaFormat.getString("mediaLocator");

                                final Duration liveDelay = usableMediaFormat.has("liveDelay") ? Duration.millis(usableMediaFormat.getLong("liveDelay")) : null;

                                String playbackSessionId = jsonObject.optString("playSessionId", UUID.randomUUID().toString());
                                JsonStreamInfo streamInfo = new JsonStreamInfo(jsonObject.optJSONObject("streamInfo"), mediaType);
                                EnigmaContractRestrictions contractRestrictions = EnigmaContractRestrictions.createWithDefaults(jsonObject.optJSONObject("contractRestrictions"));

                                JSONObject cdnObject = jsonObject.optJSONObject("cdn");
                                String cdnProvider = null;
                                if (cdnObject != null) {
                                    cdnProvider = cdnObject.optString("provider", "");
                                }
                                Integer durationInMilliseconds = jsonObject.optInt("durationInMilliseconds", -1);
                                IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(assetId1, manifestUrl, cdnProvider, playbackSessionId, durationInMilliseconds);
                                final JSONObject adsInfoJson = jsonObject.optJSONObject("ads");
                                final ExposureAdMetadata adsInfo = new ExposureAdMetadata(
                                        adsInfoJson,
                                        EnigmaMediaFormat.parseMediaFormat(usableMediaFormat).getStreamFormat(),
                                        streamInfo.isLiveStream());

                                if (streamInfo.ssaiEnabled() && adDetector != null) {
                                    adDetector.setEnabled(true);
                                    adDetector.getTimeline().setIsActive(true);
                                    if (!streamInfo.isLiveStream()) {
                                        IAdResourceLoader adsLoader = adDetector.getFactory().createResourceLoader(adsInfo, adsInfoJson);
                                        if (adsLoader != null) {
                                            adDetector.update(adsLoader, 0);
                                        }
                                    }
                                    adDetector.setLiveDelay(liveDelay);
                                } else if (!streamInfo.ssaiEnabled() && adDetector != null) {
                                    adDetector.setEnabled(false);
                                    adDetector.setLiveDelay(liveDelay);
                                }

                                final Map<Integer, String> spriteUrls = parseSpriteUrls(spritesJson);
                                spriteRepository.setVTTUrls(spriteUrls, session);

                                boolean finalEntitlementCheck = entitlementCheck;
                                IProcessStep<IStreamPrograms> nextStep = new ProcessStep<>() {
                                    @Override
                                    protected void execute(IStreamPrograms streamPrograms) {

                                        final IBufferingAnalyticsHandler analyticsHandler = newOnlineAnalyticsHandler(assetId1, session, playbackSessionId, timeProvider, analyticsInformation);
                                        Analytics analytics = playRequest.getPlaybackProperties().enableAnalytics() ?
                                                createAnalytics(timeProvider, taskFactoryProvider.getTaskFactory(), analyticsHandler, analyticsInformation.postIntervalSeconds * 1000) :
                                                Analytics.silentAnalitycs();


                                        IInternalPlaybackSession playbackSession = newPlaybackSession(
                                                new InternalPlaybackSession.ConstructorArgs(
                                                        streamInfo,
                                                        streamPrograms,
                                                        playbackSessionInfo,
                                                        contractRestrictions,
                                                        drmInfo[0],
                                                        analytics.getAnalyticsReporter(),
                                                        spriteRepository,
                                                        adsInfo,
                                                        adDetector, false, null));
                                        playbackSession.addInternalListener(analytics.getInternalPlaybackSessionListener());
                                        playbackSession.addInternalListener(createProgramService(session, streamInfo, streamPrograms, playbackSessionInfo, newEntitlementProvider(), playbackSession, taskFactoryProvider, finalEntitlementCheck));
                                        playbackSession.setPlayerImplementationControls(playerImplementationControls);
                                        playerConnector.deliverPlaybackSession(playbackSession);

                                        setStateIfCurrentStartAction(EnigmaPlayerState.LOADING);

                                        PlayerImplementationLoadRequest loadRequest = new PlayerImplementationLoadRequest.Stream(manifestUrl, contractRestrictions);
                                        if (liveDelay != null) {
                                            loadRequest.setLiveDelay(liveDelay);
                                        }
                                        playerImplementationControls.load(loadRequest, new StartPlaybackControlResultHandler(getStartActionResultHandler(), jsonObject, playRequest.getPlaybackProperties().getPlayFrom(), playerImplementationControls, adDetector) {
                                            @Override
                                            protected void onLogDebug(String message) {
                                                Log.d(TAG, message);
                                            }
                                        });
                                    }
                                };

                                if (streamInfo.hasStreamPrograms() && epgEnabled) {
                                    long end = streamInfo.hasEnd() ? streamInfo.getEnd(Duration.Unit.MILLISECONDS) : (streamInfo.getStart(Duration.Unit.MILLISECONDS) + Duration.days(1).inWholeUnits(Duration.Unit.MILLISECONDS));
                                    IEpgRequest request = new EpgRequest(streamInfo.getChannelId(), streamInfo.getStart(Duration.Unit.MILLISECONDS), end);
                                    IEpg epg = createEpg(session.getBusinessUnit());
                                    epg.getPrograms(request, new IEpgResponseHandler() {
                                        @Override
                                        public void onSuccess(IEpgResponse epgResponse) {
                                            nextStep.continueProcess(new StreamPrograms(epgResponse, streamInfo.isLiveStream(), deviceUtcTimeDifference));
                                        }

                                        @Override
                                        public void onError(EnigmaError error) {
                                            Log.d("EnigmaPlayer", "Could not fetch epg-data for " + streamInfo.getChannelId());
                                            Log.d("EnigmaPlayer", error.getTrace());
                                            nextStep.continueProcess(null);
                                        }
                                    });
                                } else {
                                    nextStep.continueProcess(null);
                                }
                            } else {
                                String format = "";
                                boolean isDrmSupported = false;
                                for (int i = 0; i < formats.length(); ++i) {
                                    JSONObject mediaFormat = formats.getJSONObject(i);
                                    format = mediaFormat.optString("format", "null");
                                    Object drm = mediaFormat.opt("drm");
                                    if (drm != null) {
                                        isDrmSupported = true;
                                    }
                                    break;
                                }
                                onError.accept(new NoSupportedMediaFormatsError("Could not find a media format supported by the current player implementation. Requested format:" + format + ", isDRM:" + isDrmSupported));
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        } finally {
                            IN_PROGRESS.set(false);
                        }
                    }
            );
        }  catch (MalformedURLException e) {
            IN_PROGRESS.set(false);
            getStartActionResultHandler().onError(new InvalidAssetError(assetId, new UnexpectedError(e)));
        }
    }

    private void contentAssetRequest(String assetId) {
        URL url = null;
        try {
            UrlPath path = session.getBusinessUnit().getApiBaseUrl("v1").append("content/asset").append(assetId);
            url = path.toURL();
        } catch (MalformedURLException e) {
            getStartActionResultHandler().onError(new InvalidAssetError(assetId, new UnexpectedError(e)));
        }

        AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("GET", session) {
            @Override
            public void prepare(IHttpConnection connection) {
                super.prepare(connection);
            }
        };

        EnigmaRiverContext.getHttpHandler().doHttp(url, apiCall, new PlayResponseHandler(assetId) {

            @Override
            protected void onError(EnigmaError error) {
                Log.w("MarkerPoints", "Couldn't fetch marker point from the server :" + error.getTrace());
            }

            @Override
            protected void onSuccess(JSONObject jsonObject) {
                markerPointsDetector.parseJSONObject(jsonObject);
            }
        });
    }

    private void setStateIfCurrentStartAction(EnigmaPlayerState state) {
        playerConnector.setStateIfCurrentStartAction(this, state);
    }

    protected IPlayResultHandler getStartActionResultHandler() {
        return callback;
    }

    protected IAdInsertionParameters buildAdInsertionParameters(IPlayRequest playRequest) {
        IAdInsertionFactory adInsertionFactory = EnigmaRiverContext.getAdInsertionFactory();
        if (adInsertionFactory == null) {
            return null;
        }
        return adInsertionFactory.createParameters(playRequest);
    }

    @Override
    public void startUsingDownloadData(Object downloadData, String playbackSessionId, String analyticsBaseUrl, String cdnProvider, int duration) {
        try {
            IPlayable playable = playRequest.getPlayable();
            String assetId = ((IAssetPlayable) playable).getAssetId();
            EnigmaPlayerState state = playerConnector.getState();
            boolean sessionResumed = state == EnigmaPlayerState.PAUSED;
            IPlaybackProperties.PlayFrom playFrom = playRequest.getPlaybackProperties().getPlayFrom();
            if (playFrom.getPreferences().contains(IPlaybackProperties.PlayFrom.PlayFromPreference.BOOKMARK)
                    || playFrom.getPreferences().contains(IPlaybackProperties.PlayFrom.PlayFromPreference.LIVE_EDGE)) {
                // then throw an exception, as live or bookmark is not supported for offlines
                throw new IllegalArgumentException("Offline playback does not support BOOKMARK or LIVE_EDGE." +
                        " Only BEGINNING or OFFSET is supported.");
            }

            byte[] analyticsBaseUrlBytes = analyticsBaseUrl.getBytes(StandardCharsets.UTF_8);
            EnigmaRiverContext.getEnigmaStorageManager().store(OFFLINE_ANALYTICS_URL, analyticsBaseUrlBytes);

            String newPlaySessionId = "null";
            if (!sessionResumed) {
                newPlaySessionId = EnigmaRiverContext.getEnigmaStorageManager().getOfflineEventsPlaybackSessionId(assetId, playbackSessionId);
            } else {
                // means it is resuming the play-session
                ITimelinePosition timelineCurrentPosition = playerConnector.getTimelineCurrentPosition();
                playFrom = IPlaybackProperties.PlayFrom.OFFSET(Duration.millis(timelineCurrentPosition.getStart()));
            }

            IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(null, "mockManifest.mpd", cdnProvider, newPlaySessionId,
                    duration);
            IContractRestrictions contractRestrictions = new EmptyContractRestrictions();
            JSONObject offlineJsonObject = new JSONObject();
            JSONObject cdnJsonObject = new JSONObject();
            cdnJsonObject.put("host", cdnProvider);
            offlineJsonObject.put("cdn", cdnJsonObject);
            offlineJsonObject.put("analytics", new JSONObject());
            final AnalyticsPlayResponseData analyticsInformation = new AnalyticsPlayResponseData(offlineJsonObject, null, session);
            final IBufferingAnalyticsHandler analyticsHandler = newOfflineAnalyticsHandler(playbackSessionInfo.getAssetId(), session, playbackSessionInfo.getPlaybackSessionId(), timeProvider, analyticsInformation, analyticsBaseUrl);

            Analytics analytics = createAnalytics(timeProvider, taskFactoryProvider.getTaskFactory(), analyticsHandler, UPDATE_FREQUENCY_5_MIN);

            IInternalPlaybackSession playbackSession = newPlaybackSession(new InternalPlaybackSession.ConstructorArgs(
                    new DownloadStreamInfo(),
                    null,
                    playbackSessionInfo,
                    contractRestrictions,
                    null,
                    analytics.getAnalyticsReporter(),
                    null,
                    null,
                    adDetector,
                    sessionResumed,
                    Duration.minutes(60)
            ));
            playbackSession.addInternalListener(analytics.getInternalPlaybackSessionListener());
            playbackSession.setPlayerImplementationControls(playerImplementationControls);


            if (playerConnector.getState() != EnigmaPlayerState.PAUSED) {
                // will start new session
                playerConnector.deliverPlaybackSession(playbackSession);
                setStateIfCurrentStartAction(EnigmaPlayerState.LOADING);
            }
            IPlayerImplementationControls.ILoadRequest loadRequest =
                    new PlayerImplementationLoadRequest.Download(downloadData, contractRestrictions);
            IPlaybackProperties.PlayFrom finalPlayFrom = playFrom;
            playerImplementationControls.load(loadRequest, new PlayResultControlResultHandler(getStartActionResultHandler()) {
                @Override
                public void onDone() {
                    playerImplementationControls.load(loadRequest, new StartPlaybackControlResultHandler(getStartActionResultHandler(), null, finalPlayFrom, playerImplementationControls, adDetector) {
                        @Override
                        protected void onLogDebug(String message) {
                            Log.d(TAG, message);
                        }
                    });
                }
            });
        } catch (RuntimeException | JSONException e) {
            e.printStackTrace();
            getStartActionResultHandler().onError(new UnexpectedError(e));
        }
    }

    @Override
    public void startUsingUrl(URL url) {
        if (!EnigmaRiverContext.getNetworkMonitor().hasInternetAccess()) {
            getStartActionResultHandler().onError(new NoInternetConnectionError());
            return;
        }

        IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(null, url.toString(), null, null, -1);
        IInternalPlaybackSession playbackSession = newPlaybackSession(new InternalPlaybackSession.ConstructorArgs(
                new UrlPlayableStreamInfo(),
                null,
                playbackSessionInfo,
                new EmptyContractRestrictions(),
                null,
                new IgnoringAnalyticsReporter(),
                null,
                null,
                adDetector,
                false,null
        ));
        playerConnector.deliverPlaybackSession(playbackSession);

        setStateIfCurrentStartAction(EnigmaPlayerState.LOADING);

        IPlayerImplementationControls.ILoadRequest loadRequest = new PlayerImplementationLoadRequest.Stream(url.toString(), new EmptyContractRestrictions());
        playerImplementationControls.load(loadRequest, new PlayResultControlResultHandler(getStartActionResultHandler()) {
            @Override
            public void onDone() {
                try {
                    playerImplementationControls.start(new PlayResultControlResultHandler(getStartActionResultHandler()) {
                        @Override
                        public void onDone() {
                        }
                    });
                } catch (RuntimeException e) {
                    getStartActionResultHandler().onError(new UnexpectedError(e));
                }
            }
        });
    }

    protected IInternalPlaybackSession newPlaybackSession(InternalPlaybackSession.ConstructorArgs constructorArgs) {
        return new InternalPlaybackSession(constructorArgs);
    }

    @Override
    public void onStarted(IInternalPlaybackSession internalPlaybackSession) {
        getStartActionResultHandler().onStarted(internalPlaybackSession);
    }

    @Override
    public void cancel() {
    }

    @Override
    public void onErrorDuringStartup(EnigmaError error) {
        getStartActionResultHandler().onError(error);
    }

    protected Analytics createAnalytics(ITimeProvider timeProvider, ITaskFactory taskFactory, IBufferingAnalyticsHandler analyticsHandler, int updateFrequency) {
        IAnalyticsReportingTask reportingTask = newAnalyticsReportingTask(taskFactory, analyticsHandler, updateFrequency);
        return new Analytics(newAnalyticsReporter(timeProvider, analyticsHandler), new IInternalPlaybackSessionListener() {
            @Override
            public void onStart(OnStartArgs args) {
                reportingTask.start();
            }

            @Override
            public void onStop(OnStopArgs args) {
                reportingTask.stop();
            }
        });
    }

    protected IAnalyticsReportingTask newAnalyticsReportingTask(ITaskFactory taskFactory, IBufferingAnalyticsHandler analyticsHandler, int updateFrequency) {
        return new AnalyticsReportingTask(taskFactory, analyticsHandler, updateFrequency);
    }

    protected IBufferingAnalyticsHandler newOnlineAnalyticsHandler(String assetId, ISession session, String playbackSessionId, ITimeProvider timeProvider, AnalyticsPlayResponseData analyticsPlayResponseData) {
        return new AnalyticsHandler(session, playbackSessionId, timeProvider, analyticsPlayResponseData);
    }

    protected IBufferingAnalyticsHandler newOfflineAnalyticsHandler(String assetId, ISession session, String playbackSessionId, ITimeProvider timeProvider, AnalyticsPlayResponseData analyticsPlayResponseData, String analyticsBaseUrl) {
        return new OfflineAnalyticsHandler(assetId, session, playbackSessionId, timeProvider, analyticsPlayResponseData, analyticsBaseUrl);
    }

    protected IAnalyticsReporter newAnalyticsReporter(ITimeProvider timeProvider, IBufferingAnalyticsHandler analyticsHandler) {
        return new AnalyticsReporter(timeProvider, analyticsHandler, deviceUtcTimeDifference);
    }


    protected IEntitlementProvider newEntitlementProvider() {
        return new EntitlementProvider(EnigmaRiverContext.getHttpHandler());
    }

    protected IInternalPlaybackSessionListener createProgramService(ISession session, IStreamInfo streamInfo, IStreamPrograms streamPrograms, IPlaybackSessionInfo playbackSessionInfo, IEntitlementProvider entitlementProvider, IPlaybackSession playbackSession, ITaskFactoryProvider taskFactoryProvider, boolean entitlementCheck) {
        return new ProgramService(session, streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession, taskFactoryProvider, entitlementCheck);
    }

    protected IEpg createEpg(IBusinessUnit businessUnit) {
        return EnigmaRiverContext.getEpgLocator().getEpg(businessUnit);
    }

    private interface IProcessStep<T> {
        void continueProcess(T data);
    }

    private Map<Integer, String> parseSpriteUrls(JSONArray spritesJson) throws JSONException {
        final HashMap<Integer, String> spriteUrls = new HashMap<>();
        if (spritesJson != null) {
            for (int i = 0; i < spritesJson.length(); i++) {
                spriteUrls.put(spritesJson.getJSONObject(i).getInt("width"), spritesJson.getJSONObject(i).getString("vtt"));
            }
        }
        return spriteUrls;
    }

    private static abstract class ProcessStep<T> implements IProcessStep<T> {
        private boolean hasContinued = false;

        @Override
        public final synchronized void continueProcess(T data) {
            if (hasContinued) {
                throw new RuntimeException("continueProcess called twice");
            } else {
                hasContinued = true;
                execute(data);
            }
        }

        protected abstract void execute(T data);
    }

    private static class PlayResultControlResultHandler extends BasePlayerImplementationControlResultHandler {
        private IPlayResultHandler playResultHandler;

        public PlayResultControlResultHandler(IPlayResultHandler playResultHandler) {
            this.playResultHandler = playResultHandler;
        }

        @Override
        public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
            this.playResultHandler.onError(new UnexpectedError("Rejected"));
        }

        @Override
        public void onError(EnigmaError error) {
            playResultHandler.onError(error);
        }
    }
}

