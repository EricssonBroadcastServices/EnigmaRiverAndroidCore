package com.redbeemedia.enigma.core.error;

import java.lang.Exception;


public class IllegalSeekPositionError extends IllegalControlError {
    private Exception exception = null;

    public IllegalSeekPositionError() {
        this(null, null, null);
    }

    public IllegalSeekPositionError(Error cause) {
        this(null, null, cause);
    }

    public IllegalSeekPositionError(String message) {
        this(null, message, null);
    }

    public IllegalSeekPositionError(String message, Error cause) {
        this(null, message, cause);
    }

    public IllegalSeekPositionError(Exception exception) {
        this(exception, null, null);
    }

    public IllegalSeekPositionError(Exception exception, Error cause) {
        this(exception, null, cause);
    }

    public IllegalSeekPositionError(Exception exception, String message) {
        this(exception, message, null);
    }

    public IllegalSeekPositionError(Exception exception, String message, Error cause) {
        super(message, cause);
        this.exception = exception;
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.ILLEGAL_SEEK_POSITION;
    }

    public Exception getException() {
        return this.exception;
    }
}
