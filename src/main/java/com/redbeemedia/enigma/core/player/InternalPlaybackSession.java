package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.analytics.AnalyticsException;
import com.redbeemedia.enigma.core.analytics.AnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.HandlerWrapper;

/*package-protected*/ class InternalPlaybackSession implements IInternalPlaybackSession {
    private static final int CYCLE_SLEEP_MILLIS = 1000;

    private final AnalyticsReporter analyticsReporter;
    private final AnalyticsHandler analyticsHandler;
    private final ITask analyticsHandlerTask;
    private final StreamInfo streamInfo;
    private final ListenerCollector collector = new ListenerCollector();
    private boolean playingFromLive = false;

    private static final int STATE_NEW = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_DEAD = 2;
    private volatile int state = STATE_NEW;

    private final IEnigmaPlayerListener playerListener = new BaseEnigmaPlayerListener() {
        @Override
        public void onPlaybackError(Error error) {
            analyticsReporter.error(error);
        }
    };

    public InternalPlaybackSession(ISession session, String id, ITimeProvider timeProvider, StreamInfo streamInfo) {
        this.streamInfo = streamInfo;
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
        enigmaPlayer.addListener(playerListener);
        try {
            analyticsHandlerTask.start();
        } catch (TaskException e) {
            throw new RuntimeException("Could not start analyticsHandlerTask", e);
        }
    }

    @Override
    public void onStop(IEnigmaPlayer enigmaPlayer) {
        if(state != STATE_STARTED) {
            throw new IllegalStateException("state="+String.valueOf(state));
        } else {
            changeState(STATE_DEAD);
        }
        enigmaPlayer.removeListener(playerListener);
        try {
            analyticsHandlerTask.cancel(500);
        } catch (TaskException e) {
            e.printStackTrace(); //Suppress
        }
        try {
            analyticsHandler.sendData(); //Try to send any remaining data.
        } catch (Exception e) {
            e.printStackTrace(); //Suppress
        }
        //Additional cleanup goes here.
    }

    @Override
    public StreamInfo getStreamInfo() {
        return streamInfo;
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
    }
}
