package com.redbeemedia.enigma.core.error;



public class NotEnabledError extends EntitlementError {
    public NotEnabledError() {
        this(null, null);
    }

    public NotEnabledError(Error cause) {
        this(null, cause);
    }

    public NotEnabledError(String message) {
        this(message, null);
    }

    public NotEnabledError(String message, Error cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.NOT_ENABLED, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.NOT_ENABLED;
    }
}
