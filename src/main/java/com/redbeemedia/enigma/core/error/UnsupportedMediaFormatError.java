// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



/**
 * If a specific stream format has been requested but is not
 * supported by the player implementation or not available for the
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
