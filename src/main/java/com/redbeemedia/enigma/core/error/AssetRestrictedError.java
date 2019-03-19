package com.redbeemedia.enigma.core.error;



/**
 * This error is use when the asset could not be played due to a
 * setting in the backend or customer portal.
 */
public class AssetRestrictedError extends AssetNotAvailableError {
    public AssetRestrictedError() {
        this(null, null);
    }

    public AssetRestrictedError(Error cause) {
        this(null, cause);
    }

    public AssetRestrictedError(String message) {
        this(message, null);
    }

    public AssetRestrictedError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.ASSET_RESTRICTED;
    }
}
