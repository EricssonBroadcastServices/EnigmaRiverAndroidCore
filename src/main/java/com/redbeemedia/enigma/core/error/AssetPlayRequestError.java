package com.redbeemedia.enigma.core.error;



public abstract class AssetPlayRequestError extends PlayRequestError {
    /*package-protected*/ AssetPlayRequestError() {
        this(null, null);
    }

    /*package-protected*/ AssetPlayRequestError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ AssetPlayRequestError(String message) {
        this(message, null);
    }

    /*package-protected*/ AssetPlayRequestError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
