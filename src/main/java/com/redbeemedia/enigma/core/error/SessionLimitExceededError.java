package com.redbeemedia.enigma.core.error;



public class SessionLimitExceededError extends LoginDeniedError {
    public SessionLimitExceededError() {
        this(null, null);
    }

    public SessionLimitExceededError(Error cause) {
        this(null, cause);
    }

    public SessionLimitExceededError(String message) {
        this(message, null);
    }

    public SessionLimitExceededError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.SESSION_LIMIT_EXCEEDED;
    }
}
