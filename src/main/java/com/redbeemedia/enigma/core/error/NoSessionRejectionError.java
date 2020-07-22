package com.redbeemedia.enigma.core.error;



public class NoSessionRejectionError extends PlayRequestRejectedError {
    public NoSessionRejectionError() {
        this(null);
    }

    public NoSessionRejectionError(EnigmaError cause) {
        super("No session found", cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INTERNAL_ERROR;
    }
}
