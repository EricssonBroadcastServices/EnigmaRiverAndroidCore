package com.redbeemedia.enigma.core.error;



public class EmptyResponseError extends HttpResponseError {
    public EmptyResponseError() {
        this(null, null);
    }

    public EmptyResponseError(Error cause) {
        this(null, cause);
    }

    public EmptyResponseError(String message) {
        this(message, null);
    }

    public EmptyResponseError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.EMPTY_RESPONSE;
    }
}
