package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.analytics.AnalyticsException;
import com.redbeemedia.enigma.core.analytics.AnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.time.ITimeProvider;

/*package-protected*/ class PlaybackSession implements IPlaybackSession {
    private static final int CYCLE_SLEEP_MILLIS = 1000;

    private final AnalyticsReporter analyticsReporter;
    private final AnalyticsHandler analyticsHandler;
    private final ITask analyticsHandlerTask;

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

    public PlaybackSession(ISession session, String id, ITimeProvider timeProvider) {
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

    @Override
    public void onStart(IEnigmaPlayer enigmaPlayer) {
        if(state != STATE_NEW) {
            throw new IllegalStateException("state="+String.valueOf(state));
        } else {
            state = STATE_STARTED;
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
            state = STATE_DEAD;
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
}
