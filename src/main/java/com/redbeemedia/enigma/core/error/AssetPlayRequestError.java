package com.redbeemedia.enigma.core.error;



public abstract class AssetPlayRequestError extends Error {
    /*package-protected*/ AssetPlayRequestError() {
        this(null, null);
    }

    /*package-protected*/ AssetPlayRequestError(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ AssetPlayRequestError(String message) {
        this(message, null);
    }

    /*package-protected*/ AssetPlayRequestError(String message, Error cause) {
        super(message, cause);
    }

}
