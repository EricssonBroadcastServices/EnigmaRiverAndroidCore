package com.redbeemedia.enigma.core.task;

/**
 * <p>
 * This class executes a runnable every <code>delayMillis</code> milliseconds while it is enabled.
 * The {@link Repeater} starts disabled.
 * </p>
 * <p>
 * Calling <code>executeNow()</code> will always result in an immediate
 * call of <code>runnable.run()</code>. If enabled, the next call to <code>runnable.run()</code>
 * will be triggered after <code>delayMillis</code> milliseconds.
 * </p>
 */
public class Repeater {
    private final Runnable runnable;

    private ITaskFactory taskFactory;
    private ITask queuedTask;
    private long delayMillis;
    private boolean enabled = false;

    public Repeater(ITaskFactory taskFactory, long delayMillis, Runnable runnable) {
        this.taskFactory = taskFactory;
        this.delayMillis = delayMillis;
        this.runnable = runnable;
    }

    public synchronized void setEnabled(boolean enabled) {
        boolean changed = (this.enabled != enabled);
        this.enabled = enabled;
        if(changed) {
            if(!enabled) {
                stop();
            } else {
                try {
                    taskFactory.newTask(() -> executeNow()).start();
                } catch (TaskException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public synchronized void executeNow() {
        if(queuedTask != null) {
            try {
                queuedTask.cancel(0);
            } catch (TaskException e) {
                e.printStackTrace();
            }
        }

        runnable.run();

        if(enabled) {
            final ITask[] me = new ITask[1];
            me[0] = taskFactory.newTask(new Runnable() {
                @Override
                public void run() {
                    synchronized (Repeater.this) {
                        if (me[0] == queuedTask && enabled) {
                            queuedTask = null;
                            executeNow();
                        }
                    }
                }
            });
            queuedTask = me[0];
            try {
                queuedTask.startDelayed(delayMillis);

            } catch (TaskException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void stop() {
        if(queuedTask != null) {
            try {
                queuedTask.cancel(0);
                queuedTask = null;
            } catch (TaskException e) {
                e.printStackTrace();
            }
        }
    }
}
