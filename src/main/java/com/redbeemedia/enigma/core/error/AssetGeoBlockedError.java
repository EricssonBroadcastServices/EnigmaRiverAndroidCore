package com.redbeemedia.enigma.core.error;



public class AssetGeoBlockedError extends AssetRestrictedError {
    public AssetGeoBlockedError() {
        this(null, null);
    }

    public AssetGeoBlockedError(EnigmaError cause) {
        this(null, cause);
    }

    public AssetGeoBlockedError(String message) {
        this(message, null);
    }

    public AssetGeoBlockedError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.GEO_BLOCKED;
    }
}
