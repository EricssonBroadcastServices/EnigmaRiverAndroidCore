package com.redbeemedia.enigma.core.error;



public class InternalError extends Error {
    public InternalError() {
        this(null, null);
    }

    public InternalError(Error cause) {
        this(null, cause);
    }

    public InternalError(String message) {
        this(message, null);
    }

    public InternalError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INTERNAL_ERROR;
    }
}
