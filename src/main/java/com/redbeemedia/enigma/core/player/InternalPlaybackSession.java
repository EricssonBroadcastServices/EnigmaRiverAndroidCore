package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.analytics.AnalyticsException;
import com.redbeemedia.enigma.core.analytics.AnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.MainThreadTaskFactory;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.HandlerWrapper;

import org.json.JSONException;

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

    private static final int STATE_NEW = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_END_REACHED = 2;
    private static final int STATE_DEAD = 3;
    private volatile int state = STATE_NEW;

    private final IEnigmaPlayerListener playerListener;


    public InternalPlaybackSession(ConstructorArgs constructorArgs) {
        this(constructorArgs.session, constructorArgs.id, constructorArgs.timeProvider, constructorArgs.streamInfo, constructorArgs.playbackSessionInfo);
    }

    public InternalPlaybackSession(ISession session, String id, ITimeProvider timeProvider, StreamInfo streamInfo, IPlaybackSessionInfo playbackSessionInfo) {
        this.playbackSessionInfo = playbackSessionInfo;
        this.streamInfo = streamInfo;
        this.streamPrograms = streamInfo.hasStreamPrograms() ? new StreamPrograms(streamInfo) : null;
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
        return true;
    }

    @Override
    public boolean isSeekToLiveAllowed() {
        return isSeekAllowed() && streamInfo.isLiveStream();
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
    }

    /*package-protected*/ static class ConstructorArgs {
        public final ISession session;
        public final String id;
        public final ITimeProvider timeProvider;
        public final StreamInfo streamInfo;
        public final IPlaybackSessionInfo playbackSessionInfo;

        public ConstructorArgs(ISession session, String id, ITimeProvider timeProvider, StreamInfo streamInfo, IPlaybackSessionInfo playbackSessionInfo) {
            this.session = session;
            this.id = id;
            this.timeProvider = timeProvider;
            this.streamInfo = streamInfo;
            this.playbackSessionInfo = playbackSessionInfo;
        }

        public static ConstructorArgs of(IPlaybackSessionFactory.PlaybackSessionArgs pbsArgs, ITimeProvider timeProvider) throws JSONException {
            String playbackSessionId = pbsArgs.jsonObject.optString("playSessionId", UUID.randomUUID().toString());
            StreamInfo streamInfo = new StreamInfo(pbsArgs.jsonObject.optJSONObject("streamInfo"));
            return new ConstructorArgs(pbsArgs.session, playbackSessionId, timeProvider, streamInfo, pbsArgs.playbackSessionInfo);
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
