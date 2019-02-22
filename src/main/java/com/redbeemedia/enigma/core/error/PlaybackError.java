package com.redbeemedia.enigma.core.error;



public abstract class PlaybackError extends Error {
    /*package-protected*/ PlaybackError() {
        this(null, null);
    }

    /*package-protected*/ PlaybackError(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ PlaybackError(String message) {
        this(message, null);
    }

    /*package-protected*/ PlaybackError(String message, Error cause) {
        super(message, cause);
    }

}
