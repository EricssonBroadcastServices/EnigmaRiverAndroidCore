package com.redbeemedia.enigma.core.player;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.redbeemedia.enigma.core.activity.AbstractActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.drm.DrmInfoFactory;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.drm.IDrmProvider;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat.DrmTechnology;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat.StreamFormat;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.player.controls.AbstractEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.BaseTimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.ITimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.timeline.TimelineListenerCollector;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.MainThreadTaskFactory;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.AndroidThreadUtil;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.IInternalCallbackObject;
import com.redbeemedia.enigma.core.util.IStateMachine;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.ProxyCallback;
import com.redbeemedia.enigma.core.util.RuntimeExceptionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EnigmaPlayer implements IEnigmaPlayer {
    private static final String TAG = "EnigmaPlayer";
    private static final EnigmaMediaFormat[] FORMAT_PREFERENCE_ORDER = new EnigmaMediaFormat[]{new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.WIDEVINE),
                                                                                               new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.NONE)};

    private ISession session;
    private IPlayerImplementation playerImplementation;
    private final EnigmaPlayerControls controls = new EnigmaPlayerControls();
    private final EnigmaPlayerTimeline timeline = new EnigmaPlayerTimeline();
    private EnigmaPlayerEnvironment environment = new EnigmaPlayerEnvironment();
    private IStateMachine<EnigmaPlayerState> stateMachine = EnigmaStateMachineFactory.create();
    private WeakReference<Activity> weakActivity = new WeakReference<>(null);
    private IActivityLifecycleListener activityLifecycleListener;
    private ITimeProvider timeProvider;
    private IHandler callbackHandler = null;

    private EnigmaPlayerCollector enigmaPlayerListeners = new EnigmaPlayerCollector();

    private final IPlaybackSessionFactory playbackSessionFactory;
    private final OpenContainer<IInternalPlaybackSession> currentPlaybackSession = new OpenContainer<>(null);
    private PlaybackSessionContainerCollector playbackSessionContainerCollector = new PlaybackSessionContainerCollector();
    private final OpenContainer<IPlaybackStartAction> currentPlaybackStartAction = new OpenContainer<>(null);

    public EnigmaPlayer(ISession session, IPlayerImplementation playerImplementation) {
        this.session = session;
        this.playerImplementation = playerImplementation;
        this.playerImplementation.install(environment);
        timeline.init();
        playbackSessionContainerCollector.addListener(environment.timelinePositionFactory);
        stateMachine.addListener((from, to) -> enigmaPlayerListeners.onStateChanged(from, to));
        environment.validateInstallation();
        this.activityLifecycleListener = new AbstractActivityLifecycleListener() {
            @Override
            public void onDestroy() {
                synchronized (currentPlaybackSession) {
                    if(currentPlaybackSession.value != null) {
                        currentPlaybackSession.value.onStop(EnigmaPlayer.this);
                    }
                    currentPlaybackSession.value = null;
                }
                playerImplementation.release();
                ((ServerTimeService) timeProvider).stop();
            }
        };
        this.timeProvider = newTimeProvider(session);
        this.playbackSessionFactory = new DefaultPlaybackSessionFactory(timeProvider);

        environment.fireEnigmaPlayerReady(this);
    }

    protected ITimeProvider newTimeProvider(ISession session) {
        ServerTimeService serverTimeService = new ServerTimeService(session, EnigmaRiverContext.getTaskFactory());
        serverTimeService.start(true);
        return serverTimeService;
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
        IPlaybackProperties playbackProperties = playRequest.getPlaybackProperties();
        IPlayResultHandler resultHandler = playRequest.getResultHandler();
        playable.useWith(new PlayableHandler(playbackProperties, playable, resultHandler));
    }

    @Override
    public boolean addListener(IEnigmaPlayerListener playerListener) {
        return enigmaPlayerListeners.addListener(playerListener);
    }

    @Override
    public boolean addListener(IEnigmaPlayerListener playerListener, Handler handler) {
        return addListener(playerListener, new HandlerWrapper(handler));
    }

    protected boolean addListener(IEnigmaPlayerListener playerListener, IHandler handler) {
        return enigmaPlayerListeners.addListener(playerListener, handler);
    }

    @Override
    public boolean removeListener(IEnigmaPlayerListener playerListener) {
        return enigmaPlayerListeners.removeListener(playerListener);
    }

    @Override
    public EnigmaPlayer setCallbackHandler(Handler handler) {
        return setCallbackHandler(new HandlerWrapper(handler));
    }

    @Override
    public EnigmaPlayer setCallbackHandler(IHandler handler) {
        this.callbackHandler = handler;
        return this;
    }

    private <T extends IInternalCallbackObject> T useCallbackHandlerIfPresent(Class<T> callbackInterface, T callback) {
        if(callbackHandler != null) {
            return ProxyCallback.createCallbackOnThread(callbackHandler, callbackInterface, callback);
        } else {
            return callback;
        }
    }

    @Override
    public IEnigmaPlayerControls getControls() {
        return controls;
    }

    @Override
    public ITimeline getTimeline() {
        return timeline;
    }

    @Override
    public EnigmaPlayerState getState() {
        return stateMachine.getState();
    }

    private class PlayableHandler implements IPlayableHandler {
        private IPlaybackProperties playbackProperties;
        private final IPlayable playable;
        private IPlayResultHandler playResultHandler;

        public PlayableHandler(IPlaybackProperties playbackProperties, IPlayable playable,IPlayResultHandler playResultHandler) {
            this.playbackProperties = playbackProperties;
            this.playable = playable;
            this.playResultHandler = useCallbackHandlerIfPresent(IPlayResultHandler.class, playResultHandler);
        }

        @Override
        public void startUsingAssetId(String assetId) {
            IPlaybackStartAction playbackStartAction;
            synchronized (currentPlaybackStartAction) {
                if(currentPlaybackStartAction.value != null) {
                    currentPlaybackStartAction.value.cancel();
                }
                currentPlaybackStartAction.value = new PlaybackStartAction(playbackProperties.getPlayFrom(), playable, playResultHandler);
                playbackStartAction = currentPlaybackStartAction.value;
            }
            playbackStartAction.startUsingAssetId(playbackProperties, playResultHandler, assetId);
        }


        @Override
        public void startUsingUrl(URL url) {
            environment.playerImplementationControls.load(url.toString(), new PlayResultControlResultHandler(playResultHandler) {
                @Override
                public void onDone() {
                    try {
                        environment.playerImplementationControls.start(new PlayResultControlResultHandler(playResultHandler) {
                            @Override
                            public void onDone() {
                                playResultHandler.onStarted(null); //TODO provide PlaybackSession
                            }
                        });
                    } catch (RuntimeException e) {
                        playResultHandler.onError(new UnexpectedError(e));
                    }
                }
            });
        }

        private class PlayResultControlResultHandler extends BasePlayerImplementationControlResultHandler {
            private IPlayResultHandler playResultHandler;

            public PlayResultControlResultHandler(IPlayResultHandler playResultHandler) {
                this.playResultHandler = playResultHandler;
            }

            @Override
            public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
                this.playResultHandler.onError(new UnexpectedError("Rejected"));
            }

            @Override
            public void onError(Error error) {
                playResultHandler.onError(error);
            }
        }
    }

    private interface IPlaybackStartAction {
        void startUsingAssetId(IPlaybackProperties playbackProperties, IPlayResultHandler playResultHandler, String assetId);
        IPlaybackProperties.PlayFrom getPlayFrom();
        void onStarted(IInternalPlaybackSession playbackSession);
        void cancel();
    }

    private class PlaybackStartAction implements IPlaybackStartAction {
        private final IPlaybackProperties.PlayFrom playFrom;
        private final IPlayable playable;
        private final IPlayResultHandler playResultHandler;

        public PlaybackStartAction(IPlaybackProperties.PlayFrom playFrom, IPlayable playable, IPlayResultHandler playResultHandler) {
            this.playFrom = playFrom;
            this.playable = playable;
            this.playResultHandler = playResultHandler;
        }

        @Override
        public void startUsingAssetId(IPlaybackProperties playbackProperties, IPlayResultHandler playResultHandler, final String assetId) {
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
                    stateMachine.setState(EnigmaPlayerState.IDLE);
                }

                @Override
                protected void onSuccess(JSONObject jsonObject) throws JSONException {
                    String requestId = jsonObject.optString("requestId");
                    String playToken = jsonObject.optString("playToken");
                    JSONArray formats = jsonObject.getJSONArray("formats");
                    JSONObject usableMediaFormat = getUsableMediaFormat(formats, environment.formatSupportSpec);
                    if (usableMediaFormat != null) {
                        JSONObject drms = usableMediaFormat.optJSONObject("drm");
                        if (drms != null) {
                            JSONObject drmTypeInfo = drms.optJSONObject(DrmTechnology.WIDEVINE.getKey());
                            String licenseUrl = drmTypeInfo.getString("licenseServerUrl");
                            IDrmInfo drmInfo = DrmInfoFactory.createWidevineDrmInfo(licenseUrl, playToken, requestId);
                            environment.setDrmInfo(drmInfo);
                        }
                        String manifestUrl = usableMediaFormat.getString("mediaLocator");
                        IPlaybackTechnologyIdentifier technologyIdentifier = environment.playerImplementationInternals.getTechnologyIdentifier();
                        IPlaybackSessionInfo playbackSessionInfo = new PlaybackSessionInfo(playable, assetId, manifestUrl, technologyIdentifier);
                        replacePlaybackSession(playbackSessionFactory.createPlaybackSession(new IPlaybackSessionFactory.PlaybackSessionArgs(session, jsonObject, playbackSessionInfo)));
                        stateMachine.setState(EnigmaPlayerState.LOADING);
                        environment.playerImplementationControls.load(manifestUrl, new StartPlaybackControlResultHandler(playResultHandler, jsonObject, playbackProperties.getPlayFrom(), environment.playerImplementationControls) {
                            @Override
                            protected void onLogDebug(String message) {
                                Log.d(TAG, message);
                            }
                        });
                    } else {
                        onError(new NoSupportedMediaFormatsError("Could not find a media format supported by the current player implementation."));
                    }
                }
            });
        }

        @Override
        public IPlaybackProperties.PlayFrom getPlayFrom() {
            return playFrom;
        }

        @Override
        public void onStarted(IInternalPlaybackSession playbackSession) {
            updatePlayingFromLive();
            playResultHandler.onStarted(playbackSession);
        }

        @Override
        public void cancel() {
        }
    }


    private void replacePlaybackSession(IInternalPlaybackSession playbackSession) {
        synchronized (currentPlaybackSession) {
            IInternalPlaybackSession oldSession = this.currentPlaybackSession.value;
            if(this.currentPlaybackSession.value != null) {
                this.currentPlaybackSession.value.onStop(this);
            }
            this.currentPlaybackSession.value = playbackSession;
            playbackSessionContainerCollector.onPlaybackSessionChanged(oldSession, playbackSession);
            enigmaPlayerListeners.onPlaybackSessionChanged(oldSession, playbackSession);
            this.currentPlaybackSession.value.onStart(this);
        }
    }

    private void setPlayingFromLive(boolean live) {
        synchronized (currentPlaybackSession) {
            if(currentPlaybackSession.value != null) {
                currentPlaybackSession.value.setPlayingFromLive(live);
            }
        }
    }

    private void updatePlayingFromLive() {
        ITimelinePosition timelinePosition = environment.playerImplementationInternals.getCurrentPosition();
        ITimelinePosition endPos = environment.playerImplementationInternals.getCurrentEndBound();
        if(timelinePosition != null && endPos != null) {
            long seconds = endPos.subtract(timelinePosition).inWholeUnits(Duration.Unit.SECONDS);
            setPlayingFromLive(seconds < 60 && stateMachine.getState() == EnigmaPlayerState.PLAYING);
        } else {
            setPlayingFromLive(false);
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
        private DefaultTimelinePositionFactory timelinePositionFactory = new DefaultTimelinePositionFactory();
        private IPlayerImplementationControls playerImplementationControls;
        private IPlayerImplementationInternals playerImplementationInternals;
        private IPlayerImplementationListener playerImplementationListener;
        private List<IEnigmaPlayerReadyListener> playerReadyListeners = new ArrayList<>();

        @Override
        public IDrmProvider getDrmProvider() {
            return this;
        }

        @Override
        public void setMediaFormatSupportSpec(IMediaFormatSupportSpec formatSupportSpec) {
            this.formatSupportSpec = formatSupportSpec;
        }

        @Override
        public void setControls(IPlayerImplementationControls controls) {
            this.playerImplementationControls = controls;
        }

        @Override
        public void setInternals(IPlayerImplementationInternals internals) {
            this.playerImplementationInternals = internals;
        }

        @Override
        public ITimelinePositionFactory getTimelinePositionFactory() {
            return timelinePositionFactory;
        }

        @Override
        public IPlayerImplementationListener getPlayerImplementationListener() {
            synchronized (this) {
                if(playerImplementationListener == null) {
                    playerImplementationListener = new IPlayerImplementationListener() {
                        @Override
                        public void onError(Error error) {
                            enigmaPlayerListeners.onPlaybackError(error);
                            stateMachine.setState(EnigmaPlayerState.IDLE);
                        }

                        @Override
                        public void onLoadCompleted() {
                            synchronized (currentPlaybackStartAction) {
                                if(currentPlaybackStartAction.value != null) {
                                    environment.playerImplementationControls.start(new BasePlayerImplementationControlResultHandler());
                                    stateMachine.setState(EnigmaPlayerState.LOADED);
                                }
                            }
                        }

                        @Override
                        public void onPlaybackStarted() {
                            synchronized (currentPlaybackStartAction) {
                                if(currentPlaybackStartAction.value != null) {
                                    currentPlaybackStartAction.value.onStarted(currentPlaybackSession.value);
                                    currentPlaybackStartAction.value = null;
                                }
                            }
                            stateMachine.setState(EnigmaPlayerState.PLAYING);
                        }

                        @Override
                        public void onPlaybackPaused() {
                            if(stateMachine.getState() == EnigmaPlayerState.PLAYING) {
                                stateMachine.setState(EnigmaPlayerState.PAUSED);
                            }
                        }

                        @Override
                        public void onTimelineBoundsChanged(ITimelinePosition start, ITimelinePosition end) {
                            timeline.onStreamTimelineBoundsChanged(start, end);
                        }

                        @Override
                        public void onPositionChanged() {
                            timeline.onPositionNeedsUpdating();
                        }

                        @Override
                        public void onStreamEnded() {
                            synchronized (currentPlaybackSession) {
                                if(currentPlaybackSession.value != null) {
                                    currentPlaybackSession.value.fireEndReached();
                                }
                            }
                            stateMachine.setState(EnigmaPlayerState.LOADED);
                        }

                        @Override
                        public void onTracksChanged(Collection<? extends IPlayerImplementationTrack> tracks) {
                            synchronized (currentPlaybackSession) {
                                if(currentPlaybackSession.value != null) {
                                    currentPlaybackSession.value.setTracks(tracks);
                                }
                            }
                        }
                    };
                }
                return playerImplementationListener;
            }
        }

        public void setDrmInfo(IDrmInfo drmInfo) {
            this.drmInfo = drmInfo;
        }

        @Override
        public IDrmInfo getDrmInfo() {
            return drmInfo;
        }

        public void validateInstallation() {
            if(playerImplementationControls == null) {
                throw new IllegalStateException("PlayerImplementation did not provide controls!");
            }
            if(playerImplementationInternals == null) {
                throw new IllegalStateException("PlayerImplementation did not provide internals!");
            }
        }

        @Override
        public void addEnigmaPlayerReadyListener(IEnigmaPlayerReadyListener listener) {
            synchronized (playerReadyListeners) {
                playerReadyListeners.add(listener);
            }
        }

        public void fireEnigmaPlayerReady(IEnigmaPlayer enigmaPlayer) {
            RuntimeExceptionHandler exceptionHandler = new RuntimeExceptionHandler();
            synchronized (playerReadyListeners) {
                exceptionHandler.catchExceptions(playerReadyListeners, listener -> {
                        listener.onReady(enigmaPlayer);
                });
                playerReadyListeners.clear();
                playerReadyListeners = null;
            }
            exceptionHandler.rethrowIfAnyExceptions();
        }
    }

    private static class DefaultFormatSupportSpec implements IMediaFormatSupportSpec {
        @Override
        public boolean supports(EnigmaMediaFormat enigmaMediaFormat) {
            return enigmaMediaFormat != null && enigmaMediaFormat.equals(StreamFormat.DASH, DrmTechnology.NONE);
        }
    }

    private class EnigmaPlayerControls extends AbstractEnigmaPlayerControls {
        private ControlResultHandlerAdapter wrapResultHandler(IControlResultHandler resultHandler) {
            return new ControlResultHandlerAdapter(useCallbackHandlerIfPresent(IControlResultHandler.class, resultHandler));
        }

        @Override
        protected IControlResultHandler getDefaultResultHandler() {
            return new DefaultControlResultHandler(TAG);
        }

        private boolean seekAllowed() {
            synchronized (currentPlaybackSession) {
                return currentPlaybackSession.value != null && currentPlaybackSession.value.isSeekAllowed();
            }
        }

        private boolean seekToLiveAllowed() {
            synchronized (currentPlaybackSession) {
                return currentPlaybackSession.value != null && currentPlaybackSession.value.isSeekToLiveAllowed();
            }
        }

        @Override
        public void start(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            environment.playerImplementationControls.start(controlResultHandler);
            controlResultHandler.runWhenDone(() -> {
                stateMachine.setState(EnigmaPlayerState.PLAYING);
                updatePlayingFromLive();
            });
        }

        @Override
        public void pause(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            environment.playerImplementationControls.pause(controlResultHandler);
            controlResultHandler.runWhenDone(() -> {
                stateMachine.setState(EnigmaPlayerState.PAUSED);
                setPlayingFromLive(false);
            });
        }

        @Override
        public void stop(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            environment.playerImplementationControls.stop(controlResultHandler);
            controlResultHandler.runWhenDone(() -> {
                stateMachine.setState(EnigmaPlayerState.IDLE);
                setPlayingFromLive(false);
            });
        }

        @Override
        public void seekTo(long millis, IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            if(seekAllowed()) {
                environment.playerImplementationControls.seekTo(new IPlayerImplementationControls.TimelineRelativePosition(millis), controlResultHandler);
                controlResultHandler.runWhenDone(() -> updatePlayingFromLive());
            } else {
                controlResultHandler.onRejected(RejectReason.contractRestriction("Seek not allowed"));
            }
        }

        @Override
        public void seekTo(StreamPosition streamPosition, IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            if(streamPosition == StreamPosition.START) {
                if(seekAllowed()) {
                    environment.playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.TIMELINE_START, controlResultHandler);
                    controlResultHandler.runWhenDone(() -> updatePlayingFromLive());
                } else {
                    controlResultHandler.onRejected(RejectReason.contractRestriction("Seek not allowed"));
                }
            } else if(streamPosition == StreamPosition.LIVE_EDGE) {
                if(seekToLiveAllowed()) {
                    environment.playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.LIVE_EDGE, controlResultHandler);
                    controlResultHandler.runWhenDone(() -> setPlayingFromLive(true));
                } else {
                    if(seekAllowed()) {
                        controlResultHandler.onRejected(RejectReason.inapplicable("Seek to live not allowed"));
                    } else {
                        controlResultHandler.onRejected(RejectReason.contractRestriction("Seek not allowed"));
                    }
                }
            } else {
                controlResultHandler.onRejected(RejectReason.illegal("Unknown "+StreamPosition.class.getSimpleName()+" \""+streamPosition+"\""));
            }
        }

        @Override
        public void setVolume(float volume, IControlResultHandler resultHandler) {
            environment.playerImplementationControls.setVolume(volume, wrapResultHandler(resultHandler));
        }

        @Override
        public void setSubtitleTrack(final ISubtitleTrack track, IControlResultHandler resultHandler) {
            environment.playerImplementationControls.setSubtitleTrack(track, wrapResultHandler(resultHandler).runWhenDone(new Runnable() {
                @Override
                public void run() {
                    synchronized (currentPlaybackSession) {
                        if(currentPlaybackSession.value != null) {
                            currentPlaybackSession.value.setSelectedSubtitleTrack(track);
                        }
                    }
                }
            }));
        }
    }

    private class EnigmaPlayerTimeline implements ITimeline {
        private TimelineListenerCollector collector = new TimelineListenerCollector();
        private boolean lastVisibleValue = false;
        private Repeater repeater;
        private ITimelinePosition streamTimelineStart = null;
        private ITimelinePosition streamTimelineEnd = null;
        private ITimelinePosition streamTimelinePosition = null;
        private long currentStreamOffset = 0;
        private boolean hasProgram = false;
        private final ProgramTracker programTracker = new ProgramTracker().addListener(new ProgramTracker.IProgramChangedListener() {
            @Override
            public void onProgramChanged(IProgram oldProgram, IProgram newProgram) {
                if(newProgram != null) {
                    IInternalPlaybackSession playbackSession;
                    synchronized (currentPlaybackSession) {
                        playbackSession = currentPlaybackSession.value;
                    }
                    if(playbackSession != null) {
                        StreamInfo streamInfo = playbackSession.getStreamInfo();
                        if(streamInfo.hasStartUtcSeconds()) {
                            long streamStartUtcMillis = streamInfo.getStartUtcSeconds()*1000L;
                            ITimelinePositionFactory positionFactory = environment.timelinePositionFactory;
                            long startOffset = newProgram.getStartUtcMillis()-streamStartUtcMillis;
                            long endOffset = newProgram.getEndUtcMillis()-streamStartUtcMillis;
                            ITimelinePosition start = positionFactory.newPosition(startOffset);
                            ITimelinePosition end = positionFactory.newPosition(endOffset);
                            EnigmaPlayerTimeline.this.onExposedTimelineBoundsChanged(start, end);
                            hasProgram = true;
                        }
                    }
                } else {
                    hasProgram = false;
                    EnigmaPlayerTimeline.this.onExposedTimelineBoundsChanged(streamTimelineStart, streamTimelineEnd);
                }
            }
        });

        public EnigmaPlayerTimeline() {
            ITaskFactory mainThreadTaskFactory = new MainThreadTaskFactory();
            repeater = new Repeater(mainThreadTaskFactory, 1000 / 30, () -> collector.onCurrentPositionChanged(environment.playerImplementationInternals.getCurrentPosition()));
            collector.addListener(new BaseTimelineListener() {
                @Override
                public void onVisibilityChanged(boolean visible) {
                    lastVisibleValue = visible;
                }
            });
        }

        public void init() {
            programTracker.addListener(environment.timelinePositionFactory);
            programTracker.init(EnigmaPlayer.this);
            collector.addListener(new BaseTimelineListener() {
                @Override
                public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
                    streamTimelinePosition = timelinePosition;
                    updateStreamOffset();
                }
            });
            EnigmaPlayer.this.addListener(new BaseEnigmaPlayerListener() {
                @Override
                public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
                    if(to == EnigmaPlayerState.PLAYING) {
                        repeater.setEnabled(true);
                    } else if(from == EnigmaPlayerState.PLAYING) {
                        repeater.setEnabled(false);
                    }
                    if(to == EnigmaPlayerState.LOADED) {
                        collector.onVisibilityChanged(true);
                    } else if(to == EnigmaPlayerState.IDLE || to == EnigmaPlayerState.LOADING) {
                        collector.onVisibilityChanged(false);
                    }
                }
            });
        }

        @Override
        public ITimelinePosition getCurrentPosition() {
            return environment.playerImplementationInternals.getCurrentPosition();
        }

        @Override
        public ITimelinePosition getCurrentEndBound() {
            return environment.playerImplementationInternals.getCurrentEndBound();
        }

        @Override
        public ITimelinePosition getCurrentStartBound() {
            return environment.playerImplementationInternals.getCurrentStartBound();
        }

        @Override
        public boolean getVisibility() {
            return lastVisibleValue;
        }

        @Override
        public void addListener(ITimelineListener listener) {
            collector.addListener(listener);
        }

        @Override
        public void addListener(ITimelineListener listener, Handler handler) {
            collector.addListener(listener, new HandlerWrapper(handler));
        }

        @Override
        public void removeListener(ITimelineListener listener) {
            collector.removeListener(listener);
        }

        public void onStreamTimelineBoundsChanged(ITimelinePosition start, ITimelinePosition end) {
            streamTimelineStart = start;
            streamTimelineEnd = end;
            updateStreamOffset();
            if(!hasProgram) {
                EnigmaPlayerTimeline.this.onExposedTimelineBoundsChanged(streamTimelineStart, streamTimelineEnd);
            }
            updatePlayingFromLive();
        }

        private void updateStreamOffset() {
            if(streamTimelineStart != null && streamTimelinePosition != null) {
                long oldOffset = currentStreamOffset;
                currentStreamOffset = streamTimelinePosition.subtract(streamTimelineStart).inWholeUnits(Duration.Unit.MILLISECONDS);
                if(oldOffset != currentStreamOffset) {
                    programTracker.onOffsetChanged(currentStreamOffset);
                }
            }
        }

        public void onExposedTimelineBoundsChanged(ITimelinePosition start, ITimelinePosition end) {
            AndroidThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    collector.onBoundsChanged(start, end);
                    repeater.executeNow();
                }
            });
        }

        public void onPositionNeedsUpdating() {
            repeater.executeNow();
        }
    }

    private class PlaybackSessionInfo implements IPlaybackSessionInfo {
        private final IPlayable playable;
        private final String assetId;
        private final String mediaLocator;
        private final IPlaybackTechnologyIdentifier tech;

        public PlaybackSessionInfo(IPlayable playable, String assetId, String mediaLocator, IPlaybackTechnologyIdentifier tech) {
            this.playable = playable;
            this.assetId = assetId;
            this.mediaLocator = mediaLocator;
            this.tech = tech;
        }

        @Override
        public Duration getCurrentPlaybackOffset() {
            ITimelinePosition currentPosition = environment.playerImplementationInternals.getCurrentPosition();
            ITimelinePosition startBound = environment.playerImplementationInternals.getCurrentStartBound();
            return currentPosition.subtract(startBound);
        }

        @Override
        public String getPlayerTechnologyName() {
            return tech.getTechnologyName();
        }

        @Override
        public String getPlayerTechnologyVersion() {
            return tech.getTechnologyVersion();
        }

        @Override
        public String getAssetId() {
            return assetId;
        }

        @Override
        public IPlayable getPlayable() {
            return playable;
        }

        @Override
        public String getMediaLocator() {
            return mediaLocator;
        }
    }
}