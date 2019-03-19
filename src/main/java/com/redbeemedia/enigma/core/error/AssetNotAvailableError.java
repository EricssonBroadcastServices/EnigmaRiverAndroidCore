package com.redbeemedia.enigma.core.error;



/**
 * Subclasses of this error is used when an asset is valid, but for
 * some reason can't be played.
 */
public abstract class AssetNotAvailableError extends Error {
    /*package-protected*/ AssetNotAvailableError() {
        this(null, null);
    }

    /*package-protected*/ AssetNotAvailableError(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ AssetNotAvailableError(String message) {
        this(message, null);
    }

    /*package-protected*/ AssetNotAvailableError(String message, Error cause) {
        super(message, cause);
    }

}
