package com.redbeemedia.enigma.core.error;



public class TooManyConcurrentStreamsError extends AssetRestrictedError {
    public TooManyConcurrentStreamsError() {
        this(null, null);
    }

    public TooManyConcurrentStreamsError(Error cause) {
        this(null, cause);
    }

    public TooManyConcurrentStreamsError(String message) {
        this(message, null);
    }

    public TooManyConcurrentStreamsError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.TOO_MANY_CONCURRENT_STREAMS;
    }
}
