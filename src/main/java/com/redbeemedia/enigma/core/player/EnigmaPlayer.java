package com.redbeemedia.enigma.core.player;

import android.app.Activity;
import android.os.Handler;

import com.redbeemedia.enigma.core.activity.AbstractActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
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
import com.redbeemedia.enigma.core.video.IVideoTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class EnigmaPlayer implements IEnigmaPlayer {
    private static final String TAG = "EnigmaPlayer";

    private static final IMediaFormatSelector DEFAULT_MEDIA_FORMAT_SELECTOR = new SimpleMediaFormatSelector(EnigmaMediaFormat.DASH().widevine(),
                                                                                                            EnigmaMediaFormat.DASH().unenc(),
                                                                                                            EnigmaMediaFormat.HLS().fairplay(),
                                                                                                            EnigmaMediaFormat.HLS().unenc(),
                                                                                                            EnigmaMediaFormat.SMOOTHSTREAMING().unenc());
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

    private EnigmaPlayerCollector enigmaPlayerListeners = new EnigmaPlayerCollector();

    private final InternalEnigmaPlayerCommunicationsChannel communicationsChannel = new InternalEnigmaPlayerCommunicationsChannel();
    private final OpenContainer<IInternalPlaybackSession> currentPlaybackSession = new OpenContainer<>(null);
    private final OpenContainer<PlaybackSessionSeed> playbackSessionSeed = new OpenContainer<>(null);
    private PlaybackSessionContainerCollector playbackSessionContainerCollector = new PlaybackSessionContainerCollector();
    private final OpenContainer<IPlaybackStartAction> currentPlaybackStartAction = new OpenContainer<>(null);

    private volatile boolean released = false;

    /**
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
        this.playerImplementation = playerImplementation;
        this.playerImplementation.install(environment);
        playbackSessionContainerCollector.addListener(environment.timelinePositionFactory);
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
                    newStartActionPlayerConnection(playRequest));
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
    public void release() {
        if(released) {
            return;
        }

        RuntimeExceptionHandler exceptionHandler = new RuntimeExceptionHandler();
        exceptionHandler.catchExceptions(() -> {
            replacePlaybackSession(null);
        });
        exceptionHandler.catchExceptions(() -> playerImplementation.release());
        exceptionHandler.catchExceptions(() -> lifecycle.fireOnStop(null));

        released = true;

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
        return timeline;
    }

    @Override
    public EnigmaPlayerState getState() {
        return stateMachine.getState();
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

    protected IPlaybackStartAction newPlaybackStartAction(ISession session, IBusinessUnit businessUnit, ITimeProvider timeProvider, IPlayRequest playRequest, IHandler callbackHandler,ITaskFactoryProvider taskFactoryProvider, IPlayerImplementationControls playerImplementationControls, IPlaybackStartAction.IEnigmaPlayerCallbacks playerConnection) {
        return new DefaultPlaybackStartAction(session, businessUnit, timeProvider, playRequest, callbackHandler,taskFactoryProvider, playerImplementationControls, playerConnection);
    }

    protected ITaskFactoryProvider getTaskFactoryProvider() {
        return EnigmaRiverContext.getTaskFactoryProvider();
    }

    private void replacePlaybackSession(IInternalPlaybackSession playbackSession) {
        synchronized (currentPlaybackSession) {
            IInternalPlaybackSession oldSession = this.currentPlaybackSession.value;
            if(this.currentPlaybackSession.value != null) {
                this.currentPlaybackSession.value.onStop(this);
                this.currentPlaybackSession.value.getPlayerConnection().severConnection();
            }
            this.currentPlaybackSession.value = playbackSession;
            playbackSessionContainerCollector.onPlaybackSessionChanged(oldSession, playbackSession);
            enigmaPlayerListeners.onPlaybackSessionChanged(oldSession, playbackSession);
            if(this.currentPlaybackSession.value != null) {
                this.currentPlaybackSession.value.getPlayerConnection().openConnection(communicationsChannel);
                this.currentPlaybackSession.value.onStart(this);
            }
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

    /*package-protected*/ boolean hasPlaybackSessionSeed() {
        return OpenContainerUtil.getValueSynchronized(playbackSessionSeed) != null;
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
                                    currentPlaybackStartAction.value.onStarted(OpenContainerUtil.getValueSynchronized(currentPlaybackSession));
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
                            synchronized (currentPlaybackSession) {
                                if(currentPlaybackSession.value != null) {
                                    currentPlaybackSession.value.fireEndReached();
                                }
                            }
                            replacePlaybackSession(null);
                            stateMachine.setState(EnigmaPlayerState.IDLE);
                        }

                        private <T> void propagateToCurrentPlaybackSession(T arg, IInternalPlaybackSessionsMethod<T> playbackSessionsMethod) {
                            synchronized (currentPlaybackSession) {
                                if(currentPlaybackSession.value != null) {
                                    playbackSessionsMethod.call(currentPlaybackSession.value, arg);
                                }
                            }
                        }

                        @Override
                        public void onTracksChanged(Collection<? extends IPlayerImplementationTrack> tracks) {
                            propagateToCurrentPlaybackSession(tracks, IInternalPlaybackSession::setTracks);
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
                    };
                }
                return playerImplementationListener;
            }
        }

        @Override
        public IDrmInfo getDrmInfo() {
            IInternalPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
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
    }

    private class EnigmaPlayerControls extends AbstractEnigmaPlayerControls {
        private ControlResultHandlerAdapter wrapResultHandler(IControlResultHandler resultHandler) {
            return new ControlResultHandlerAdapter(ProxyCallback.useCallbackHandlerIfPresent(callbackHandler, IControlResultHandler.class, resultHandler));
        }

        @Override
        protected IControlResultHandler getDefaultResultHandler() {
            return new DefaultControlResultHandler(TAG);
        }

        @Override
        public void start(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);

            try {
                EnigmaPlayerState currentState = stateMachine.getState();
                IInternalPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
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
                IPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
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
                setPlayingFromLive(false);
            });
        }

        private void afterSeekToSuccess(IInternalPlaybackSession playbackSession) {
            sendSeekEvent(playbackSession);
            updatePlayingFromLive();
        }

        @Override
        public void seekTo(long millis, IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            //We need to use  playerImplementationInternals.getCurrentPosition() so we need to run on UiThread (for exo).
            AndroidThreadUtil.runOnUiThread(() -> {
                try {
                    IInternalPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
                    ITimelinePosition seekPos = environment.timelinePositionFactory.newPosition(millis);

                    boolean seekForward = false;
                    boolean seekBackward = false;
                    ITimelinePosition currentPosition = environment.playerImplementationInternals.getCurrentPosition();
                    if(currentPosition != null) {
                        long timeDiffMillis = seekPos.subtract(currentPosition).inWholeUnits(Duration.Unit.MILLISECONDS);
                        seekForward = timeDiffMillis > 0;
                        seekBackward = timeDiffMillis < 0;
                    }

                    ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateSeek(seekForward, seekBackward, playbackSession);
                    if(validationResults.isSuccess()) {
                        environment.playerImplementationControls.seekTo(new IPlayerImplementationControls.TimelineRelativePosition(millis), controlResultHandler);
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

        private void sendSeekEvent(IInternalPlaybackSession playbackSession) {
            synchronized (currentPlaybackSession) {
                if(currentPlaybackSession.value != playbackSession) {
                    return; //Stale event
                } else {
                    currentPlaybackSession.value.fireSeekCompleted();
                }
            }
        }

        @Override
        public void seekTo(StreamPosition streamPosition, IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);

            try {
                IInternalPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
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
                        IPlaybackSession playbackSession = OpenContainerUtil.getValueSynchronized(currentPlaybackSession);
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
            controlsMethod.call(environment.playerImplementationControls, track, wrapResultHandler(resultHandler).runWhenDone(() -> {
                synchronized (currentPlaybackSession) {
                    if(currentPlaybackSession.value != null) {
                        playbackSessionsMethod.call(currentPlaybackSession.value, track);
                    }
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
    }

    private class EnigmaPlayerTimeline implements ITimeline {
        private final SimpleTimeline timeline = new SimpleTimeline();
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
                        IStreamInfo streamInfo = playbackSession.getStreamInfo();
                        if(streamInfo.hasStart()) {
                            long streamStartUtcMillis = streamInfo.getStart(Duration.Unit.MILLISECONDS);
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

        private EnigmaPlayerTimeline(EnigmaPlayerLifecycle lifecycle) {
            ITaskFactory mainThreadTaskFactory = getTaskFactoryProvider().getMainThreadTaskFactory();
            repeater = new Repeater(mainThreadTaskFactory, 1000 / 30, () -> timeline.setCurrentPosition(environment.playerImplementationInternals.getCurrentPosition()));
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
                        timeline.setVisibility(true);
                    } else if(to == EnigmaPlayerState.IDLE || to == EnigmaPlayerState.LOADING) {
                        timeline.setVisibility(false);
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
            ITimelinePosition currentEnd = timeline.getCurrentEndBound();
            if(currentEnd == null) {
                currentEnd = environment.playerImplementationInternals.getCurrentEndBound();
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
            ITimelinePosition currentPosition = environment.playerImplementationInternals.getCurrentPosition();
            ITimelinePosition startBound = environment.playerImplementationInternals.getCurrentStartBound();
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