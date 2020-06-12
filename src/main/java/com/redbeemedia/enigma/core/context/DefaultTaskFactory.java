package com.redbeemedia.enigma.core.context;

import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/*package-protected*/ class DefaultTaskFactory implements ITaskFactory {
    private final ThreadFactory threadFactory = Executors.defaultThreadFactory();

    @Override
    public ITask newTask(Runnable runnable) {
        return new Task(runnable, threadFactory);
    }

    private class Task implements ITask {
        private final ThreadFactory threadFactory;
        private Thread thread;
        private volatile Thread delayThread = null;
        private volatile boolean started = false;
        private volatile boolean cancelRequested = false;
        private volatile boolean canceled = false;

        public Task(Runnable runnable, ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            this.thread = threadFactory.newThread(runnable);
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
            delayThread = threadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException e) {
                        //Task was cancelled
                        return;
                    }
                    if(!cancelRequested) {
                        Task.this.start();
                    }
                }
            });
            delayThread.start();
        }

        @Override
        public synchronized void cancel(long joinMillis) throws IllegalStateException {
            if(thread == Thread.currentThread()) { //If a task tries to cancel itself then we ignore it.
                return;
            }
            this.cancelRequested = true;

            //Cancel delayThread
            if(delayThread != null) {
                delayThread.interrupt();
                try {
                    delayThread.join(joinMillis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

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
