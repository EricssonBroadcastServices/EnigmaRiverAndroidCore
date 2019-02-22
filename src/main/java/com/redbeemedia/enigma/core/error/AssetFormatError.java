package com.redbeemedia.enigma.core.error;



public abstract class AssetFormatError extends AssetPlayRequestError {
    /*package-protected*/ AssetFormatError() {
        this(null, null);
    }

    /*package-protected*/ AssetFormatError(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ AssetFormatError(String message) {
        this(message, null);
    }

    /*package-protected*/ AssetFormatError(String message, Error cause) {
        super(message, cause);
    }

}
