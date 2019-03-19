package com.redbeemedia.enigma.core.error;



public class InvalidJsonToServerError extends InternalError {
    public InvalidJsonToServerError() {
        this(null, null);
    }

    public InvalidJsonToServerError(Error cause) {
        this(null, cause);
    }

    public InvalidJsonToServerError(String message) {
        this(message, null);
    }

    public InvalidJsonToServerError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INVALID_JSON;
    }
}
