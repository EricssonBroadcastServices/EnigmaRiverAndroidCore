package com.redbeemedia.enigma.core.error;



public class AssetNotEnabledError extends AssetNotAvailableError {
    public AssetNotEnabledError() {
        this(null, null);
    }

    public AssetNotEnabledError(Error cause) {
        this(null, cause);
    }

    public AssetNotEnabledError(String message) {
        this(message, null);
    }

    public AssetNotEnabledError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.NOT_ENABLED;
    }
}
