package com.redbeemedia.enigma.core.error;



public class DeviceLimitReachedError extends LoginDeniedError {
    public DeviceLimitReachedError() {
        this(null, null);
    }

    public DeviceLimitReachedError(Error cause) {
        this(null, cause);
    }

    public DeviceLimitReachedError(String message) {
        this(message, null);
    }

    public DeviceLimitReachedError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.DEVICE_LIMIT_REACHED;
    }
}
