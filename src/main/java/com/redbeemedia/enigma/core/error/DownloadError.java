package com.redbeemedia.enigma.core.error;



public abstract class DownloadError extends EnigmaError {
    /*package-protected*/ DownloadError() {
        this(null, null);
    }

    /*package-protected*/ DownloadError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ DownloadError(String message) {
        this(message, null);
    }

    /*package-protected*/ DownloadError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
