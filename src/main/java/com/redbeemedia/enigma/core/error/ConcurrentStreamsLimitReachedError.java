package com.redbeemedia.enigma.core.error;



public class ConcurrentStreamsLimitReachedError extends EntitlementError {
    public ConcurrentStreamsLimitReachedError() {
        this(null, null);
    }

    public ConcurrentStreamsLimitReachedError(Error cause) {
        this(null, cause);
    }

    public ConcurrentStreamsLimitReachedError(String message) {
        this(message, null);
    }

    public ConcurrentStreamsLimitReachedError(String message, Error cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.CONCURRENT_STREAMS_LIMIT_REACHED, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.TOO_MANY_CONCURRENT_STREAMS;
    }
}
