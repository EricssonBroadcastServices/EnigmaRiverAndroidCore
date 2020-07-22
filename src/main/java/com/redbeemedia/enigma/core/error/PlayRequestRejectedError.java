package com.redbeemedia.enigma.core.error;



/**
 * Used when a PlayRequest is rejected due to various reasons
 */
public abstract class PlayRequestRejectedError extends PlayRequestError {
    /*package-protected*/ PlayRequestRejectedError() {
        this(null, null);
    }

    /*package-protected*/ PlayRequestRejectedError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ PlayRequestRejectedError(String message) {
        this(message, null);
    }

    /*package-protected*/ PlayRequestRejectedError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
