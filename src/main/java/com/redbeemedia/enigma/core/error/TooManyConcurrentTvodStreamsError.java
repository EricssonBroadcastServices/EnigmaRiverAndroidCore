package com.redbeemedia.enigma.core.error;



public class TooManyConcurrentTvodStreamsError extends TooManyConcurrentStreamsError {
    public TooManyConcurrentTvodStreamsError() {
        this(null, null);
    }

    public TooManyConcurrentTvodStreamsError(EnigmaError cause) {
        this(null, cause);
    }

    public TooManyConcurrentTvodStreamsError(String message) {
        this(message, null);
    }

    public TooManyConcurrentTvodStreamsError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.TOO_MANY_CONCURRENT_TVODS;
    }
}
