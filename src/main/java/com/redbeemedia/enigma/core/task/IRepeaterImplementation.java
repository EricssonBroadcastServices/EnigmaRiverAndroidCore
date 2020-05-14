package com.redbeemedia.enigma.core.task;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IRepeaterImplementation {
    void setEnabled(boolean enabled);
    void executeNow();

    interface Factory {
        IRepeaterImplementation create(ITaskFactory taskFactory, long delayMillis, Runnable runnable);
    }
}
