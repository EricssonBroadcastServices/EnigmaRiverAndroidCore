package com.redbeemedia.enigma.core.error;



public class ServerError extends Error {
    public ServerError() {
        this(null, null);
    }

    public ServerError(Error cause) {
        this(null, cause);
    }

    public ServerError(String message) {
        this(message, null);
    }

    public ServerError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.SERVER_ERROR;
    }
}
