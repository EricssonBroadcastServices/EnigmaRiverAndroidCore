package com.redbeemedia.enigma.core.error;



public class AnonymousIpBlockedError extends EntitlementError {
    public AnonymousIpBlockedError() {
        this(null, null);
    }

    public AnonymousIpBlockedError(EnigmaError cause) {
        this(null, cause);
    }

    public AnonymousIpBlockedError(String message) {
        this(message, null);
    }

    public AnonymousIpBlockedError(String message, EnigmaError cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.ANONYMOUS_IP_BLOCKED, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.ANONYMOUS_IP_BLOCKED;
    }
}
