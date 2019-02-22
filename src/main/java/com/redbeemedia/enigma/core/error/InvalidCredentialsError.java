package com.redbeemedia.enigma.core.error;



public class InvalidCredentialsError extends CredentialsError {
    public InvalidCredentialsError() {
        this(null, null);
    }

    public InvalidCredentialsError(Error cause) {
        this(null, cause);
    }

    public InvalidCredentialsError(String message) {
        this(message, null);
    }

    public InvalidCredentialsError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INVALID_CREDENTIALS;
    }
}
