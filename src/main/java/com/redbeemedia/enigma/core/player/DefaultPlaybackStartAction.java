package com.redbeemedia.enigma.core.player;

import android.util.Log;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.ads.ExposureAdMetadata;
import com.redbeemedia.enigma.core.ads.IAdResourceLoader;
import com.redbeemedia.enigma.core.ads.IAdDetector;
import com.redbeemedia.enigma.core.analytics.AnalyticsException;
import com.redbeemedia.enigma.core.analytics.AnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.AnalyticsPlayResponseData;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.IBufferingAnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.SilentAnalyticsReporter;
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
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.ads.IAdInsertionFactory;
import com.redbeemedia.enigma.core.ads.IAdInsertionParameters;
import com.redbeemedia.enigma.core.playrequest.AdobePrimetime;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
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
import com.redbeemedia.enigma.core.video.ISpriteRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private final ISpriteRepository spriteRepository;

    public DefaultPlaybackStartAction(ISession defaultSession, IBusinessUnit defaultBusinessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler, ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IEnigmaPlayerCallbacks playerConnector, ISpriteRepository spriteRepository) {
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
    }

    public void setAdDetector(IAdDetector adDetector) {
        this.adDetector = adDetector;
    }

    @Override
    public void start() {
        ITask task = taskFactoryProvider.getTaskFactory().newTask(() -> {
            startBlocking();
        });
        try {
            task.start();
        } catch (TaskException e) {
            Log.e(TAG, "Failed to start task",e);
            getStartActionResultHandler().onError(new UnexpectedError(e));
            return;
        }
    }

    private void startBlocking() {
        try {
            playRequest.getPlayable().useWith(this);
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException while trying to use Playable",e);
            getStartActionResultHandler().onError(new UnexpectedError(e));
        }
    }

    @Override
    public void startUsingAssetId(String assetId) {
        { //Check requirements
            if (session == null) {
                getStartActionResultHandler().onError(new NoSessionRejectionError());
                return;
            }

            if (!EnigmaRiverContext.getNetworkMonitor().hasInternetAccess()) {
                getStartActionResultHandler().onError(new NoInternetConnectionError());
                return;
            }

            if(!timeProvider.isReady(Duration.seconds(30))) {
                getStartActionResultHandler().onError(new ServerTimeoutError("Could not start time service"));
                return;
            }
        }

        URL url;
        try {
            UrlPath path = session.getBusinessUnit().getApiBaseUrl("v2").append("entitlement").append(assetId).append("play");

            IAdInsertionParameters adInsertionParameters = buildAdInsertionParameters(playRequest);
            if (adInsertionParameters != null) {
                path = path.appendQueryStringParameters(adInsertionParameters.getParameters());
            }
            url = path.toURL();

        } catch (MalformedURLException e) {
            getStartActionResultHandler().onError(new InvalidAssetError(assetId, new UnexpectedError(e)));
            return;
        }
        AdobePrimetime adobePrimeTime = playRequest.getPlaybackProperties().getAdobePrimetime();
        AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("GET", session) {
            @Override
            public void prepare(IHttpConnection connection) {
                if(adobePrimeTime != null) { connection.setHeader(AdobePrimetime.HTTP_HEADER_KEY, adobePrimeTime.token); }
                super.prepare(connection);
            }
        };

        EnigmaRiverContext.getHttpHandler().doHttp(url, apiCall, new PlayResponseHandler(assetId) {
            @Override
            protected void onError(EnigmaError error) {
                getStartActionResultHandler().onError(error);

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
            }

            @Override
            protected void onSuccess(JSONObject jsonObject) throws JSONException {
                String requestId = jsonObject.optString("requestId");
                String playToken = jsonObject.optString("playToken");
                JSONArray formats = jsonObject.getJSONArray("formats");
                JSONArray spritesJson = jsonObject.optJSONArray("sprites");
                JSONObject usableMediaFormat = playerConnector.getUsableMediaFormat(formats);
                if (usableMediaFormat != null) {
                    JSONObject drms = usableMediaFormat.optJSONObject("drm");
                    final IDrmInfo[] drmInfo = new IDrmInfo[]{null};
                    final String streamingTechnology = usableMediaFormat.optString("format");
                    if ("DASH".equals(streamingTechnology) && drms != null) {
                        JSONObject drmTypeInfo = drms.optJSONObject(EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey());
                        String licenseUrl = drmTypeInfo.getString("licenseServerUrl");
                        drmInfo[0] = DrmInfoFactory.createWidevineDrmInfo(licenseUrl, playToken, requestId);
                    }

                    final AnalyticsPlayResponseData analyticsInformation = new AnalyticsPlayResponseData(jsonObject, streamingTechnology);
                    String manifestUrl = usableMediaFormat.getString("mediaLocator");

                    final Duration liveDelay = usableMediaFormat.has("liveDelay") ? Duration.millis(usableMediaFormat.getLong("liveDelay")) : null;

                    String playbackSessionId = jsonObject.optString("playSessionId", UUID.randomUUID().toString());
                    JsonStreamInfo streamInfo = new JsonStreamInfo(jsonObject.optJSONObject("streamInfo"));
                    EnigmaContractRestrictions contractRestrictions = EnigmaContractRestrictions.createWithDefaults(jsonObject.optJSONObject("contractRestrictions"));
                    IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(manifestUrl);

                    final JSONObject adsInfoJson = jsonObject.optJSONObject("ads");
                    final ExposureAdMetadata adsInfo = new ExposureAdMetadata(
                            adsInfoJson,
                            EnigmaMediaFormat.parseMediaFormat(usableMediaFormat).getStreamFormat(),
                            streamInfo.isLiveStream());

                    if (streamInfo.ssaiEnabled() && adDetector != null) {
                        adDetector.setEnabled(true);
                        adDetector.getTimeline().setIsActive(true);
                        if(!streamInfo.isLiveStream()) {
                            IAdResourceLoader adsLoader = adDetector.getFactory().createResourceLoader(adsInfo, adsInfoJson);
                            if (adsLoader != null) {
                                adDetector.update(adsLoader, 0);
                            }
                        }
                    } else if (!streamInfo.ssaiEnabled() && adDetector != null) {
                        adDetector.setEnabled(false);
                    }

                    final Map<Integer, String> spriteUrls = parseSpriteUrls(spritesJson);
                    spriteRepository.setVTTUrls(spriteUrls, session);

                    IProcessStep<IStreamPrograms> nextStep = new ProcessStep<IStreamPrograms>() {
                        @Override
                        protected void execute(IStreamPrograms streamPrograms) {

                            Analytics analytics = playRequest.getPlaybackProperties().enableAnalytics() ?
                                    createAnalytics(session, playbackSessionId, timeProvider, taskFactoryProvider.getTaskFactory(), analyticsInformation) :
                                    Analytics.silentAnalitycs();


                            IInternalPlaybackSession playbackSession = newPlaybackSession(
                                    new InternalPlaybackSession.ConstructorArgs(
                                            streamInfo,
                                            streamPrograms,
                                            playbackSessionInfo,
                                            contractRestrictions,
                                            drmInfo[0],
                                            analytics.analyticsReporter,
                                            spriteRepository,
                                            adsInfo,
                                            adDetector));
                            playbackSession.addInternalListener(analytics.internalPlaybackSessionListener);
                            playbackSession.addInternalListener(createProgramService(session, streamInfo, streamPrograms, playbackSessionInfo, newEntitlementProvider(), playbackSession, taskFactoryProvider));

                            playerConnector.deliverPlaybackSession(playbackSession);

                            setStateIfCurrentStartAction(EnigmaPlayerState.LOADING);

                            PlayerImplementationLoadRequest loadRequest = new PlayerImplementationLoadRequest.Stream(manifestUrl, contractRestrictions);
                            if(liveDelay != null) {
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

                    if(streamInfo.hasStreamPrograms()) {
                        long end = streamInfo.hasEnd() ? streamInfo.getEnd(Duration.Unit.MILLISECONDS) : (streamInfo.getStart(Duration.Unit.MILLISECONDS)+Duration.days(1).inWholeUnits(Duration.Unit.MILLISECONDS));
                        IEpgRequest request = new EpgRequest(streamInfo.getChannelId(), streamInfo.getStart(Duration.Unit.MILLISECONDS), end);
                        IEpg epg = createEpg(session.getBusinessUnit());
                        epg.getPrograms(request, new IEpgResponseHandler() {
                            @Override
                            public void onSuccess(IEpgResponse epgResponse) {
                                nextStep.continueProcess(new StreamPrograms(epgResponse));
                            }

                            @Override
                            public void onError(EnigmaError error) {
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

    private void setStateIfCurrentStartAction(EnigmaPlayerState state) {
        playerConnector.setStateIfCurrentStartAction(this, state);
    }

    protected IPlayResultHandler getStartActionResultHandler() {
        return callback;
    }

    protected IAdInsertionParameters buildAdInsertionParameters(IPlayRequest playRequest) {
        IAdInsertionFactory adInsertionFactory = EnigmaRiverContext.getAdInsertionFactory();
        if(adInsertionFactory == null) {
            return null;
        }
        return adInsertionFactory.createParameters(playRequest);
    }

    @Override
    public void startUsingDownloadData(Object downloadData) {
        try {
            IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo("mockManifest.mpd");
            IContractRestrictions contractRestrictions = new EmptyContractRestrictions();
            IInternalPlaybackSession playbackSession = newPlaybackSession(new InternalPlaybackSession.ConstructorArgs(
                    new DownloadStreamInfo(),
                    null,
                    playbackSessionInfo,
                    contractRestrictions,
                    null,
                    new IgnoringAnalyticsReporter(),
                    null,
                    null,
                    adDetector
            ));
            playerConnector.deliverPlaybackSession(playbackSession);

            setStateIfCurrentStartAction(EnigmaPlayerState.LOADING);

            IPlayerImplementationControls.ILoadRequest loadRequest =
                    new PlayerImplementationLoadRequest.Download(downloadData, contractRestrictions);
            playerImplementationControls.load(loadRequest, new PlayResultControlResultHandler(getStartActionResultHandler()) {
                @Override
                public void onDone() {
                    playerImplementationControls.start(new PlayResultControlResultHandler(getStartActionResultHandler()) {
                        @Override
                        public void onDone() {
                        }
                    });
                }
            });
        } catch(RuntimeException e) {
            getStartActionResultHandler().onError(new UnexpectedError(e));
            return;
        }
    }

    @Override
    public void startUsingUrl(URL url) {
        if(!EnigmaRiverContext.getNetworkMonitor().hasInternetAccess()) {
            getStartActionResultHandler().onError(new NoInternetConnectionError());
            return;
        }

        IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(url.toString());
        IInternalPlaybackSession playbackSession = newPlaybackSession(new InternalPlaybackSession.ConstructorArgs(
                new UrlPlayableStreamInfo(),
                null,
                playbackSessionInfo,
                new EmptyContractRestrictions(),
                null,
                new IgnoringAnalyticsReporter(),
                null,
                null,
                adDetector
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

    protected Analytics createAnalytics(ISession session, String playbackSessionId, ITimeProvider timeProvider, ITaskFactory taskFactory, AnalyticsPlayResponseData analyticsInformation) {
        final IBufferingAnalyticsHandler analyticsHandler = newAnalyticsHandler(session, playbackSessionId, timeProvider, analyticsInformation);

        final ITask analyticsHandlerTask = taskFactory.newTask(newAnalyticsHandlerRunnable(analyticsHandler, analyticsInformation.postIntervalSeconds * 1000));

        return new Analytics(newAnalyticsReporter(timeProvider, analyticsHandler), new IInternalPlaybackSessionListener() {
            @Override
            public void onStart(OnStartArgs args) {
                try {
                    analyticsHandlerTask.start();
                } catch (TaskException e) {
                    throw new RuntimeException("Could not start analyticsHandlerTask", e);
                }
            }

            @Override
            public void onStop(OnStopArgs args) {
                try {
                    analyticsHandlerTask.cancel(500);
                } catch (TaskException e) {
                    e.printStackTrace(); //Suppress
                }
                ITask sendRemainingDataTask = taskFactory.newTask(() -> {
                    try {
                        analyticsHandler.sendData(); //Try to send any remaining data.
                    } catch (Exception e) {
                        e.printStackTrace(); //Suppress
                    }
                });
                try {
                    sendRemainingDataTask.start();
                } catch (TaskException e) {
                    e.printStackTrace(); //Suppress
                }
            }
        });
    }

    protected Runnable newAnalyticsHandlerRunnable(IBufferingAnalyticsHandler analyticsHandler, long updateFrequency) {
        return new Runnable() {
            @Override
            public void run() {
                boolean initialized = false;
                while(!initialized) {
                    try {
                        analyticsHandler.init();
                        initialized = true;
                    } catch (AnalyticsException e) {
                        handleException(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                while(!Thread.interrupted()) {
                    try {
                        try {
                            analyticsHandler.sendData();
                            Thread.sleep(updateFrequency);
                        } catch (AnalyticsException e) {
                            handleException(e);
                            Thread.sleep(updateFrequency);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            private void handleException(Exception e) {
                if(BuildConfig.DEBUG) {
                    throw new RuntimeException(e);
                } else {
                    e.printStackTrace(); //Log
                }
            }
        };
    }

    protected IBufferingAnalyticsHandler newAnalyticsHandler(ISession session, String playbackSessionId, ITimeProvider timeProvider, AnalyticsPlayResponseData analyticsPlayResponseData) {
        return new AnalyticsHandler(session, playbackSessionId, timeProvider, analyticsPlayResponseData);
    }

    protected IAnalyticsReporter newAnalyticsReporter(ITimeProvider timeProvider, IBufferingAnalyticsHandler analyticsHandler) {
        return new AnalyticsReporter(timeProvider, analyticsHandler);
    }


    protected IEntitlementProvider newEntitlementProvider() {
        return new EntitlementProvider(EnigmaRiverContext.getHttpHandler());
    }

    protected IInternalPlaybackSessionListener createProgramService(ISession session, IStreamInfo streamInfo, IStreamPrograms streamPrograms, IPlaybackSessionInfo playbackSessionInfo, IEntitlementProvider entitlementProvider, IPlaybackSession playbackSession, ITaskFactoryProvider taskFactoryProvider) {
        return new ProgramService(session, streamInfo, streamPrograms, playbackSessionInfo, entitlementProvider, playbackSession, taskFactoryProvider);
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
            for(int i = 0; i < spritesJson.length(); i++) {
                spriteUrls.put(spritesJson.getJSONObject(i).getInt("width"), spritesJson.getJSONObject(i).getString("vtt"));
            }
        }
        return spriteUrls;
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

    /*package-protected*/ static class Analytics {
        private final IAnalyticsReporter analyticsReporter;
        private final IInternalPlaybackSessionListener internalPlaybackSessionListener;

        public Analytics(IAnalyticsReporter analyticsReporter, IInternalPlaybackSessionListener internalPlaybackSessionListener) {
            this.analyticsReporter = analyticsReporter;
            this.internalPlaybackSessionListener = internalPlaybackSessionListener;
        }

        public IAnalyticsReporter getAnalyticsReporter() {
            return analyticsReporter;
        }

        public IInternalPlaybackSessionListener getInternalPlaybackSessionListener() {
            return internalPlaybackSessionListener;
        }

        /** Returns an analytics reporter that does not send any data. */
        static Analytics silentAnalitycs() {
            return new Analytics(
                    new SilentAnalyticsReporter(), new IInternalPlaybackSessionListener() {
                public void onStart(OnStartArgs args) { }
                public void onStop(OnStopArgs args) {}
            });
        }
    }
}
