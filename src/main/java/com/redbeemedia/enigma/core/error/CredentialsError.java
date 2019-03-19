package com.redbeemedia.enigma.core.error;



public abstract class CredentialsError extends Error {
    /*package-protected*/ CredentialsError() {
        this(null, null);
    }

    /*package-protected*/ CredentialsError(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ CredentialsError(String message) {
        this(message, null);
    }

    /*package-protected*/ CredentialsError(String message, Error cause) {
        super(message, cause);
    }

}
