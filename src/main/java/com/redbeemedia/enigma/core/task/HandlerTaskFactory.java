package com.redbeemedia.enigma.core.task;

import android.os.Handler;

import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;

public class HandlerTaskFactory implements ITaskFactory {
    private IHandler handler;

    public HandlerTaskFactory(Handler handler) {
        this(new HandlerWrapper(handler));
    }

    public HandlerTaskFactory(IHandler handler) {
        this.handler = handler;
    }

    @Override
    public ITask newTask(Runnable runnable) {
        return new HandlerTask(runnable);
    }

    private class HandlerTask implements ITask {
        private volatile boolean canceled = false;
        private Runnable job;

        public HandlerTask(Runnable job) {
            this.job = job;
        }

        @Override
        public void start() throws TaskException {
            handler.post(getRunnable());
        }

        private Runnable getRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                    if(!canceled) {
                        job.run();
                    }
                }
            };
        }

        @Override
        public void startDelayed(long delayMillis) throws TaskException {
            handler.postDelayed(getRunnable(), delayMillis);
        }

        @Override
        public void cancel(long joinMillis) throws TaskException {
            canceled = true;
        }
    }
}
