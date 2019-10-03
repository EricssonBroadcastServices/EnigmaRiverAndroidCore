package com.redbeemedia.enigma.core.error;



public abstract class AssetFormatError extends AssetPlayRequestError {
    /*package-protected*/ AssetFormatError() {
        this(null, null);
    }

    /*package-protected*/ AssetFormatError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ AssetFormatError(String message) {
        this(message, null);
    }

    /*package-protected*/ AssetFormatError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
