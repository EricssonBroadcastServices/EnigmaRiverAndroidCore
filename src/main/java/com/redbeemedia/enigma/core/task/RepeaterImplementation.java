package com.redbeemedia.enigma.core.task;

// TODO remove this class
/*package-protected*/ class RepeaterImplementation implements IRepeaterImplementation {
    private final Runnable runnable;

    private ITaskFactory taskFactory;
    private ITask queuedTask;
    private long delayMillis;
    private boolean enabled = false;

    public RepeaterImplementation(ITaskFactory taskFactory, long delayMillis, Runnable runnable) {
        this.taskFactory = taskFactory;
        this.delayMillis = delayMillis;
        this.runnable = runnable;
    }

    @Override
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

    @Override
    public synchronized void executeNow() {
        if(queuedTask != null) {
            try {
                queuedTask.cancel(0);
            } catch (TaskException e) {
                e.printStackTrace();
            }
        }
        if (!enabled) {
            return;
        }

        runnable.run();

        if(enabled) {
            final ITask[] me = new ITask[1];
            me[0] = taskFactory.newTask(new Runnable() {
                @Override
                public void run() {
                    synchronized (RepeaterImplementation.this) {
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
