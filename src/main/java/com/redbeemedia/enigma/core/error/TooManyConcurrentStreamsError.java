// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class TooManyConcurrentStreamsError extends AssetRestrictedError {
    public TooManyConcurrentStreamsError() {
        this(null, null);
    }

    public TooManyConcurrentStreamsError(EnigmaError cause) {
        this(null, cause);
    }

    public TooManyConcurrentStreamsError(String message) {
        this(message, null);
    }

    public TooManyConcurrentStreamsError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.TOO_MANY_CONCURRENT_STREAMS;
    }
}
