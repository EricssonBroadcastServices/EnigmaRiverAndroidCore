package com.redbeemedia.enigma.core.error;



public class NotAvailableError extends EntitlementError {
    public NotAvailableError() {
        this(null, null);
    }

    public NotAvailableError(EnigmaError cause) {
        this(null, cause);
    }

    public NotAvailableError(String message) {
        this(message, null);
    }

    public NotAvailableError(String message, EnigmaError cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.NOT_AVAILABLE, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.ASSET_NOT_AVAILABLE;
    }
}
