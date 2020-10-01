package com.redbeemedia.enigma.core.context;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.AndroidThreadUtil;
import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IInternalListener;

/**
 * Big portions of this class was shamelessly copied from android.arch.lifecycle.ProcessLifecycleOwner to minimize dependencies.
 */
/*package-protected*/ final class ProcessLifecycleHandler {
    private static final ProcessLifecycleHandler instance = new ProcessLifecycleHandler();

    private static final Duration TIMEOUT_MS = Duration.millis(700);

    private int startedCounter = 0;
    private int resumedCounter = 0;

    private boolean pauseSent = true;
    private boolean stopSent = true;

    private Handler handler;

    private final ApplicationLifecycleCollector collector = new ApplicationLifecycleCollector();

    private final Runnable delayedPauseRunnable = new Runnable() {
        @Override
        public void run() {
            dispatchPauseIfNeeded();
            dispatchStopIfNeeded();
        }
    };

    private ProcessLifecycleHandler() {
    }


    public static ProcessLifecycleHandler get() {
        return instance;
    }

    public void initialize(Application application) {
        if(application != null) {
            android.util.Log.d("freezelog", "ProcessLifecycleHandler 1");
            this.handler = new Handler();
            android.util.Log.d("freezelog", "ProcessLifecycleHandler 1.1");
            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 2");
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 3");
                    ProcessLifecycleHandler.this.activityStarted();
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 4");
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 5");
                    ProcessLifecycleHandler.this.activityResumed();
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 6");
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 7");
                    ProcessLifecycleHandler.this.activityPaused();
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 8");
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 9");
                    ProcessLifecycleHandler.this.activityStopped();
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 10");
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 11");
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    android.util.Log.d("freezelog", "ProcessLifecycleHandler 12");
                }
            });
        }
    }

    private void activityStarted() {
        startedCounter++;
        if (startedCounter == 1 && stopSent) {
            collector.onStarted();
            stopSent = false;
        }
    }

    private void activityResumed() {
        resumedCounter++;
        if (resumedCounter == 1) {
            if (pauseSent) {
                collector.onResumed();
                pauseSent = false;
            } else {
                handler.removeCallbacks(delayedPauseRunnable);
            }
        }
    }

    private void activityPaused() {
        resumedCounter--;
        if (resumedCounter == 0) {
            handler.postDelayed(delayedPauseRunnable, TIMEOUT_MS.inWholeUnits(Duration.Unit.MILLISECONDS));
        }
    }

    private void activityStopped() {
        startedCounter--;
        dispatchStopIfNeeded();
    }

    private void dispatchPauseIfNeeded() {
        if (resumedCounter == 0) {
            pauseSent = true;
            collector.onPaused();
        }
    }

    private void dispatchStopIfNeeded() {
        if (startedCounter == 0 && pauseSent) {
            collector.onStopped();
            stopSent = true;
        }
    }

    public boolean isStarted() {
        AndroidThreadUtil.verifyCalledFromUiThread();
        return startedCounter > 0;
    }

    public boolean isResumed() {
        AndroidThreadUtil.verifyCalledFromUiThread();
        return resumedCounter > 0;
    }

    public boolean addListener(IApplicationLifecycleListener applicationLifecycleListener) {
        return collector.addListener(applicationLifecycleListener);
    }

    public boolean addListener(IApplicationLifecycleListener applicationLifecycleListener, Handler handler) {
        return collector.addListener(applicationLifecycleListener, new HandlerWrapper(handler));
    }

    public boolean removeListener(IApplicationLifecycleListener applicationLifecycleListener) {
        return collector.removeListener(applicationLifecycleListener);
    }

    public interface IApplicationLifecycleListener extends IInternalListener {
        void onStarted();
        void onResumed();
        void onPaused();
        void onStopped();
    }

    private static class ApplicationLifecycleCollector extends Collector<IApplicationLifecycleListener> implements IApplicationLifecycleListener {
        public ApplicationLifecycleCollector() {
            super(IApplicationLifecycleListener.class);
        }

        @Override
        public void onStarted() {
            forEach(listener -> listener.onStarted());
        }

        @Override
        public void onResumed() {
            forEach(listener -> listener.onResumed());
        }

        @Override
        public void onPaused() {
            forEach(listener -> listener.onPaused());
        }

        @Override
        public void onStopped() {
            forEach(listener -> listener.onStopped());
        }
    }
}
