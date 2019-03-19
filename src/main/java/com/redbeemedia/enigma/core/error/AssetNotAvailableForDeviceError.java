package com.redbeemedia.enigma.core.error;



public class AssetNotAvailableForDeviceError extends AssetNotAvailableError {
    public AssetNotAvailableForDeviceError() {
        this(null, null);
    }

    public AssetNotAvailableForDeviceError(Error cause) {
        this(null, cause);
    }

    public AssetNotAvailableForDeviceError(String message) {
        this(message, null);
    }

    public AssetNotAvailableForDeviceError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.DEVICE_DENIED;
    }
}
