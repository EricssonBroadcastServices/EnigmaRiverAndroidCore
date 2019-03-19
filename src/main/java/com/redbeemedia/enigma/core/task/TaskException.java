package com.redbeemedia.enigma.core.task;

/**
 * Signals that a task exception of some sort has occurred.
 */
public class TaskException extends Exception {
    public TaskException() {
    }

    public TaskException(String message) {
        super(message);
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskException(Throwable cause) {
        super(cause);
    }
}
