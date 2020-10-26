package com.redbeemedia.enigma.core.error;

import java.lang.Exception;


/**
 * Could not play the DRM protected media
 * because the DRM keys have expired.
 */
public class DrmKeysExpiredError extends PlaybackError {
    private Exception exception = null;

    public DrmKeysExpiredError() {
        this(null, null, null);
    }

    public DrmKeysExpiredError(EnigmaError cause) {
        this(null, null, cause);
    }

    public DrmKeysExpiredError(String message) {
        this(null, message, null);
    }

    public DrmKeysExpiredError(String message, EnigmaError cause) {
        this(null, message, cause);
    }

    public DrmKeysExpiredError(Exception exception) {
        this(exception, null, null);
    }

    public DrmKeysExpiredError(Exception exception, EnigmaError cause) {
        this(exception, null, cause);
    }

    public DrmKeysExpiredError(Exception exception, String message) {
        this(exception, message, null);
    }

    public DrmKeysExpiredError(Exception exception, String message, EnigmaError cause) {
        super(message, cause);
        this.exception = exception;
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.DRM_KEYS_EXPIRED;
    }

    public Exception getException() {
        return this.exception;
    }
}
