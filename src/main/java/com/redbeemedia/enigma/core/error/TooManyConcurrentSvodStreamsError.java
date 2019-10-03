package com.redbeemedia.enigma.core.error;



public class TooManyConcurrentSvodStreamsError extends TooManyConcurrentStreamsError {
    public TooManyConcurrentSvodStreamsError() {
        this(null, null);
    }

    public TooManyConcurrentSvodStreamsError(EnigmaError cause) {
        this(null, cause);
    }

    public TooManyConcurrentSvodStreamsError(String message) {
        this(message, null);
    }

    public TooManyConcurrentSvodStreamsError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.TOO_MANY_CONCURRENT_SVODS;
    }
}
