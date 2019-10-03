package com.redbeemedia.enigma.core.error;

import java.lang.String;


public class PlayerImplementationError extends PlaybackError {
    private int internalErrorCode;
    private String internalErrorCodeFieldName;

    public PlayerImplementationError(int internalErrorCode, String internalErrorCodeFieldName) {
        this(internalErrorCode, internalErrorCodeFieldName, null, null);
    }

    public PlayerImplementationError(int internalErrorCode, String internalErrorCodeFieldName, EnigmaError cause) {
        this(internalErrorCode, internalErrorCodeFieldName, null, cause);
    }

    public PlayerImplementationError(int internalErrorCode, String internalErrorCodeFieldName, String message) {
        this(internalErrorCode, internalErrorCodeFieldName, message, null);
    }

    public PlayerImplementationError(int internalErrorCode, String internalErrorCodeFieldName, String message, EnigmaError cause) {
        super(message, cause);
        this.internalErrorCode = internalErrorCode;
        this.internalErrorCodeFieldName = internalErrorCodeFieldName;
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.PLAYER_IMPLEMENTATION_ERROR;
    }

    public int getInternalErrorCode() {
        return this.internalErrorCode;
    }

    public String getInternalErrorCodeFieldName() {
        return this.internalErrorCodeFieldName;
    }
}
