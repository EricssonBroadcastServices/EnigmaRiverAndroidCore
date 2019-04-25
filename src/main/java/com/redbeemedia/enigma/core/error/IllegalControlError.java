package com.redbeemedia.enigma.core.error;



public abstract class IllegalControlError extends PlaybackError {
    /*package-protected*/ IllegalControlError() {
        this(null, null);
    }

    /*package-protected*/ IllegalControlError(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ IllegalControlError(String message) {
        this(message, null);
    }

    /*package-protected*/ IllegalControlError(String message, Error cause) {
        super(message, cause);
    }

}
