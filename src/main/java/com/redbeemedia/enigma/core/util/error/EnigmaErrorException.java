package com.redbeemedia.enigma.core.util.error;

import com.redbeemedia.enigma.core.error.Error;

public class EnigmaErrorException extends RuntimeException{
    private final Error error;

    public EnigmaErrorException(Error error) {
        super(error.getClass().getSimpleName()+" ("+error.getErrorCode()+")");
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
