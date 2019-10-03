package com.redbeemedia.enigma.core.error;



public class InternalError extends EnigmaError {
    public InternalError() {
        this(null, null);
    }

    public InternalError(EnigmaError cause) {
        this(null, cause);
    }

    public InternalError(String message) {
        this(message, null);
    }

    public InternalError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INTERNAL_ERROR;
    }
}
