package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.context.AppForegroundListener;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.AndroidThreadUtil;
import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;
import com.redbeemedia.enigma.core.video.IVideoTrack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*package-protected*/ class InternalPlaybackSession implements IInternalPlaybackSession {
    private static final long HEARTBEAT_RATE_MILLIS = 60L * 1000L;

    //Task ids
    private static final String HEARTBEAT_REPEATER = "heartbeatRepeater";
    private static final String APP_FOREGROUND_MONITOR = "appForegroundMonitor";

    private final ComponentsHandler componentsHandler = new ComponentsHandler();
    private final IAnalyticsReporter analyticsReporter;
    private final AppForegroundMonitor appForegroundMonitor;
    private final Repeater heartbeatRepeater;
    private final IStreamInfo streamInfo;
    private final IStreamPrograms streamPrograms;
    private final IDrmInfo drmInfo;
    private final ListenerCollector collector = new ListenerCollector();
    private final IPlaybackSessionInfo playbackSessionInfo;
    private final IEnigmaPlayerConnection.ICommunicationsChannel communicationChannel = new EnigmaPlayerCommunicationChannel();
    private final OpenContainer<Boolean> playingFromLive = new OpenContainer<>(false);
    private final OpenContainer<List<ISubtitleTrack>> subtitleTracks = new OpenContainer<>(new ArrayList<>());
    private final OpenContainer<ISubtitleTrack> selectedSubtitleTrack = new OpenContainer<>(null);
    private final OpenContainer<List<IAudioTrack>> audioTracks = new OpenContainer<>(new ArrayList<>());
    private final OpenContainer<IAudioTrack> selectedAudioTrack = new OpenContainer<>(null);
    private final OpenContainer<IVideoTrack> selectedVideoTrack = new OpenContainer<>(null);
    private final OpenContainer<IContractRestrictions> contractRestrictions;
    private final OpenContainer<Boolean> seekAllowed = new OpenContainer<>(true);

    private static final int STATE_NEW = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_END_REACHED = 2;
    private static final int STATE_DEAD = 3;
    private volatile int state = STATE_NEW;

    private final IEnigmaPlayerListener playerListener;


    public InternalPlaybackSession(ConstructorArgs constructorArgs) {
        this(constructorArgs.streamInfo, constructorArgs.streamPrograms, constructorArgs.playbackSessionInfo, constructorArgs.contractRestrictions, constructorArgs.drmInfo, constructorArgs.analyticsReporter);
    }

    public InternalPlaybackSession(IStreamInfo streamInfo, IStreamPrograms streamPrograms, IPlaybackSessionInfo playbackSessionInfo, IContractRestrictions contractRestrictions, IDrmInfo drmInfo, IAnalyticsReporter analyticsReporter) {
        this.playbackSessionInfo = playbackSessionInfo;
        this.streamInfo = streamInfo;
        this.streamPrograms = streamPrograms;
        this.drmInfo = drmInfo;
        this.contractRestrictions = new OpenContainer<>(contractRestrictions);
        this.analyticsReporter = analyticsReporter;
        this.playerListener = new EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, streamInfo, selectedVideoTrack);
        this.heartbeatRepeater = new Repeater(getTaskFactory(HEARTBEAT_REPEATER), HEARTBEAT_RATE_MILLIS, new HeartbeatRunnable());
        this.appForegroundMonitor = new AppForegroundMonitor(getTaskFactory(APP_FOREGROUND_MONITOR));

        updateSeekAllowed(contractRestrictions);
        addListener(new BasePlaybackSessionListener() {
            @Override
            public void onContractRestrictionsChanged(IContractRestrictions oldContractRestrictions, IContractRestrictions newContractRestrictions) {
                updateSeekAllowed(newContractRestrictions);
            }
        });
    }

    private ITaskFactory getTaskFactory(String user) {
        ITaskFactoryProvider taskFactoryProvider = getTaskFactoryProvider();
        if(HEARTBEAT_REPEATER.equals(user)) {
            return taskFactoryProvider.getMainThreadTaskFactory();
        } else if(APP_FOREGROUND_MONITOR.equals(user)) {
            return taskFactoryProvider.getMainThreadTaskFactory();
        } else {
            throw new IllegalArgumentException(user+" not handled");
        }
    }

    protected ITaskFactoryProvider getTaskFactoryProvider() {
        return EnigmaRiverContext.getTaskFactoryProvider();
    }

    private void changeState(int newState) {
        int oldState = state;
        state = newState;
    }

    @Override
    public IEnigmaPlayerConnection getPlayerConnection() {
        return (IEnigmaPlayerConnection) communicationChannel;
    }

    @Override
    public IDrmInfo getDrmInfo() {
        return drmInfo;
    }

    @Override
    public void addInternalListener(IInternalPlaybackSessionListener listener) {
        componentsHandler.listeners.add(listener);
    }

    @Override
    public void onStart(IEnigmaPlayer enigmaPlayer) {
        if(state != STATE_NEW) {
            throw new IllegalStateException("state="+String.valueOf(state));
        } else {
            changeState(STATE_STARTED);
        }
        analyticsReporter.deviceInfo();
        analyticsReporter.playbackCreated(playbackSessionInfo.getAssetId());
        heartbeatRepeater.setEnabled(true);
        enigmaPlayer.addListener(playerListener);
        componentsHandler.fireOnStart(new IInternalPlaybackSessionListener.OnStartArgs(this, enigmaPlayer, communicationChannel));
        appForegroundMonitor.onPlaybackSessionStart();
    }

    @Override
    public void onStop(IEnigmaPlayer enigmaPlayer) {
        boolean aborted = false;
        if(state != STATE_STARTED && state != STATE_END_REACHED) {
            throw new IllegalStateException("state="+String.valueOf(state));
        } else {
            if(state != STATE_END_REACHED) {
                aborted = true;
            }
            changeState(STATE_DEAD);
        }
        heartbeatRepeater.setEnabled(false);
        if (aborted) {
            analyticsReporter.playbackAborted(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
        }
        enigmaPlayer.removeListener(playerListener);
        appForegroundMonitor.onPlaybackSessionStop();
        componentsHandler.fireOnStop(new IInternalPlaybackSessionListener.OnStopArgs(this, enigmaPlayer));
    }

    @Override
    public IStreamInfo getStreamInfo() {
        return streamInfo;
    }

    @Override
    public IStreamPrograms getStreamPrograms() {
        return streamPrograms;
    }

    @Override
    public IPlaybackSessionInfo getPlaybackSessionInfo() {
        return playbackSessionInfo;
    }

    @Override
    public void setPlayingFromLive(boolean live) {
        if(!streamInfo.isLiveStream()) {
            live = false;
        }
        OpenContainerUtil.setValueSynchronized(playingFromLive, live, (oldValue, newValue) -> collector.onPlayingFromLiveChanged(newValue));
    }

    @Override
    public void setTracks(Collection<? extends IPlayerImplementationTrack> tracks) {
        Collection<TracksUtil.TracksUpdate<?>> tracksUpdates = new ArrayList<>();
        tracksUpdates.add(new TracksUtil.TracksUpdate<>(
                track -> track.asSubtitleTrack(),
                collector::onSubtitleTracks,
                subtitleTracks));
        tracksUpdates.add(new TracksUtil.TracksUpdate<>(
                track -> track.asAudioTrack(),
                collector::onAudioTracks,
                audioTracks));

        //Filter out matching tracks
        for(IPlayerImplementationTrack track : tracks) {
            for(TracksUtil.TracksUpdate<?> tracksUpdate : tracksUpdates) {
                tracksUpdate.onPossibleNew(track);
            }
        }

        //Update list in a thread-safe manner
        for(TracksUtil.TracksUpdate<?> tracksUpdate : tracksUpdates) {
            tracksUpdate.update();
        }

        //Fire events to listeners if value changed
        for(TracksUtil.TracksUpdate<?> tracksUpdate : tracksUpdates) {
            tracksUpdate.fireIfChanged();
        }
    }

    private static <T> List<T> getUnmodifiableViewOf(OpenContainer<List<T>> container) {
        synchronized (container) {
            if(container.value != null) {
                return Collections.unmodifiableList(container.value);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<ISubtitleTrack> getSubtitleTracks() {
        return getUnmodifiableViewOf(subtitleTracks);
    }

    @Override
    public ISubtitleTrack getSelectedSubtitleTrack() {
        return OpenContainerUtil.getValueSynchronized(selectedSubtitleTrack);
    }

    @Override
    public void setSelectedSubtitleTrack(ISubtitleTrack track) {
        OpenContainerUtil.setValueSynchronized(selectedSubtitleTrack, track, collector::onSelectedSubtitleTrackChanged);
    }

    @Override
    public List<IAudioTrack> getAudioTracks() {
        return getUnmodifiableViewOf(audioTracks);
    }

    @Override
    public IAudioTrack getSelectedAudioTrack() {
        return OpenContainerUtil.getValueSynchronized(selectedAudioTrack);
    }

    @Override
    public void setSelectedAudioTrack(IAudioTrack track) {
        OpenContainerUtil.setValueSynchronized(selectedAudioTrack, track, collector::onSelectedAudioTrackChanged);
    }

    @Override
    public void setSelectedVideoTrack(IVideoTrack track) {
        OpenContainerUtil.setValueSynchronized(selectedVideoTrack, track, (oldValue, newValue) -> {
            if(oldValue != null  // If oldValue was null this was the initial value and thus not a change
               && newValue != null) {
                analyticsReporter.playbackBitrateChanged(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo), newValue.getBitrate());
            }
        });
    }

    @Override
    public IPlayable getPlayable() {
        return playbackSessionInfo.getPlayable();
    }

    @Override
    public void fireEndReached() {
        if(state != STATE_STARTED) {
            throw new IllegalStateException("state="+String.valueOf(state));
        } else {
            changeState(STATE_END_REACHED);
        }
        analyticsReporter.playbackCompleted(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
        collector.onEndReached();
    }

    @Override
    public void fireSeekCompleted() {
        analyticsReporter.playbackScrubbedTo(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
    }

    @Override
    public boolean isPlayingFromLive() {
        return OpenContainerUtil.getValueSynchronized(playingFromLive);
    }

    @Override
    public boolean isSeekAllowed() {
        return OpenContainerUtil.getValueSynchronized(seekAllowed);
    }

    private void updateSeekAllowed(IContractRestrictions contractRestrictions) {
        boolean allowed = contractRestrictions.getValue(ContractRestriction.TIMESHIFT_ENABLED, true);
        OpenContainerUtil.setValueSynchronized(seekAllowed, allowed, null);
    }

    @Override
    public boolean isSeekToLiveAllowed() {
        return isSeekAllowed() && streamInfo.isLiveStream();
    }

    @Override
    public IContractRestrictions getContractRestrictions() {
        return OpenContainerUtil.getValueSynchronized(contractRestrictions);
    }

    @Override
    public void setContractRestrictions(IContractRestrictions newContractRestrictions) {
        if(newContractRestrictions == null) {
            throw new NullPointerException();
        }
        OpenContainerUtil.setValueSynchronized(contractRestrictions, newContractRestrictions, (oldValue, newValue) -> collector.onContractRestrictionsChanged(oldValue, newValue));
    }

    @Override
    public void addListener(IPlaybackSessionListener listener) {
        collector.addListener(listener);
    }

    @Override
    public void addListener(IPlaybackSessionListener listener, Handler handler) {
        collector.addListener(listener, new HandlerWrapper(handler));
    }

    @Override
    public void removeListener(IPlaybackSessionListener listener) {
        collector.removeListener(listener);
    }

    private static class ListenerCollector extends Collector<IPlaybackSessionListener> implements IPlaybackSessionListener {
        public ListenerCollector() {
            super(IPlaybackSessionListener.class);
        }

        @Override
        public void _dont_implement_IPlaybackSessionListener___instead_extend_BasePlaybackSessionListener_() {
            //We want compile time errors here if a new event is added, thus we implement the interface directly.
        }

        @Override
        public void onPlayingFromLiveChanged(boolean live) {
            forEach(listener -> listener.onPlayingFromLiveChanged(live));
        }

        @Override
        public void onEndReached() {
            forEach(listener -> listener.onEndReached());
        }

        @Override
        public void onSubtitleTracks(List<ISubtitleTrack> tracks) {
            forEach(listener -> listener.onSubtitleTracks(tracks));
        }

        @Override
        public void onSelectedSubtitleTrackChanged(ISubtitleTrack oldSelectedTrack, ISubtitleTrack newSelectedTrack) {
            forEach(listener -> listener.onSelectedSubtitleTrackChanged(oldSelectedTrack, newSelectedTrack));
        }

        @Override
        public void onAudioTracks(List<IAudioTrack> tracks) {
            forEach(listener -> listener.onAudioTracks(tracks));
        }

        @Override
        public void onSelectedAudioTrackChanged(IAudioTrack oldSelectedTrack, IAudioTrack newSelectedTrack) {
            forEach(listener -> listener.onSelectedAudioTrackChanged(oldSelectedTrack, newSelectedTrack));
        }

        @Override
        public void onContractRestrictionsChanged(IContractRestrictions oldContractRestrictions, IContractRestrictions newContractRestrictions) {
            forEach(listener -> listener.onContractRestrictionsChanged(oldContractRestrictions, newContractRestrictions));
        }
    }

    /*package-protected*/ static class ConstructorArgs {
        public final IStreamInfo streamInfo;
        public final IStreamPrograms streamPrograms;
        public final IPlaybackSessionInfo playbackSessionInfo;
        public final IContractRestrictions contractRestrictions;
        public final IDrmInfo drmInfo;
        public final IAnalyticsReporter analyticsReporter;

        public ConstructorArgs(IStreamInfo streamInfo, IStreamPrograms streamPrograms, IPlaybackSessionInfo playbackSessionInfo, IContractRestrictions contractRestrictions, IDrmInfo drmInfo, IAnalyticsReporter analyticsReporter) {
            this.streamInfo = streamInfo;
            this.streamPrograms = streamPrograms;
            this.playbackSessionInfo = playbackSessionInfo;
            this.contractRestrictions = contractRestrictions;
            this.drmInfo = drmInfo;
            this.analyticsReporter = analyticsReporter;
        }
    }

    private class HeartbeatRunnable implements Runnable {
        private Long lastOffset = null;

        @Override
        public void run() {
            try {
                lastOffset = getCurrentPlaybackOffset(playbackSessionInfo, streamInfo);
            } catch (Exception e) {
                //Ignore
            }
            if(lastOffset != null) {
                analyticsReporter.playbackHeartbeat(lastOffset.longValue());
            }
        }
    }

    /*package-protected*/ static class EnigmaPlayerListenerForAnalytics extends BaseEnigmaPlayerListener {
        private final IAnalyticsReporter analyticsReporter;
        private final IPlaybackSessionInfo playbackSessionInfo;
        private final IStreamInfo streamInfo;
        private boolean hasStartedAtLeastOnce = false;
        private final OpenContainer<IVideoTrack> selectedVideoTrack;
        private boolean programTrackingLost = false;

        public EnigmaPlayerListenerForAnalytics(IAnalyticsReporter analyticsReporter, IPlaybackSessionInfo playbackSessionInfo, IStreamInfo streamInfo, OpenContainer<IVideoTrack> selectedVideoTrack) {
            this.analyticsReporter = analyticsReporter;
            this.playbackSessionInfo = playbackSessionInfo;
            this.streamInfo = streamInfo;
            this.selectedVideoTrack = selectedVideoTrack;
        }

        @Override
        public void onPlaybackError(EnigmaError error) {
            analyticsReporter.playbackError(error);
        }

        @Override
        public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
            if(to == EnigmaPlayerState.LOADING) {
                analyticsReporter.playbackHandshakeStarted(playbackSessionInfo.getAssetId());
            } else if(to == EnigmaPlayerState.LOADED) {
                analyticsReporter.playbackPlayerReady(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo),
                        playbackSessionInfo.getPlayerTechnologyName(),
                        playbackSessionInfo.getPlayerTechnologyVersion());
            } else if(to == EnigmaPlayerState.PLAYING) {
                if(hasStartedAtLeastOnce) {
                    if(from != EnigmaPlayerState.BUFFERING) {
                        analyticsReporter.playbackResumed(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
                    }
                } else {
                    hasStartedAtLeastOnce = true;
                    long playbackOffset = getCurrentPlaybackOffset(playbackSessionInfo, streamInfo);
                    String playMode = streamInfo.getPlayMode();
                    String mediaLocator = playbackSessionInfo.getMediaLocator();
                    Long referenceTime = streamInfo.isLiveStream() ? streamInfo.getStart(Duration.Unit.MILLISECONDS) : null;
                    IVideoTrack videoTrack = OpenContainerUtil.getValueSynchronized(selectedVideoTrack);
                    Integer bitrate = null;
                    if(videoTrack != null) {
                        int videoTrackBitrate = videoTrack.getBitrate();
                        if(videoTrackBitrate != -1) {
                            bitrate = videoTrackBitrate;
                        }
                    }
                    String programId = playbackSessionInfo.getCurrentProgramId();
                    analyticsReporter.playbackStarted(playbackOffset, playMode, mediaLocator, referenceTime, bitrate, programId);
                }
            } else if(to == EnigmaPlayerState.PAUSED) {
                if(hasStartedAtLeastOnce) {
                    analyticsReporter.playbackPaused(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
                }
            } else if(to == EnigmaPlayerState.BUFFERING) {
                analyticsReporter.playbackBufferingStarted(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
            }

            if(from == EnigmaPlayerState.BUFFERING) {
                analyticsReporter.playbackBufferingStopped(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
            }
        }

        @Override
        public void onProgramChanged(IProgram from, IProgram to) {
            if(from != null || programTrackingLost) {
                programTrackingLost = (to == null);
                String programId = null;
                if(to != null) {
                    programId = to.getProgramId();
                }
                analyticsReporter.playbackProgramChanged(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo), programId);
            }
        }
    }

    private static long getCurrentPlaybackOffset(IPlaybackSessionInfo playbackSessionInfo, IStreamInfo streamInfo) {
        long playbackOffset = playbackSessionInfo.getCurrentPlaybackOffset().inWholeUnits(Duration.Unit.MILLISECONDS);
        long startUtcMillis = streamInfo.hasStart() ? streamInfo.getStart(Duration.Unit.MILLISECONDS) : 0;
        long sum = playbackOffset+startUtcMillis;
        return sum;
    }

    private static class EnigmaPlayerCommunicationChannel implements IEnigmaPlayerConnection, IEnigmaPlayerConnection.ICommunicationsChannel {
        private final OpenContainer<IEnigmaPlayerConnection.ICommunicationsChannel> communicationsChannel = new OpenContainer<>(null);

        @Override
        public void openConnection(ICommunicationsChannel communicationsChannel) {
            OpenContainerUtil.setValueSynchronized(this.communicationsChannel, communicationsChannel, null);
        }

        @Override
        public void severConnection() {
            OpenContainerUtil.setValueSynchronized(this.communicationsChannel, null, null);
        }

        private void ifConnectionOpen(ICommunicationsChannelAction action) {
            ICommunicationsChannel activeChannel = OpenContainerUtil.getValueSynchronized(communicationsChannel);
            if(activeChannel != null) {
                action.onCommunicationsChannel(activeChannel);
            }
        }

        @Override
        public void onPlaybackError(EnigmaError error, boolean endStream) {
            ifConnectionOpen(channel -> channel.onPlaybackError(error, endStream));
        }

        @Override
        public void onExpirePlaybackSession(PlaybackSessionSeed playbackSessionSeed) {
            ifConnectionOpen(channel -> channel.onExpirePlaybackSession(playbackSessionSeed));
        }

        private interface ICommunicationsChannelAction {
            void onCommunicationsChannel(ICommunicationsChannel channel);
        }
    }

    private class AppForegroundMonitor extends AppForegroundListener {
        private final Duration GRACE_PERIOD = Duration.minutes(10);

        private final ITaskFactory taskFactory;
        private final OpenContainer<Boolean> inForeground = new OpenContainer<>(true);
        private ITask gracePeriodEndTask = null;

        public AppForegroundMonitor(ITaskFactory taskFactory) {
            this.taskFactory = taskFactory;
        }

        @Override
        public void onForegrounded() {
            AndroidThreadUtil.runOnUiThread(() -> OpenContainerUtil.setValueSynchronized(inForeground, true, (oldValue, newValue) -> {
                cancelOngoingGracePeriod(200L);
                analyticsReporter.playbackAppResumed(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
            }));
        }

        @Override
        public void onBackgrounded() {
            AndroidThreadUtil.runOnUiThread(() -> OpenContainerUtil.setValueSynchronized(inForeground, false, (oldValue, newValue) -> {
                try {
                    analyticsReporter.playbackAppBackgrounded(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
                    cancelOngoingGracePeriod(1);
                    gracePeriodEndTask = taskFactory.newTask(new Runnable() {
                        @Override
                        public void run() {
                            analyticsReporter.playbackGracePeriodEnded(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
                            communicationChannel.onExpirePlaybackSession(new PlaybackSessionSeed(playbackSessionInfo));
                        }
                    });
                    gracePeriodEndTask.startDelayed(GRACE_PERIOD.inWholeUnits(Duration.Unit.MILLISECONDS));
                } catch (Exception e) {
                    analyticsReporter.playbackError(new UnexpectedError(e));
                }
            }));
        }

        private void cancelOngoingGracePeriod(long waitForJoin) {
            if(gracePeriodEndTask != null) {
                try {
                    gracePeriodEndTask.cancel(waitForJoin);
                } catch (TaskException e) {
                    e.printStackTrace(); //Best effort
                }
                gracePeriodEndTask = null;
            }
        }

        public void onPlaybackSessionStart() {
            this.startListening();
        }

        public void onPlaybackSessionStop() {
            this.stopListening();
            cancelOngoingGracePeriod(500);
        }
    }

    private static class ComponentsHandler {
        private final List<IInternalPlaybackSessionListener> listeners = new ArrayList<>();

        public void fireOnStart(IInternalPlaybackSessionListener.OnStartArgs args) {
            for(int i = 0; i < listeners.size(); ++i) {
                listeners.get(i).onStart(args);
            }
        }

        public void fireOnStop(IInternalPlaybackSessionListener.OnStopArgs args) {
            for(int i = listeners.size()-1; i >= 0; --i) {
                listeners.get(i).onStop(args);
            }
        }
    }
}
