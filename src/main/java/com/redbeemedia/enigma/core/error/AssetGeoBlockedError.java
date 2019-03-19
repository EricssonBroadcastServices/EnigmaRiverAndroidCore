package com.redbeemedia.enigma.core.error;



public class AssetGeoBlockedError extends AssetRestrictedError {
    public AssetGeoBlockedError() {
        this(null, null);
    }

    public AssetGeoBlockedError(Error cause) {
        this(null, cause);
    }

    public AssetGeoBlockedError(String message) {
        this(message, null);
    }

    public AssetGeoBlockedError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.GEO_BLOCKED;
    }
}
