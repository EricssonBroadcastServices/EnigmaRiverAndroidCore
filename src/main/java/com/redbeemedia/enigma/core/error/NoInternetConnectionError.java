package com.redbeemedia.enigma.core.error;



public class NoInternetConnectionError extends PlayRequestRejectedError {
    public NoInternetConnectionError() {
        this(null);
    }

    public NoInternetConnectionError(EnigmaError cause) {
        super("No internet connection detected", cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INTERNAL_ERROR;
    }
}
