package com.redbeemedia.enigma.core.error;



/**
 * If a specific stream format has been requested but is not
 * supported by the player implementationor not available for the
 * asset.
 */
public class UnsupportedMediaFormatError extends AssetFormatError {
    public UnsupportedMediaFormatError() {
        this(null, null);
    }

    public UnsupportedMediaFormatError(EnigmaError cause) {
        this(null, cause);
    }

    public UnsupportedMediaFormatError(String message) {
        this(message, null);
    }

    public UnsupportedMediaFormatError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.UNSUPPORTED_STREAM_FORMAT;
    }

}
