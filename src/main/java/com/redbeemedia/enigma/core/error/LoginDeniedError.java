package com.redbeemedia.enigma.core.error;



public abstract class LoginDeniedError extends Error {
    /*package-protected*/ LoginDeniedError() {
        this(null, null);
    }

    /*package-protected*/ LoginDeniedError(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ LoginDeniedError(String message) {
        this(message, null);
    }

    /*package-protected*/ LoginDeniedError(String message, Error cause) {
        super(message, cause);
    }

}
