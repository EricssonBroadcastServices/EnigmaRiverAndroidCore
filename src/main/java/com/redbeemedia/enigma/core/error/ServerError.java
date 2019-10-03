package com.redbeemedia.enigma.core.error;



public class ServerError extends EnigmaError {
    public ServerError() {
        this(null, null);
    }

    public ServerError(EnigmaError cause) {
        this(null, cause);
    }

    public ServerError(String message) {
        this(message, null);
    }

    public ServerError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.SERVER_ERROR;
    }
}
