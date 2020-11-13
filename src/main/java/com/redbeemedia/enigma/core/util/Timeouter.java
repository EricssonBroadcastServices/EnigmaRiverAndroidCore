package com.redbeemedia.enigma.core.util;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.task.ITask;
import com.redbeemedia.enigma.core.task.ITaskFactory;
import com.redbeemedia.enigma.core.task.TaskException;
import com.redbeemedia.enigma.core.time.Duration;

/**
 * Utility class that executes {@code onTimeout} after a set duration of time after {@link #start()}
 * is called, unless {@link #cancel()} is called first.
 */
public final class Timeouter {
    private final ITask task;
    private final Duration duration;
    private volatile boolean resolved = false;
    private volatile Runnable onResolve = null;
    private volatile Runnable onTimeout = null;

    public Timeouter(Duration duration) {
        this(EnigmaRiverContext.getTaskFactoryProvider().getTaskFactory(), duration);
    }

    public Timeouter(ITaskFactory taskFactory, Duration duration) {
        this.duration = duration;
        this.task = taskFactory.newTask(() -> {
            if(!resolved) {
                resolved = true;
                runIfNotNull(onResolve);
                runIfNotNull(onTimeout);
            }
        });
    }

    /**
     * Runnable to execute when Timeouter times out or is cancelled.
     * @param onResolve
     */
    public void setOnResolve(Runnable onResolve) {
        this.onResolve = onResolve;
    }

    /**
     * Runnable to execute when and if Timeouter times out before cancelled.
     * @param onTimeout
     */
    public void setOnTimeout(Runnable onTimeout) {
        this.onTimeout = onTimeout;
    }

    /**
     * Starts the Timeouter which will time out after {@code duration}.
     */
    public void start() {
        try {
            task.startDelayed(duration.inWholeUnits(Duration.Unit.MILLISECONDS));
        } catch (TaskException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cancels the Timeouter if not already timed out or cancelled.
     */
    public void cancel() {
        if(!resolved) {
            resolved = true;
            try {
                task.cancel(1);
            } catch (TaskException e) {
                e.printStackTrace(); //Log and ignore
            }
            runIfNotNull(onResolve);
        }
    }

    private static void runIfNotNull(Runnable runnable) {
        if(runnable != null) {
            runnable.run();
        }
    }
}
