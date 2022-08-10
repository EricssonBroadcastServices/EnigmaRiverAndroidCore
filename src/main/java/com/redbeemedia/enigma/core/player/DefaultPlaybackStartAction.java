package com.redbeemedia.enigma.core.player;

import static com.redbeemedia.enigma.core.playrequest.MaterialProfile.PARAM_KEY;

import android.util.Log;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.ads.ExposureAdMetadata;
import com.redbeemedia.enigma.core.ads.IAdDetector;
import com.redbeemedia.enigma.core.ads.IAdInsertionFactory;
import com.redbeemedia.enigma.core.ads.IAdInsertionParameters;
import com.redbeemedia.enigma.core.ads.IAdResourceLoader;
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
import com.redbeemedia.enigma.core.http.SimpleHttpCall;
import com.redbeemedia.enigma.core.json.StringResponseHandler;
import com.redbeemedia.enigma.core.marker.IMarkerPointsDetector;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.playrequest.AdobePrimetime;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.MaterialProfile;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/*package-protected*/ class DefaultPlaybackStartAction implements IPlaybackStartAction, IPlayableHandler {
    private static final String TAG = "StartAction";
    public static final String SUPPORTED_FORMATS = "supportedFormats";
    public static final String SUPPORTED_DRMS = "supportedDrms";
    public static final String URL_SERVER_TIME_PROVIDER = "https://time.akamai.com/";

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
        if (!IN_PROGRESS.compareAndSet(false, true)) {
            Log.w("WARN","Called play asset multiple times in short span, ignoring latest call");
            return;
        }

        setDeviceUtcTimeDifference();
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
                    Log.w("WARN","Cannot access network");
                }
            }

            if(!timeProvider.isReady(Duration.seconds(30))) {
                IN_PROGRESS.set(false);
                getStartActionResultHandler().onError(new ServerTimeoutError("Could not start time service"));
                return;
            }
        }
        // make cue-points request
        contentAssetRequest(assetId);

        URL url;
        try {
            UrlPath path = session.getBusinessUnit().getApiBaseUrl("v2").append("entitlement").append(assetId).append("play");

            IAdInsertionParameters adInsertionParameters = buildAdInsertionParameters(playRequest);
            if (adInsertionParameters != null) {
                path = path.appendQueryStringParameters(adInsertionParameters.getParameters());
            }
            path = appendSupportedFormatsAndDRMs(path);

            MaterialProfile materialProfile = playRequest.getPlaybackProperties().getMaterialProfile();
            if (materialProfile != null) {
                path = path.append(PARAM_KEY + materialProfile.name());
            }

            url = path.toURL();

        } catch (MalformedURLException e) {
            IN_PROGRESS.set(false);
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
                IN_PROGRESS.set(false);
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
                try{
                String requestId = jsonObject.optString("requestId");
                String playToken = jsonObject.optString("playToken");
                JSONArray formats = jsonObject.getJSONArray("formats");
                JSONArray spritesJson = jsonObject.optJSONArray("sprites");
                boolean audioOnly = jsonObject.optBoolean("audioOnly",false);
                MediaType mediaType = audioOnly ? MediaType.AUDIO : MediaType.VIDEO;
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
                    JsonStreamInfo streamInfo = new JsonStreamInfo(jsonObject.optJSONObject("streamInfo"), mediaType);
                    EnigmaContractRestrictions contractRestrictions = EnigmaContractRestrictions.createWithDefaults(jsonObject.optJSONObject("contractRestrictions"));

                    JSONObject cdnObject = jsonObject.optJSONObject("cdn");
                    String cdnProvider = null;
                    if (cdnObject != null) {
                        cdnProvider = cdnObject.optString("provider", "");
                    }

                    IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(manifestUrl, cdnProvider, playbackSessionId);
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
                        adDetector.setLiveDelay(liveDelay);
                    } else if (!streamInfo.ssaiEnabled() && adDetector != null) {
                        adDetector.setEnabled(false);
                        adDetector.setLiveDelay(liveDelay);
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
                                nextStep.continueProcess(new StreamPrograms(epgResponse, streamInfo.isLiveStream(), deviceUtcTimeDifference));
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
                        String format = "";
                        boolean isDrmSupported = false;
                        for (int i = 0; i < formats.length(); ++i) {
                            JSONObject mediaFormat = formats.getJSONObject(i);
                            format = (String) mediaFormat.optString("format","null");
                            Object drm = mediaFormat.opt("drm");
                            if (drm != null) {
                                isDrmSupported = true;
                            }
                            break;
                        }
                        onError(new NoSupportedMediaFormatsError("Could not find a media format supported by the current player implementation. Requested format:" + format + ", isDRM:" + isDrmSupported));
                    }
                } finally {
                    IN_PROGRESS.set(false);
                }
            }
        });
    }


    private UrlPath appendSupportedFormatsAndDRMs(UrlPath path) {
        Set<EnigmaMediaFormat.StreamFormat> formats = new HashSet<>();
        Set<EnigmaMediaFormat.DrmTechnology> drms = new HashSet<>();
        for(EnigmaMediaFormat mediaFormat : supportedFormats){
            formats.add(mediaFormat.getStreamFormat());
            drms.add(mediaFormat.getDrmTechnology());
        }
        String formatsBuilder = buildFormats(formats);
        String drmBuilder = buildDrm(drms);
        Map<String, String> paramMap = new HashMap<>();
        if (!formatsBuilder.isEmpty()) {
            Log.w(TAG, "No supported formats");
            paramMap.put(SUPPORTED_FORMATS, formatsBuilder);
        }
        if (!drmBuilder.trim().isEmpty()) {
            paramMap.put(SUPPORTED_DRMS, drmBuilder);
        }
        if (!paramMap.isEmpty()) {
            path = path.appendQueryStringParameters(paramMap);
        }
        return path;
    }

    private void setDeviceUtcTimeDifference() {
        try {
            UrlPath url = new UrlPath(URL_SERVER_TIME_PROVIDER);
            SimpleHttpCall apiCall = new SimpleHttpCall("GET");
            EnigmaRiverContext.getHttpHandler().doHttp(url.toURL(), apiCall, new StringResponseHandler() {

                @Override
                public void onError(EnigmaError error) {
                    deviceUtcTimeDifference = 0L;
                }

                @Override
                public void onSuccess(String serverUtcTimeStr) {
                    try {
                        long serverUtcTime = Long.parseLong(serverUtcTimeStr) * 1000;
                        long deviceTime = new Date().getTime();
                        long networkTime = 500L;
                        deviceUtcTimeDifference = deviceTime - serverUtcTime - networkTime;
                    }catch (Exception e){
                        e.printStackTrace();
                        // ignore and set device time difference as 0
                        deviceUtcTimeDifference = 0L;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // IGNORE
        }
    }

    private String buildFormats(Set<EnigmaMediaFormat.StreamFormat> formats) {
        StringBuilder formatsBuilder = new StringBuilder();
        if(formats.contains(EnigmaMediaFormat.StreamFormat.DASH)){
            formatsBuilder.append("dash").append(",");
        }
        if(formats.contains(EnigmaMediaFormat.StreamFormat.HLS)){
            formatsBuilder.append("hls").append(",");
        }
        if(formats.contains(EnigmaMediaFormat.StreamFormat.MP3)){
            formatsBuilder.append("mp3").append(",");
        }
        if(formats.contains(EnigmaMediaFormat.StreamFormat.SMOOTHSTREAMING)){
            formatsBuilder.append("smoothstreaming").append(",");
        }
        return removeLastComma(formatsBuilder);
    }

    private String buildDrm(Set<EnigmaMediaFormat.DrmTechnology> drms) {
        StringBuilder drmBuilder = new StringBuilder();
        if(drms.contains(EnigmaMediaFormat.DrmTechnology.WIDEVINE)){
            drmBuilder.append("widevine").append(",");
        }
        if(drms.contains(EnigmaMediaFormat.DrmTechnology.PLAYREADY)){
            drmBuilder.append("playready").append(",");
        }
        if(drms.contains(EnigmaMediaFormat.DrmTechnology.FAIRPLAY)){
            drmBuilder.append("fairplay").append(",");
        }
        return removeLastComma(drmBuilder);
    }

    private String removeLastComma(StringBuilder drmBuilder) {
        String drmBuilderStr = drmBuilder.toString();
        if (drmBuilderStr.endsWith(",")) {
            drmBuilderStr = drmBuilderStr.substring(0, drmBuilderStr.length() - 1);
        }
        return drmBuilderStr;
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
        if(adInsertionFactory == null) {
            return null;
        }
        return adInsertionFactory.createParameters(playRequest);
    }

    @Override
    public void startUsingDownloadData(Object downloadData) {
        try {
            IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo("mockManifest.mpd",null, null);
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
                    playerImplementationControls.load(loadRequest, new StartPlaybackControlResultHandler(getStartActionResultHandler(), null, playRequest.getPlaybackProperties().getPlayFrom(), playerImplementationControls, adDetector) {
                        @Override
                        protected void onLogDebug(String message) {
                            Log.d(TAG, message);
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

        IPlaybackSessionInfo playbackSessionInfo = playerConnector.getPlaybackSessionInfo(url.toString(),null, null);
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
        return new AnalyticsReporter(timeProvider, analyticsHandler, deviceUtcTimeDifference);
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

        /**
         * Returns an analytics reporter that does not send any data.
         */
        static Analytics silentAnalitycs() {
            return new Analytics(
                    new SilentAnalyticsReporter(), new IInternalPlaybackSessionListener() {
                public void onStart(OnStartArgs args) { }
                public void onStop(OnStopArgs args) {}
            });
        }
    }
}

