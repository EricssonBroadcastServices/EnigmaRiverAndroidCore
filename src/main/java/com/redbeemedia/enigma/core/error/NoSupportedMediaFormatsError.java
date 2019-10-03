package com.redbeemedia.enigma.core.error;



public class NoSupportedMediaFormatsError extends AssetFormatError {
    public NoSupportedMediaFormatsError() {
        this(null, null);
    }

    public NoSupportedMediaFormatsError(EnigmaError cause) {
        this(null, cause);
    }

    public NoSupportedMediaFormatsError(String message) {
        this(message, null);
    }

    public NoSupportedMediaFormatsError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.NO_SUPPORTED_MEDIAFORMAT_FOUND;
    }
}
