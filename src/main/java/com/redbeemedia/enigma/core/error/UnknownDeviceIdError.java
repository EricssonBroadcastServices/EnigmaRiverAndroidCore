package com.redbeemedia.enigma.core.error;



public class UnknownDeviceIdError extends LoginDeniedError {
    public UnknownDeviceIdError() {
        this(null, null);
    }

    public UnknownDeviceIdError(Error cause) {
        this(null, cause);
    }

    public UnknownDeviceIdError(String message) {
        this(message, null);
    }

    public UnknownDeviceIdError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.UNKNOWN_DEVICE_ID;
    }
}
