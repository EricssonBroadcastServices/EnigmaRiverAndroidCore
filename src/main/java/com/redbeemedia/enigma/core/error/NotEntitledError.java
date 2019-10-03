package com.redbeemedia.enigma.core.error;

import com.redbeemedia.enigma.core.entitlement.EntitlementStatus;


public class NotEntitledError extends EntitlementError {
    public NotEntitledError(EntitlementStatus entitlementStatus) {
        this(entitlementStatus, null, null);
    }

    public NotEntitledError(EntitlementStatus entitlementStatus, Error cause) {
        this(entitlementStatus, null, cause);
    }

    public NotEntitledError(EntitlementStatus entitlementStatus, String message) {
        this(entitlementStatus, message, null);
    }

    public NotEntitledError(EntitlementStatus entitlementStatus, String message, Error cause) {
        super(entitlementStatus, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.NOT_ENTITLED;
    }
}
