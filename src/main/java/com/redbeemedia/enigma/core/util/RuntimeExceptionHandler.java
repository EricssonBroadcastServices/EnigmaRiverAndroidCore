package com.redbeemedia.enigma.core.util;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class RuntimeExceptionHandler {
    private RuntimeException exception = null;

    private void collectException(RuntimeException e) {
        if(exception == null) {
            exception = e;
        } else {
            exception.addSuppressed(e);
        }
    }

    public void catchExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            collectException(e);
        }
    }

    public <T> void catchExceptions(Iterable<T> list, IAction<T> action) {
        for(T obj : list) {
            try {
                action.execute(obj);
            } catch(RuntimeException e) {
                collectException(e);
            }
        }
    }

    public void rethrowIfAnyExceptions() {
        if(exception != null) {
            throw exception;
        }
    }

    public interface IAction<T> {
        void execute(T obj);
    }
}
