package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.util.AndroidThreadUtil;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public abstract class AppForegroundListener {
    private final ProcessLifecycleHandler.IApplicationLifecycleListener listener = new ProcessLifecycleHandler.IApplicationLifecycleListener() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onResumed() {
            onForegrounded();
        }

        @Override
        public void onPaused() {
            onBackgrounded();
        }

        @Override
        public void onStopped() {

        }
    };


    public abstract void onForegrounded();

    public abstract void onBackgrounded();

    protected void startListening() {
        AndroidThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProcessLifecycleHandler.get().addListener(listener);
                if(!ProcessLifecycleHandler.get().isResumed()) {
                    onBackgrounded();
                }
            }
        });
    }

    protected void stopListening() {
        ProcessLifecycleHandler.get().removeListener(listener);
    }
}
