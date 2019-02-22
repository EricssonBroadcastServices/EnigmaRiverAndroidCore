package com.redbeemedia.enigma.core.error;



public class InvalidSessionTokenError extends CredentialsError {
    public InvalidSessionTokenError() {
        this(null, null);
    }

    public InvalidSessionTokenError(Error cause) {
        this(null, cause);
    }

    public InvalidSessionTokenError(String message) {
        this(message, null);
    }

    public InvalidSessionTokenError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INVALID_SESSION_TOKEN;
    }
}
