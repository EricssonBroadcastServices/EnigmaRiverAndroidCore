package com.redbeemedia.enigma.core.error;



public abstract class HttpResponseError extends ServerError {
    /*package-protected*/ HttpResponseError() {
        this(null, null);
    }

    /*package-protected*/ HttpResponseError(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ HttpResponseError(String message) {
        this(message, null);
    }

    /*package-protected*/ HttpResponseError(String message, Error cause) {
        super(message, cause);
    }

}
