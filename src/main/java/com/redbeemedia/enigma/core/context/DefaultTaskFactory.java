package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;

/*package-protected*/ class DefaultTaskFactory implements ITaskFactory {
    @Override
    public ITask newTask(Runnable runnable) {
        return new ThreadTask(runnable);
    }

    private static class ThreadTask implements ITask {
        private Thread thread;
        private volatile boolean started = false;
        private volatile boolean cancelRequested = false;
        private volatile boolean canceled = false;

        public ThreadTask(Runnable runnable) {
            this.thread = new Thread(runnable);
        }

        @Override
        public synchronized void start() throws IllegalStateException {
            if(!cancelRequested) {
                if(started) {
                    throw new IllegalStateException("Already started");
                } else {
                    this.thread.start();
                    started = true;
                }
            }
        }

        @Override
        public void startDelayed(long delayMillis) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ThreadTask.this.start();
                }
            }).start();
        }

        @Override
        public synchronized void cancel(long joinMillis) throws IllegalStateException {
            this.cancelRequested = true;
            if(started && !canceled) {
                thread.interrupt();
                try {
                    thread.join(joinMillis);
                    this.canceled = true;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
