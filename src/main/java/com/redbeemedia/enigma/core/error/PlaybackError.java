package com.redbeemedia.enigma.core.error;



public abstract class PlaybackError extends EnigmaError {
    /*package-protected*/ PlaybackError() {
        this(null, null);
    }

    /*package-protected*/ PlaybackError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ PlaybackError(String message) {
        this(message, null);
    }

    /*package-protected*/ PlaybackError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
