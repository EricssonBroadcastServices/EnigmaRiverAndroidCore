package com.redbeemedia.enigma.core.player;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.activity.AbstractActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.ads.AdBreak;
import com.redbeemedia.enigma.core.ads.AdDetector;
import com.redbeemedia.enigma.core.ads.AdEventType;
import com.redbeemedia.enigma.core.ads.AdIncludedTimeline;
import com.redbeemedia.enigma.core.ads.IAd;
import com.redbeemedia.enigma.core.ads.IAdDetector;
import com.redbeemedia.enigma.core.ads.IAdIncludedTimeline;
import com.redbeemedia.enigma.core.ads.IAdResourceLoader;
import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.drm.IDrmProvider;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.ChainedMediaFormatSelector;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat.DrmTechnology;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat.StreamFormat;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormatUtil;
import com.redbeemedia.enigma.core.format.IMediaFormatSelector;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;
import com.redbeemedia.enigma.core.format.SimpleMediaFormatSelector;
import com.redbeemedia.enigma.core.lifecycle.BaseLifecycleListener;
import com.redbeemedia.enigma.core.lifecycle.Lifecycle;
import com.redbeemedia.enigma.core.marker.IMarkerPointsDetector;
import com.redbeemedia.enigma.core.marker.MarkerPointsDetector;
import com.redbeemedia.enigma.core.playable.IAssetPlayable;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.controls.AbstractEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.BaseTimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.ITimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.timeline.SimpleTimeline;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.playrequest.BasePlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.AndroidThreadUtil;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.IStateMachine;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;
import com.redbeemedia.enigma.core.util.ProxyCallback;
import com.redbeemedia.enigma.core.util.RuntimeExceptionHandler;
import com.redbeemedia.enigma.core.video.ISpriteRepository;
import com.redbeemedia.enigma.core.video.IVideoTrack;
import com.redbeemedia.enigma.core.video.SpriteRepository;
import com.redbeemedia.enigma.core.virtualui.IVirtualControls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class EnigmaPlayer implements IEnigmaPlayer {
    private static final String TAG = "EnigmaPlayer";

    private static final IMediaFormatSelector DEFAULT_MEDIA_FORMAT_SELECTOR = new SimpleMediaFormatSelector(EnigmaMediaFormat.DASH().widevine(),
            EnigmaMediaFormat.DASH().unenc(),
            EnigmaMediaFormat.HLS().fairplay(),
            EnigmaMediaFormat.HLS().unenc(),
            EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
            EnigmaMediaFormat.MP3().unenc());
    private IMediaFormatSelector mediaFormatSelector = null;

    private final EnigmaPlayerLifecycle lifecycle = new EnigmaPlayerLifecycle();
    private final OpenContainer<ISession> defaultSession;
    private final OpenContainer<IBusinessUnit> businessUnit;
    private IPlayerImplementation playerImplementation;
    private final EnigmaPlayerControls controls = new EnigmaPlayerControls();
    private final EnigmaPlayerTimeline timeline = new EnigmaPlayerTimeline(lifecycle);
    private EnigmaPlayerEnvironment environment = new EnigmaPlayerEnvironment();
    private IStateMachine<EnigmaPlayerState> stateMachine = EnigmaStateMachineFactory.create();
    private WeakReference<Activity> weakActivity = new WeakReference<>(null);
    private IActivityLifecycleListener activityLifecycleListener;
    private ITimeProvider timeProvider;
    private IHandler callbackHandler = null;
    private final OpenContainer<IAdDetector> adsDetector;
    private final OpenContainer<IMarkerPointsDetector> markerPointsDetector;
    private ISpriteRepository spriteRepository;
    private boolean isReplacingPlaybackSession = false;
    private IVirtualControls virtualControls;

    private EnigmaPlayerCollector enigmaPlayerListeners = new EnigmaPlayerCollector();

    private final InternalEnigmaPlayerCommunicationsChannel communicationsChannel = new InternalEnigmaPlayerCommunicationsChannel();
    private final OpenContainer<IInternalPlaybackSession> currentPlaybackSession = new OpenContainer<>(null);
    private final OpenContainer<PlaybackSessionSeed> playbackSessionSeed = new OpenContainer<>(null);
    private PlaybackSessionContainerCollector playbackSessionContainerCollector = new PlaybackSessionContainerCollector();
    private final OpenContainer<IPlaybackStartAction> currentPlaybackStartAction = new OpenContainer<>(null);

    private volatile boolean released = false;
    private volatile boolean isSeekBusy = false;

    /**
     * @param session              the default session to use for PlayRequest
     * @param playerImplementation
     * @deprecated Use {@link #EnigmaPlayer(IBusinessUnit, IPlayerImplementation)} instead
     * and supply the session in the
     * {@link com.redbeemedia.enigma.core.playrequest.PlayRequest PlayRequest} constructor.
     *
     * @param session the default session to use for PlayRequest
     * @param playerImplementation
     */
    @Deprecated
    public EnigmaPlayer(ISession session, IPlayerImplementation playerImplementation) {
        this(session, session != null ? session.getBusinessUnit() : null, playerImplementation);
    }

    public EnigmaPlayer(IBusinessUnit businessUnit, IPlayerImplementation playerImplementation) {
        this(null, businessUnit, playerImplementation);
    }

    private EnigmaPlayer(ISession session, IBusinessUnit initialBusinessUnit, IPlayerImplementation playerImplementation) {
        if(initialBusinessUnit == null) {
            throw new IllegalArgumentException("No business unit provided");
        }
        this.defaultSession = new OpenContainer<>(session);
        this.businessUnit = new OpenContainer<>(initialBusinessUnit);
        AdDetector adDetector = new AdDetector(EnigmaRiverContext.getHttpHandler(), timeline, environment.timelinePositionFactory);
        this.adsDetector = new OpenContainer<>(adDetector);
        MarkerPointsDetector markerPointsDetector = new MarkerPointsDetector(timeline);
        this.markerPointsDetector = new OpenContainer<>(markerPointsDetector);
        this.playerImplementation = playerImplementation;
        this.playerImplementation.install(environment);
        this.spriteRepository = new SpriteRepository(environment.timelinePositionFactory, EnigmaRiverContext.getHttpHandler());
        playbackSessionContainerCollector.addListener(environment.timelinePositionFactory);
        playbackSessionContainerCollector.addListener(timeline);
        stateMachine.addListener((from, to) -> enigmaPlayerListeners.onStateChanged(from, to));
        environment.validateInstallation();
        this.activityLifecycleListener = new AbstractActivityLifecycleListener() {
            @Override
            public void onDestroy() {
                EnigmaPlayer.this.release();
            }
        };

        //It would be nice to store ServerTimeService in an EnigmaPlayerContext instead..! Somewhere down the line...
        this.timeProvider = newTimeProvider(OpenContainerUtil.getValueSynchronized(businessUnit), lifecycle);

        lifecycle.fireOnStart(null);
        environment.fireEnigmaPlayerReady(this);
    }

    protected ITimeProvider newTimeProvider(IBusinessUnit businessUnit, EnigmaPlayerLifecycle lifecycle) {
        return new ServerTimeService(businessUnit, EnigmaRiverContext.getTaskFactoryProvider().getTaskFactory(), lifecycle);
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
        IPlaybackStartAction playbackStartAction;
        synchronized (currentPlaybackStartAction) {
            if(currentPlaybackStartAction.value != null) {
                // Idea: Here we could use a policy to decide if startActions should be cancelled or
                //       if we should reject the new PlayRequest or maybe queue it.
                currentPlaybackStartAction.value.cancel();
            }

            currentPlaybackStartAction.value = newPlaybackStartAction(
                    OpenContainerUtil.getValueSynchronized(defaultSession),
                    OpenContainerUtil.getValueSynchronized(businessUnit),
                    timeProvider,
                    playRequest,
                    callbackHandler,
                    getTaskFactoryProvider(),
                    environment.playerImplementationControls,
                    newStartActionPlayerConnection(playRequest),
                    spriteRepository);

            currentPlaybackStartAction.value.setAdDetector(adsDetector.value);
            currentPlaybackStartAction.value.setMarkerPointsDetector(markerPointsDetector.value);
            playbackStartAction = currentPlaybackStartAction.value;
        }
        playbackStartAction.start();
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

    @Override
    public void setDefaultSession(ISession session) {
        OpenContainerUtil.setValueSynchronized(defaultSession, session, (oldValue, newValue) -> {
            if(newValue != null) {
                OpenContainerUtil.setValueSynchronized(businessUnit, newValue.getBusinessUnit(), null);
            }
        });
    }

    @Override
    public boolean isLiveStream() {
        IInternalPlaybackSession valueSynchronized = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
        if (valueSynchronized != null) {
            return valueSynchronized.getStreamInfo().isLiveStream();
        } else {
            return false;
        }
    }

    @Override
    public void release() {
        if(released) {
            return;
        }

        released = true;

        RuntimeExceptionHandler exceptionHandler = new RuntimeExceptionHandler();
        exceptionHandler.catchExceptions(() -> {
            replacePlaybackSession(null);
        });
        exceptionHandler.catchExceptions(() -> playerImplementation.release());
        exceptionHandler.catchExceptions(() -> lifecycle.fireOnStop(null));
        exceptionHandler.catchExceptions(() -> spriteRepository.clear());

        exceptionHandler.rethrowIfAnyExceptions();
    }

    public EnigmaPlayer setMediaFormatSelector(IMediaFormatSelector mediaFormatSelector) {
        this.mediaFormatSelector = mediaFormatSelector;
        return this;
    }

    public EnigmaPlayer setMediaFormatPreference(EnigmaMediaFormat... mediaFormatPreference) {
        return setMediaFormatSelector(new SimpleMediaFormatSelector(mediaFormatPreference));
    }

    @Override
    public IEnigmaPlayerControls getControls() {
        return controls;
    }

    @Override
    public ITimeline getTimeline() {
        return adsDetector.value.getTimeline();
    }

    @Override
    public EnigmaPlayerState getState() {
        return stateMachine.getState();
    }

    @Override
    public IAdDetector getAdDetector() {
        return adsDetector.value;
    }

    @Override
    public IMarkerPointsDetector getMarkerPointsDetector() {
        return markerPointsDetector.value;
    }

    private IPlaybackStartAction.IEnigmaPlayerCallbacks newStartActionPlayerConnection(final IPlayRequest playRequest) {
        return new IPlaybackStartAction.IEnigmaPlayerCallbacks() {
            @Override
            public void deliverPlaybackSession(IInternalPlaybackSession internalPlaybackSession) {
                replacePlaybackSession(internalPlaybackSession);
            }

            @Override
            public void setStateIfCurrentStartAction(IPlaybackStartAction action, EnigmaPlayerState newState) {
                synchronized (currentPlaybackStartAction) {
                    if(currentPlaybackStartAction.value == action) {
                        stateMachine.setState(newState);
                    }
                }
            }

            @Override
            public JSONObject getUsableMediaFormat(JSONArray formats) throws JSONException {
                IMediaFormatSelector selector = new ChainedMediaFormatSelector(
                        DEFAULT_MEDIA_FORMAT_SELECTOR,
                        mediaFormatSelector,
                        playRequest.getPlaybackProperties().getMediaFormatSelector());
                return EnigmaMediaFormatUtil.selectUsableMediaFormat(formats, environment.formatSupportSpec, selector);
            }

            @Override
            public IPlaybackSessionInfo getPlaybackSessionInfo(String manifestUrl) {
                IPlaybackTechnologyIdentifier technologyIdentifier = environment.playerImplementationInternals.getTechnologyIdentifier();
                String assetId = "N/A";
                IPlayable playable = playRequest.getPlayable();
                if(playable instanceof IAssetPlayable) {
                    assetId = ((IAssetPlayable) playable).getAssetId();
                }
                return new EnigmaPlayer.PlaybackSessionInfo(playRequest.getPlayable(), assetId, manifestUrl, technologyIdentifier, playRequest.getPlaybackProperties());
            }
        };
    }

    protected IPlaybackStartAction newPlaybackStartAction(ISession session,
                                                          IBusinessUnit businessUnit,
                                                          ITimeProvider timeProvider,
                                                          IPlayRequest playRequest,
                                                          IHandler callbackHandler,
                                                          ITaskFactoryProvider taskFactoryProvider,
                                                          IPlayerImplementationControls playerImplementationControls,
                                                          IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection,
                                                          ISpriteRepository spriteRepository) {
        return new DefaultPlaybackStartAction(session, businessUnit, timeProvider, playRequest, callbackHandler,taskFactoryProvider, playerImplementationControls, playerConnection, spriteRepository, environment.formatSupportSpec.getSupportedFormats());
    }

    protected ITaskFactoryProvider getTaskFactoryProvider() {
        return EnigmaRiverContext.getTaskFactoryProvider();
    }

    private void replacePlaybackSession(IInternalPlaybackSession playbackSession) {
        isReplacingPlaybackSession = true;
        IInternalPlaybackSession oldSession = null;
        try {

            synchronized (currentPlaybackSession) {
                oldSession = this.currentPlaybackSession.value;
                if(this.currentPlaybackSession.value != null) {
                    this.currentPlaybackSession.value.onStop(this);
                    this.currentPlaybackSession.value.getPlayerConnection().severConnection();
                }
                this.currentPlaybackSession.value = playbackSession;
                if(!released) {
                    playbackSessionContainerCollector.onPlaybackSessionChanged(oldSession, playbackSession);
                }

                if(this.currentPlaybackSession.value != null) {
                    this.currentPlaybackSession.value.getPlayerConnection().openConnection(communicationsChannel);
                    this.currentPlaybackSession.value.onStart(this);
                }
            }
        } finally {
            isReplacingPlaybackSession = false;
            if(!released) {
                enigmaPlayerListeners.onPlaybackSessionChanged(oldSession, playbackSession);
            }

        }
    }

    private void setPlayingFromLive(boolean live) {
        if(stateMachine.getState() != EnigmaPlayerState.PLAYING) { return; }
        IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
        if(playbackSession != null) {
            playbackSession.setPlayingFromLive(live);
        }
    }

    public IAnalyticsReporter getCurrentAnalyticsReporter() {
        IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
        if(playbackSession != null) {
           return playbackSession.getAnalyticsReporter();
        }
        return null;
    }

    private void updatePlayingFromLive() {
        if(stateMachine.getState() != EnigmaPlayerState.PLAYING) { return; }
        ITimelinePosition timelinePosition = environment.playerImplementationInternals.getCurrentPosition();
        ITimelinePosition startPosition = environment.playerImplementationInternals.getCurrentStartBound();
        ITimelinePosition endPos = environment.playerImplementationInternals.getCurrentEndBound();
        IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
        if(timelinePosition != null && endPos != null) {
            long seconds = endPos.subtract(timelinePosition).inWholeUnits(Duration.Unit.SECONDS);
            setPlayingFromLive(seconds < 60 && stateMachine.getState() == EnigmaPlayerState.PLAYING);
            long windowTime = endPos.subtract(startPosition).inWholeUnits(Duration.Unit.SECONDS);
            boolean allowed = windowTime > 300 && stateMachine.getState() == EnigmaPlayerState.PLAYING;
            playbackSession.setSeekLiveAllowed(allowed);
        } else {
            setPlayingFromLive(false);
            playbackSession.setSeekLiveAllowed(false);
        }
    }

    /*package-protected*/ boolean hasPlaybackSessionSeed() {
        return OpenContainerUtil.getValueSynchronized(playbackSessionSeed) != null;
    }

    @Override
    public boolean isAdBeingPlayed(){
        boolean adBeingPlayed = false;
        IAdIncludedTimeline timeline = getAdDetector().getTimeline();
        if (timeline instanceof AdIncludedTimeline) {
            if (timeline.getCurrentAdBreak() != null) {
                adBeingPlayed = true;
            }
        }
        return adBeingPlayed;
    }

    @Override
    public IVirtualControls getVirtualControls() {
        return virtualControls;
    }

    public void setVirtualControls(IVirtualControls virtualControls) {
        this.virtualControls = virtualControls;
    }

    private class EnigmaPlayerEnvironment implements IEnigmaPlayerEnvironment, IDrmProvider {
        private IMediaFormatSupportSpec formatSupportSpec = new DefaultFormatSupportSpec();
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
                        public void onError(EnigmaError error) {
                            if(stateMachine.getState() == EnigmaPlayerState.LOADING) {
                                IPlaybackStartAction activeStartAction = OpenContainerUtil.getValueSynchronized(currentPlaybackStartAction);
                                if(activeStartAction != null) {
                                    activeStartAction.onErrorDuringStartup(error);
                                }
                            }
                            enigmaPlayerListeners.onPlaybackError(error);
                            stateMachine.setState(EnigmaPlayerState.IDLE);
                        }

                        @Override
                        public void onLoadCompleted() {
                            synchronized (currentPlaybackStartAction) {
                                if(currentPlaybackStartAction.value != null) {
                                    stateMachine.setState(EnigmaPlayerState.LOADED);
                                    environment.playerImplementationControls.start(new BasePlayerImplementationControlResultHandler());
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
                        public void onPlaybackBuffering() {
                            if(stateMachine.getState() == EnigmaPlayerState.PLAYING) {
                                stateMachine.setState(EnigmaPlayerState.BUFFERING);
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
                            IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
                            if(playbackSession != null) {
                                playbackSession.fireEndReached();
                            }
                            replacePlaybackSession(null);
                            stateMachine.setState(EnigmaPlayerState.IDLE);
                        }

                        private <T> void propagateToCurrentPlaybackSession(T arg, IInternalPlaybackSessionsMethod<T> playbackSessionsMethod) {
                            if(isReplacingPlaybackSession) { return; }

                            IInternalPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
                            if(playbackSession != null) {
                                playbackSessionsMethod.call(playbackSession, arg);
                            }
                        }

                        @Override
                        public void onTracksChanged(Collection<? extends IPlayerImplementationTrack> tracks) {
                            propagateToCurrentPlaybackSession(tracks, IInternalPlaybackSession::setTracks);
                            // sometime tracks are fetched even before playback start, so there is null check
                            if (virtualControls != null) {
                                // tracks have changed , so refresh buttons state
                                virtualControls.getAudioTrackButton().refresh();
                                virtualControls.getSubtitlesButton().refresh();
                            }
                        }

                        @Override
                        public void onAudioTrackSelectionChanged(IAudioTrack track) {
                            propagateToCurrentPlaybackSession(track, IInternalPlaybackSession::setSelectedAudioTrack);
                        }

                        @Override
                        public void onSubtitleTrackSelectionChanged(ISubtitleTrack track) {
                            propagateToCurrentPlaybackSession(track, IInternalPlaybackSession::setSelectedSubtitleTrack);
                        }

                        @Override
                        public void onVideoTrackSelectionChanged(IVideoTrack track) {
                            propagateToCurrentPlaybackSession(track, IInternalPlaybackSession::setSelectedVideoTrack);
                        }

                        @Override
                        public void onManifestChanged(String manifestUrl, StreamFormat streamFormat, long startTime) {
                            IInternalPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
                            if(playbackSession == null || playbackSession.getStreamInfo() == null || adsDetector.value == null) { return; }
                            if(streamFormat == StreamFormat.HLS &&
                               playbackSession.getStreamInfo().ssaiEnabled() &&
                               playbackSession.getStreamInfo().isLiveStream()) {
                                IAdResourceLoader resourceLoader = adsDetector.value.getFactory().createResourceLoader(playbackSession.getAdsMetadata(), manifestUrl);
                                if(resourceLoader != null) {
                                    adsDetector.value.update(resourceLoader, startTime);
                                }
                            }

                        }
                    };
                }
                return playerImplementationListener;
            }
        }

        @Override
        public IDrmInfo getDrmInfo() {
            IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
            if(playbackSession != null) {
                return playbackSession.getDrmInfo();
            } else {
                return null;
            }
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

        @Override
        public Set<EnigmaMediaFormat> getSupportedFormats() {
            EnigmaMediaFormat enigmaMediaFormat = new EnigmaMediaFormat(StreamFormat.DASH,DrmTechnology.NONE);
            Set<EnigmaMediaFormat> enigmaMediaFormats = new HashSet<>();
            enigmaMediaFormats.add(enigmaMediaFormat);
            return enigmaMediaFormats;
        }
    }

    private class EnigmaPlayerControls extends AbstractEnigmaPlayerControls {
        private ControlResultHandlerAdapter wrapResultHandler(IControlResultHandler resultHandler) {
            return new ControlResultHandlerAdapter(ProxyCallback.useCallbackHandlerIfPresent(callbackHandler, IControlResultHandler.class, resultHandler));
        }

        @Override
        public IControlResultHandler getDefaultResultHandler() {
            return new DefaultControlResultHandler(TAG);
        }

        @Override
        public void start(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);

            try {
                EnigmaPlayerState currentState = stateMachine.getState();
                IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
                boolean hasPlaybackSessionSeed = hasPlaybackSessionSeed();
                ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(currentState, playbackSession, hasPlaybackSessionSeed);
                if(validationResults.isSuccess()) {
                    if(playbackSession != null) {
                        environment.playerImplementationControls.start(controlResultHandler);
                        controlResultHandler.runWhenDone(() -> {
                            stateMachine.setState(EnigmaPlayerState.PLAYING);
                            updatePlayingFromLive();
                        });
                        return;
                    } else {
                        PlaybackSessionSeed seed = null;
                        synchronized (playbackSessionSeed) {
                            if(playbackSessionSeed.value != null) {
                                seed = playbackSessionSeed.value;
                                playbackSessionSeed.value = null;
                            }
                        }
                        if(seed != null) {
                            EnigmaPlayer.this.play(seed.createPlayRequest(new BasePlayResultHandler() {
                                @Override
                                public void onStarted(IPlaybackSession playbackSession) {
                                    controlResultHandler.onDone();
                                }

                                @Override
                                public void onError(EnigmaError error) {
                                    controlResultHandler.onError(error);
                                }
                            }));
                            return;
                        } else {
                            throw new IllegalStateException("Can't find playback session"); //This should never happen, ControlLogic should have identified this.
                        }
                    }
                } else {
                    ((ControlLogic.IFailedValidationResults) validationResults).triggerCallback(controlResultHandler);
                    return;
                }
            } catch (RuntimeException e) {
                controlResultHandler.onError(new UnexpectedError(e));
                return;
            }
        }

        @Override
        public void pause(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            try {
                EnigmaPlayerState currentState = stateMachine.getState();
                IPlaybackSession playbackSession = currentPlaybackSession.value;
                ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validatePause(currentState, playbackSession);
                if(validationResults.isSuccess()) {
                    environment.playerImplementationControls.pause(controlResultHandler);
                    controlResultHandler.runWhenDone(() -> {
                        stateMachine.setState(EnigmaPlayerState.PAUSED);
                        setPlayingFromLive(false);
                    });
                } else {
                    ((ControlLogic.IFailedValidationResults) validationResults).triggerCallback(controlResultHandler);
                    return;
                }
            } catch (RuntimeException e) {
                controlResultHandler.onError(new UnexpectedError(e));
                return;
            }
        }

        @Override
        public void stop(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            environment.playerImplementationControls.stop(controlResultHandler);
            controlResultHandler.runWhenDone(() -> {
                stateMachine.setState(EnigmaPlayerState.IDLE);
            });
        }

        private void afterSeekToSuccess(IInternalPlaybackSession playbackSession) {
            sendSeekEvent(playbackSession);
            updatePlayingFromLive();
        }

        @Override
        public void seekTo(long requestedMs, IControlResultHandler resultHandler) {
            synchronized (currentPlaybackSession) {
                ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
                //We need to use  playerImplementationInternals.getCurrentPosition() so we need to run on UiThread (for exo).
                AndroidThreadUtil.runOnUiThread(() -> {
                    long millis = requestedMs;
                    try {
                        IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
                        ITimelinePosition seekPos = environment.timelinePositionFactory.newPosition(millis);

                        boolean seekForward = false;
                        boolean seekBackward = false;
                        ITimelinePosition currentPosition = getTimeline().getCurrentPosition();
                        ITimelinePosition livePosition = getTimeline().getLivePosition();
                        if (livePosition != null && seekPos.after(livePosition)) {
                            millis += livePosition.subtract(seekPos).inWholeUnits(Duration.Unit.MILLISECONDS);
                        }
                        if (currentPosition != null) {
                            long timeDiffMillis = seekPos.subtract(currentPosition).inWholeUnits(Duration.Unit.MILLISECONDS);
                            seekForward = timeDiffMillis > 0;
                            seekBackward = timeDiffMillis < 0;
                        }

                        ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateSeek(seekForward, seekBackward, playbackSession);
                        if(validationResults.isSuccess()) {
                            long adDuration = adsDetector.value.getTimeline().getPastAdDuration().inWholeUnits(Duration.Unit.MILLISECONDS);
                            long newPosition = millis + adDuration;
                            AdDetector adDetector = (AdDetector) adsDetector.value;
                            if (adDetector.isSsaiEnabled() && getTimeline() instanceof AdIncludedTimeline) {
                                adDetector.setJumpOnOriginalScrubTime(null);
                                AdIncludedTimeline adIncludedTimeline = (AdIncludedTimeline) getTimeline();
                                long adsForGivenScrubTime = adIncludedTimeline.getTotalAdDurationFromThisTime(environment.timelinePositionFactory.newPosition(requestedMs));
                                long adsForGivenCurrentTime = adIncludedTimeline.getTotalAdDurationFromThisTime(currentPosition);
                                ITimelinePosition afterTimeIncludingAds = environment.timelinePositionFactory.newPosition(adsForGivenScrubTime + requestedMs);
                                ITimelinePosition beforeTimeIncludingAds = environment.timelinePositionFactory.newPosition(adsForGivenCurrentTime + currentPosition.getStart());

                                AdBreak adBreak = adIncludedTimeline.getLastAdBreakBetweenPositions(beforeTimeIncludingAds, afterTimeIncludingAds);

                                if (adBreak !=null && !adBreak.isAdShown()) {
                                    newPosition = adBreak.getStart().getStart();
                                    adBreak.setAdShown(true);
                                    adsDetector.value.setAdPlaying(true);
                                    adDetector.setJumpOnOriginalScrubTime(environment.timelinePositionFactory.newPosition(requestedMs));
                                } else {
                                    newPosition = (adsForGivenScrubTime + requestedMs);
                                }
                            }
                            environment.playerImplementationControls.seekTo(new IPlayerImplementationControls.TimelineRelativePosition(newPosition), controlResultHandler);
                            controlResultHandler.runWhenDone(() -> {
                                afterSeekToSuccess(playbackSession);
                            });
                        } else {
                            ((ControlLogic.IFailedValidationResults) validationResults).triggerCallback(controlResultHandler);
                            return;
                        }
                    } catch (RuntimeException e) {
                        controlResultHandler.onError(new UnexpectedError(e));
                        return;
                    }
                });
            }
        }

        private void sendSeekEvent(IInternalPlaybackSession playbackSession) {
            if(isReplacingPlaybackSession) { return; }
            IInternalPlaybackSession session = currentPlaybackSession.value;
            if(session != playbackSession) {
                return; //Stale event
            } else {
                session.fireSeekCompleted();
            }
        }

        @Override
        public void seekTo(StreamPosition streamPosition, IControlResultHandler resultHandler) {
            if(isReplacingPlaybackSession) { return; }
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);

            try {
                IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
                ControlLogic.IValidationResults<IPlayerImplementationControls.ISeekPosition> validationResults = ControlLogic.validateSeek(streamPosition, playbackSession);
                if(validationResults.isSuccess()) {
                    IPlayerImplementationControls.ISeekPosition seekPosition = validationResults.getRelevantData();
                    environment.playerImplementationControls.seekTo(seekPosition, controlResultHandler);
                    controlResultHandler.runWhenDone(() -> afterSeekToSuccess(playbackSession));
                } else {
                    ((ControlLogic.IFailedValidationResults) validationResults).triggerCallback(controlResultHandler);
                    return;
                }
            } catch (RuntimeException e) {
                controlResultHandler.onError(new UnexpectedError(e));
                return;
            }
        }

        private void jumpToProgram(IControlResultHandler resultHandler, boolean jumpBackwards) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);

            AndroidThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(isReplacingPlaybackSession) { return; }
                        IPlaybackSession playbackSession = currentPlaybackSession.value;
                        ControlLogic.IValidationResults<Long> validationResults = ControlLogic.validateProgramJump(jumpBackwards, playbackSession);
                        if(validationResults.isSuccess()) {
                            Long seekPos = validationResults.getRelevantData();
                            environment.playerImplementationControls.seekTo(new IPlayerImplementationControls.TimelineRelativePosition(seekPos), controlResultHandler);
                        } else {
                            ((ControlLogic.IFailedValidationResults) validationResults).triggerCallback(controlResultHandler);
                            return;
                        }
                    } catch (RuntimeException e) {
                        controlResultHandler.onError(new UnexpectedError(e));
                        return;
                    }
                }
            });
        }

        @Override
        public void nextProgram(IControlResultHandler resultHandler) {
            jumpToProgram(resultHandler, false);
        }

        @Override
        public void previousProgram(IControlResultHandler resultHandler) {
            jumpToProgram(resultHandler, true);
        }

        @Override
        public void setVolume(float volume, IControlResultHandler resultHandler) {
            environment.playerImplementationControls.setVolume(volume, wrapResultHandler(resultHandler));
        }


        private <T> void setTrack(T track, IControlResultHandler resultHandler,
                                  IPlayerImplementationControlsMethod<T> controlsMethod,
                                  IInternalPlaybackSessionsMethod<T>  playbackSessionsMethod) {
            if(isReplacingPlaybackSession) { return; }
            controlsMethod.call(environment.playerImplementationControls, track, wrapResultHandler(resultHandler).runWhenDone(() -> {

                if(isReplacingPlaybackSession) { return; }
                IInternalPlaybackSession playbackSession = currentPlaybackSession.value;
                if(playbackSession != null) {
                    playbackSessionsMethod.call(playbackSession, track);
                }
            }));
        }

        @Override
        public void setSubtitleTrack(final ISubtitleTrack track, IControlResultHandler resultHandler) {
            setTrack(track, resultHandler,
                    IPlayerImplementationControls::setSubtitleTrack,
                    IInternalPlaybackSession::setSelectedSubtitleTrack);
        }

        @Override
        public void setAudioTrack(IAudioTrack track, IControlResultHandler resultHandler) {
            setTrack(track, resultHandler,
                    IPlayerImplementationControls::setAudioTrack,
                    IInternalPlaybackSession::setSelectedAudioTrack);
        }

        @Override
        public void setMaxVideoTrackDimensions(int width, int height, IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            try {
                environment.playerImplementationControls.setMaxVideoTrackDimensions(width, height, controlResultHandler);
            } catch (RuntimeException e) {
                controlResultHandler.onError(new UnexpectedError(e));
                return;
            }
        }
    }

    private class EnigmaPlayerTimeline implements ITimeline, IPlaybackSessionContainerListener {
        private final SimpleTimeline timeline = new SimpleTimeline();
        private Repeater repeater;
        private ITimelinePosition streamTimelineStart = null;
        private ITimelinePosition streamTimelineEnd = null;
        private ITimelinePosition streamTimelinePosition = null;
        private long currentStreamOffset = 0;
        private boolean hasProgram = false;

        // The frequency (in ms) of the time line updating.
        private final long UPDATE_INTERVAL = 1000 / 30;
        // The minimum time window required (in minutes) for the timeline to be visible during  playback of linear assets.
        private final Duration CHANNEL_VISIBILITY_WINDOW = Duration.minutes(30);
        // The minimum time window required (in minutes) for the timeline to be visible during  playback of live events.
        private final Duration LIVE_EVENT_VISIBILITY_WINDOW = Duration.minutes(2);

        private final OpenContainer<IInternalPlaybackSession> lastSignaledPlaybackSession = new OpenContainer<>(null);
        private final ProgramTracker programTracker = new ProgramTracker().addListener(new ProgramTracker.IProgramChangedListener() {
            @Override
            public void onProgramChanged(IProgram oldProgram, IProgram newProgram) {
                if(newProgram != null) {
                    IInternalPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
                    if(playbackSession != null) {
                        IStreamInfo streamInfo = playbackSession.getStreamInfo();
                        if(streamInfo.hasStart()) {
                            long streamStartUtcMillis = streamInfo.getStart(Duration.Unit.MILLISECONDS);
                            ITimelinePositionFactory positionFactory = environment.timelinePositionFactory;
                            long startOffset = newProgram.getStartUtcMillis() - streamStartUtcMillis;
                            long endOffset = newProgram.getEndUtcMillis() - streamStartUtcMillis;
                            ITimelinePosition start = positionFactory.newPosition(startOffset);
                            ITimelinePosition end = positionFactory.newPosition(endOffset);
                            timeline.setVisibility(!streamInfo.isLiveStream());
                            if (start.getStart() < 0) {
                                start = positionFactory.newPosition(0);
                            }
                            // for Live stream display end equal to live position
                            if (isLiveStream()) {
                                end = getLivePosition();
                            } else {
                                if (end == null) {
                                    end = positionFactory.newPosition(0);
                                }
                            }
                            if (oldProgram != null && isLiveStream()) {
                                startOffset = oldProgram.getStartUtcMillis() - streamStartUtcMillis;
                                start = positionFactory.newPosition(startOffset);
                            }

                            if (isLiveStream()) {
                                // dont use it for VOD, as duration should come from stream read by Player
                                // for Live stream display end equal to live position
                                EnigmaPlayerTimeline.this.onExposedTimelineBoundsChanged(start, end);
                            }
                            hasProgram = true;
                        }
                    }
                } else {
                    hasProgram = false;
                    // for Live stream display end equal to live position
                    if (isLiveStream() && getLivePosition() != null) {
                        ITimelinePosition end = getLivePosition();
                        EnigmaPlayerTimeline.this.onExposedTimelineBoundsChanged(streamTimelineStart, end);
                    } else {
                        EnigmaPlayerTimeline.this.onExposedTimelineBoundsChanged(streamTimelineStart, streamTimelineEnd);
                    }
                }
            }
        });

        private EnigmaPlayerTimeline(EnigmaPlayerLifecycle lifecycle) {
            ITaskFactory mainThreadTaskFactory = getTaskFactoryProvider().getMainThreadTaskFactory();
            repeater = new Repeater(mainThreadTaskFactory, UPDATE_INTERVAL, () -> {
                timeline.setCurrentPosition(environment.playerImplementationInternals.getCurrentPosition());
                timeline.setLivePosition(environment.playerImplementationInternals.getLivePosition());
            });
            lifecycle.addListener(new BaseLifecycleListener<Void, Void>() {
                @Override
                public void onStart(Void aVoid) {
                    EnigmaPlayerTimeline.this.init();
                }

                @Override
                public void onStop(Void aVoid) {
                    EnigmaPlayerTimeline.this.repeater.setEnabled(false);
                }
            });
        }

        public void init() {
            programTracker.addListener(environment.timelinePositionFactory);
            programTracker.addListener((oldProgram, newProgram) -> enigmaPlayerListeners.onProgramChanged(oldProgram, newProgram));
            programTracker.init(EnigmaPlayer.this);
            timeline.addListener(new BaseTimelineListener() {
                @Override
                public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
                    streamTimelinePosition = timelinePosition;
                    updateStreamOffset();
                    if (isSeekBusy) {
                        return;
                    }
                    isSeekBusy = true;
                    try {
                        if (adsDetector.value != null) {

                            // if ad is not being played
                            if(adsDetector.value.isAdPlaying()){
                                return;
                            }

                                IAdIncludedTimeline timeline = adsDetector.value.getTimeline();
                            if (timeline instanceof AdIncludedTimeline) {
                                AdIncludedTimeline adIncludedTimeline = (AdIncludedTimeline) timeline;
                                // See if we started playing right on top of an ad
                                //AdBreak newAdtoStart = adInfestedTimeline.getCurrentAdBreak();
                                AdBreak newAdtoStart = adIncludedTimeline.getAdBreakIfPositionIsBetweenTheAd(timelinePosition);
                                // check if this adBreak has been shown already
                                if (newAdtoStart == null) {
                                    adsDetector.value.setAdPlaying(false);
                                }
                                if (newAdtoStart != null && newAdtoStart.isAdShown()) {
                                    ITimelinePosition newPosition = newAdtoStart.getEnd();
                                    environment.playerImplementationControls.seekTo(
                                            new IPlayerImplementationControls.TimelineRelativePosition(newPosition.getStart()),
                                            new BasePlayerImplementationControlResultHandler() {
                                                @Override
                                                public void onError(EnigmaError error) {
                                                    Log.d("Timeline", error.getTrace());
                                                }
                                            });
                                }else{
                                    if(newAdtoStart != null && !newAdtoStart.isAdShown()) {
                                        if (!adsDetector.value.isAdPlaying()) {
                                            environment.playerImplementationControls.seekTo(
                                                    new IPlayerImplementationControls.TimelineRelativePosition(newAdtoStart.getStart().getStart()),
                                                    new BasePlayerImplementationControlResultHandler() {
                                                        @Override
                                                        public void onError(EnigmaError error) {
                                                            Log.d("Timeline", error.getTrace());
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("SSAI", ex.getMessage(), ex);
                    } finally {
                        isSeekBusy = false;
                    }
                }
            });
            if(adsDetector.value != null) {
                adsDetector.value.addListener(new IAdDetector.AdStateListener() {
                    @Override
                    public void adStateChanged(IAdDetector adsDetector, @Nullable IAd currentAdd, AdEventType eventType) {
                        updateTimelineVisibility();
                    }
                });
            }
            EnigmaPlayer.this.addListener(new BaseEnigmaPlayerListener() {
                @Override
                public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
                    if(to == EnigmaPlayerState.PLAYING) {
                        repeater.setEnabled(true);
                    } else if(from == EnigmaPlayerState.PLAYING) {
                        repeater.setEnabled(false);
                    }
                    if(to == EnigmaPlayerState.LOADED) {
                        updateTimelineVisibility();
                    } else if(to == EnigmaPlayerState.IDLE || to == EnigmaPlayerState.LOADING) {
                        timeline.setVisibility(false);
                    }
                }
            });
        }


        @Override
        public void onPlaybackSessionChanged(IInternalPlaybackSession oldSession, IInternalPlaybackSession newSession) {
            OpenContainerUtil.setValueSynchronized(lastSignaledPlaybackSession, newSession, null);
            updateTimelineVisibility();
        }

        private boolean calculateVisibility() {
            IInternalPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(lastSignaledPlaybackSession);
            if(playbackSession == null) {
                return false;
            } else if (adsDetector.value != null && adsDetector.value.isAdPlaying()) {
                return false;
            } else if(playbackSession.getStreamInfo().isLiveStream()) {
                ITimelinePosition endBound = environment.playerImplementationInternals.getCurrentEndBound();
                ITimelinePosition startBound = environment.playerImplementationInternals.getCurrentStartBound();
                if (startBound != null && endBound != null) {
                    Duration duration = endBound.subtract(startBound);
                    if (playbackSession.getStreamInfo().isEvent()) {
                        return duration.compareTo(LIVE_EVENT_VISIBILITY_WINDOW) > 0;
                    } else {
                        return duration.compareTo(CHANNEL_VISIBILITY_WINDOW) > 0;
                    }
                }
            }
            return !playbackSession.getStreamInfo().isLiveStream();
        }

        private void updateTimelineVisibility() {
            timeline.setVisibility(calculateVisibility());
        }

        @Override
        public ITimelinePosition getLivePosition() {
            if(stateMachine.getState() != EnigmaPlayerState.PLAYING || isReplacingPlaybackSession) { return null; }

            IInternalPlaybackSession session = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);

            if (session != null && session.getStreamInfo().isLiveStream()) {
                return environment.playerImplementationInternals.getLivePosition();
            }

            return null;
        }

        @Override
        public ITimelinePosition getCurrentPosition() {
            return environment.playerImplementationInternals.getCurrentPosition();
        }

        @Override
        public ITimelinePosition getCurrentEndBound() {
            ITimelinePosition currentEnd = timeline.getCurrentEndBound();
            if(currentEnd == null) {
                currentEnd = environment.playerImplementationInternals.getCurrentEndBound();
            }
            if (isLiveStream()) {
                if (adsDetector.value != null && adsDetector.value.getLiveDelay() != null) {
                    if (currentEnd != null) {
                        currentEnd = currentEnd.subtract(adsDetector.value.getLiveDelay());
                    }
                }
            }
            return currentEnd;
        }

        @Override
        public ITimelinePosition getCurrentStartBound() {
            ITimelinePosition currentStart = timeline.getCurrentStartBound();
            if(currentStart == null) {
                currentStart = environment.playerImplementationInternals.getCurrentStartBound();
            }
            return currentStart;
        }

        @Override
        public boolean getVisibility() {
            return timeline.getVisibility();
        }

        @Override
        public void addListener(ITimelineListener listener) {
            timeline.addListener(listener);
        }

        @Override
        public void addListener(ITimelineListener listener, Handler handler) {
            timeline.addListener(listener, handler);
        }

        @Override
        public void removeListener(ITimelineListener listener) {
            timeline.removeListener(listener);
        }

        public void onStreamTimelineBoundsChanged(ITimelinePosition start, ITimelinePosition end) {
            streamTimelineStart = start;
            streamTimelineEnd = end;
            updateStreamOffset();
            if(!hasProgram) {
                EnigmaPlayerTimeline.this.onExposedTimelineBoundsChanged(streamTimelineStart, streamTimelineEnd);
            }
            if(start != null && end != null) {
                updateTimelineVisibility();
                updatePlayingFromLive();
            }
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
                    timeline.setBounds(start, end);
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
        private final IPlaybackProperties playbackProperties;

        public PlaybackSessionInfo(IPlayable playable, String assetId, String mediaLocator, IPlaybackTechnologyIdentifier tech, IPlaybackProperties playbackProperties) {
            this.playable = playable;
            this.assetId = assetId;
            this.mediaLocator = mediaLocator;
            this.tech = tech;
            this.playbackProperties = playbackProperties;
        }

        @Override
        public Duration getCurrentPlaybackOffset() {
            if(released) { return Duration.millis(0); }
            ITimelinePosition currentPosition = getTimeline().getCurrentPosition();
            ITimelinePosition startBound = getTimeline().getCurrentStartBound();
            if(startBound == null) {
                startBound = environment.timelinePositionFactory.newPosition(0);
            }
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

        @Override
        public String getCurrentProgramId() {
            IProgram currentProgram = timeline.programTracker.getCurrentProgram();
            return currentProgram != null ? currentProgram.getProgramId() : null;
        }

        @Override
        public IPlaybackProperties getPlaybackProperties() {
            return playbackProperties;
        }
    }

    /**
     * Communication interface from InternalPlaybackSession back to EnigmaPlayer
     */
    private class InternalEnigmaPlayerCommunicationsChannel implements IEnigmaPlayerConnection.ICommunicationsChannel {
        @Override
        public void onPlaybackError(EnigmaError error, boolean endStream) {
            try {
                enigmaPlayerListeners.onPlaybackError(error);
            } finally {
                if(endStream) {
                    environment.playerImplementationControls.stop(new BasePlayerImplementationControlResultHandler());
                }
            }
        }

        @Override
        public void onExpirePlaybackSession(PlaybackSessionSeed seed) {
            OpenContainerUtil.setValueSynchronized(playbackSessionSeed, seed, null);
            replacePlaybackSession(null);
        }
    }

    private interface IPlayerImplementationControlsMethod<T> {
        void call(IPlayerImplementationControls controls, T arg, IPlayerImplementationControlResultHandler resultHandler);
    }

    private interface IInternalPlaybackSessionsMethod<T> {
        void call(IInternalPlaybackSession internalPlaybackSession, T arg);
    }

    /*package-protected*/ static class EnigmaPlayerLifecycle extends Lifecycle<Void, Void> {
    }
}