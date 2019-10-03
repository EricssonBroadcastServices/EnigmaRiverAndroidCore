package com.redbeemedia.enigma.core.error;

import com.redbeemedia.enigma.core.entitlement.EntitlementStatus;


public abstract class EntitlementError extends Error {
    private EntitlementStatus entitlementStatus;

    /*package-protected*/ EntitlementError(EntitlementStatus entitlementStatus) {
        this(entitlementStatus, null, null);
    }

    /*package-protected*/ EntitlementError(EntitlementStatus entitlementStatus, Error cause) {
        this(entitlementStatus, null, cause);
    }

    /*package-protected*/ EntitlementError(EntitlementStatus entitlementStatus, String message) {
        this(entitlementStatus, message, null);
    }

    /*package-protected*/ EntitlementError(EntitlementStatus entitlementStatus, String message, Error cause) {
        super(message, cause);
        this.entitlementStatus = entitlementStatus;
    }


    public EntitlementStatus getEntitlementStatus() {
        return this.entitlementStatus;
    }
}
