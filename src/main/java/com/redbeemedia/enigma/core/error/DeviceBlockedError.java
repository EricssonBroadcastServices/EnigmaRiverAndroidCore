package com.redbeemedia.enigma.core.error;



public class DeviceBlockedError extends EntitlementError {
    public DeviceBlockedError() {
        this(null, null);
    }

    public DeviceBlockedError(EnigmaError cause) {
        this(null, cause);
    }

    public DeviceBlockedError(String message) {
        this(message, null);
    }

    public DeviceBlockedError(String message, EnigmaError cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.DEVICE_BLOCKED, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.DEVICE_DENIED;
    }
}
