package com.redbeemedia.enigma.core.context.exception;

public class ContextInitializationException extends RuntimeException {
    public ContextInitializationException() {
    }

    public ContextInitializationException(String message) {
        super(message);
    }

    public ContextInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContextInitializationException(Throwable cause) {
        super(cause);
    }
}
