package com.redbeemedia.enigma.core.error;



public class ServerTimeoutError extends ServerError {
    public ServerTimeoutError() {
        this(null, null);
    }

    public ServerTimeoutError(Error cause) {
        this(null, cause);
    }

    public ServerTimeoutError(String message) {
        this(message, null);
    }

    public ServerTimeoutError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.SERVER_TIMEOUT;
    }
}
