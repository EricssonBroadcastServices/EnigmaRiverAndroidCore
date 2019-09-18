package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.analytics.AnalyticsException;
import com.redbeemedia.enigma.core.analytics.AnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.MainThreadTaskFactory;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/*package-protected*/ class InternalPlaybackSession implements IInternalPlaybackSession {
    private static final int CYCLE_SLEEP_MILLIS = 1000;
    private static final long HEARTBEAT_RATE_MILLIS = 60L*1000L;

    private final AnalyticsReporter analyticsReporter;
    private final AnalyticsHandler analyticsHandler;
    private final ITask analyticsHandlerTask;
    private final Repeater heartbeatRepeater;
    private final StreamInfo streamInfo;
    private final IStreamPrograms streamPrograms;
    private final ListenerCollector collector = new ListenerCollector();
    private final IPlaybackSessionInfo playbackSessionInfo;
    private boolean playingFromLive = false;
    private final OpenContainer<List<ISubtitleTrack>> subtitleTracks = new OpenContainer<>(null);
    private final OpenContainer<ISubtitleTrack> selectedSubtitleTrack = new OpenContainer<>(null);
    private final OpenContainer<List<IAudioTrack>> audioTracks = new OpenContainer<>(null);
    private final OpenContainer<IAudioTrack> selectedAudioTrack = new OpenContainer<>(null);
    private final OpenContainer<IContractRestrictions> contractRestrictions;
    private final OpenContainer<Boolean> seekAllowed = new OpenContainer<>(true);

    private static final int STATE_NEW = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_END_REACHED = 2;
    private static final int STATE_DEAD = 3;
    private volatile int state = STATE_NEW;

    private final IEnigmaPlayerListener playerListener;


    public InternalPlaybackSession(ConstructorArgs constructorArgs) {
        this(constructorArgs.session, constructorArgs.id, constructorArgs.timeProvider, constructorArgs.streamInfo, constructorArgs.playbackSessionInfo, constructorArgs.contractRestrictions);
    }

    public InternalPlaybackSession(ISession session, String id, ITimeProvider timeProvider, StreamInfo streamInfo, IPlaybackSessionInfo playbackSessionInfo, IContractRestrictions contractRestrictions) {
        this.playbackSessionInfo = playbackSessionInfo;
        this.streamInfo = streamInfo;
        this.streamPrograms = streamInfo.hasStreamPrograms() ? new StreamPrograms(streamInfo) : null;
        this.contractRestrictions = new OpenContainer<>(contractRestrictions);
        this.analyticsHandler = new AnalyticsHandler(session, id, timeProvider);
        ITaskFactory taskFactory = EnigmaRiverContext.getTaskFactory();
        this.analyticsHandlerTask = taskFactory.newTask(new Runnable() {
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
                            Thread.sleep(CYCLE_SLEEP_MILLIS);
                        } catch (AnalyticsException e) {
                            handleException(e);
                            Thread.sleep(CYCLE_SLEEP_MILLIS);
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
        });
        this.analyticsReporter = new AnalyticsReporter(timeProvider, analyticsHandler);
        this.playerListener = new EnigmaPlayerListenerForAnalytics(analyticsReporter, playbackSessionInfo, streamInfo);
        ITaskFactory mainThreadTaskFactory = new MainThreadTaskFactory();
        heartbeatRepeater = new Repeater(mainThreadTaskFactory, HEARTBEAT_RATE_MILLIS, new HeartbeatRunnable());

        updateSeekAllowed(contractRestrictions);
        addListener(new BasePlaybackSessionListener() {
            @Override
            public void onContractRestrictionsChanged(IContractRestrictions oldContractRestrictions, IContractRestrictions newContractRestrictions) {
                updateSeekAllowed(newContractRestrictions);
            }
        });
    }

    private void changeState(int newState) {
        int oldState = state;
        state = newState;
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
        try {
            analyticsHandlerTask.start();
        } catch (TaskException e) {
            throw new RuntimeException("Could not start analyticsHandlerTask", e);
        }
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
        if(aborted) {
            analyticsReporter.playbackAborted(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
        }
        enigmaPlayer.removeListener(playerListener);
        try {
            analyticsHandlerTask.cancel(500);
        } catch (TaskException e) {
            e.printStackTrace(); //Suppress
        }
        ITask sendRemainingDataTask = EnigmaRiverContext.getTaskFactory().newTask(() -> {
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

    @Override
    public StreamInfo getStreamInfo() {
        return streamInfo;
    }

    @Override
    public IStreamPrograms getStreamPrograms() {
        return streamPrograms;
    }

    @Override
    public void setPlayingFromLive(boolean live) {
        if(!streamInfo.isLiveStream()) {
            live = false;
        }
        if(live != playingFromLive) {
            playingFromLive = live;
            collector.onPlayingFromLiveChanged(live);
        }
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
    public boolean isPlayingFromLive() {
        return streamInfo.isLiveStream() && playingFromLive;
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
        public final ISession session;
        public final String id;
        public final ITimeProvider timeProvider;
        public final StreamInfo streamInfo;
        public final IPlaybackSessionInfo playbackSessionInfo;
        public final IContractRestrictions contractRestrictions;

        public ConstructorArgs(ISession session, String id, ITimeProvider timeProvider, StreamInfo streamInfo, IPlaybackSessionInfo playbackSessionInfo, IContractRestrictions contractRestrictions) {
            this.session = session;
            this.id = id;
            this.timeProvider = timeProvider;
            this.streamInfo = streamInfo;
            this.playbackSessionInfo = playbackSessionInfo;
            this.contractRestrictions = contractRestrictions;
        }

        public static ConstructorArgs of(ISession session, JSONObject jsonObject, IPlaybackSessionInfo playbackSessionInfo, ITimeProvider timeProvider) throws JSONException {
            String playbackSessionId = jsonObject.optString("playSessionId", UUID.randomUUID().toString());
            StreamInfo streamInfo = new StreamInfo(jsonObject.optJSONObject("streamInfo"));
            EnigmaContractRestrictions contractRestrictions = EnigmaContractRestrictions.createWithDefaults(jsonObject.optJSONObject("contractRestrictions"));
            return new ConstructorArgs(session, playbackSessionId, timeProvider, streamInfo, playbackSessionInfo, contractRestrictions);
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
        private final AnalyticsReporter analyticsReporter;
        private final IPlaybackSessionInfo playbackSessionInfo;
        private final StreamInfo streamInfo;
        private boolean hasStartedAtLeastOnce = false;

        public EnigmaPlayerListenerForAnalytics(AnalyticsReporter analyticsReporter, IPlaybackSessionInfo playbackSessionInfo, StreamInfo streamInfo) {
            this.analyticsReporter = analyticsReporter;
            this.playbackSessionInfo = playbackSessionInfo;
            this.streamInfo = streamInfo;
        }

        @Override
        public void onPlaybackError(Error error) {
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
                    analyticsReporter.playbackResumed(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
                } else {
                    hasStartedAtLeastOnce = true;
                    long playbackOffset = getCurrentPlaybackOffset(playbackSessionInfo, streamInfo);
                    String playMode = streamInfo.getPlayMode();
                    String mediaLocator = playbackSessionInfo.getMediaLocator();
                    Long referenceTime = streamInfo.isLiveStream() ? streamInfo.getStartUtcSeconds()*1000L : null;
                    analyticsReporter.playbackStarted(playbackOffset, playMode, mediaLocator, referenceTime);
                }
            } else if(to == EnigmaPlayerState.PAUSED) {
                if(hasStartedAtLeastOnce) {
                    analyticsReporter.playbackPaused(getCurrentPlaybackOffset(playbackSessionInfo, streamInfo));
                }
            }
        }
    }

    private static long getCurrentPlaybackOffset(IPlaybackSessionInfo playbackSessionInfo, StreamInfo streamInfo) {
        long playbackOffset = playbackSessionInfo.getCurrentPlaybackOffset().inWholeUnits(Duration.Unit.MILLISECONDS);
        long startUtcSeconds = streamInfo.hasStartUtcSeconds() ? streamInfo.getStartUtcSeconds() : 0;
        long startUtcMillis = startUtcSeconds*1000L;
        long sum = playbackOffset+startUtcMillis;
        return sum;
    }
}
