package com.redbeemedia.enigma.core.error;



public class GeoBlockedError extends EntitlementError {
    public GeoBlockedError() {
        this(null, null);
    }

    public GeoBlockedError(Error cause) {
        this(null, cause);
    }

    public GeoBlockedError(String message) {
        this(message, null);
    }

    public GeoBlockedError(String message, Error cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.GEO_BLOCKED, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.GEO_BLOCKED;
    }
}
