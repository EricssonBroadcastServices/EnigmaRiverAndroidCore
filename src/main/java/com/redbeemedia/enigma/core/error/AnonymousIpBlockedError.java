package com.redbeemedia.enigma.core.error;



public class AnonymousIpBlockedError extends AssetNotAvailableForDeviceError {
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
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.ANONYMOUS_IP_BLOCKED;
    }
}
