package com.redbeemedia.enigma.core.error;



public class NotEntitledToAssetError extends AssetNotAvailableError {
    public NotEntitledToAssetError() {
        this(null, null);
    }

    public NotEntitledToAssetError(Error cause) {
        this(null, cause);
    }

    public NotEntitledToAssetError(String message) {
        this(message, null);
    }

    public NotEntitledToAssetError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.NOT_ENTITLED;
    }
}
