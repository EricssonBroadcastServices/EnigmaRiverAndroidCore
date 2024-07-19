// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class InvalidJsonToServerError extends InternalError {
    public InvalidJsonToServerError() {
        this(null, null);
    }

    public InvalidJsonToServerError(EnigmaError cause) {
        this(null, cause);
    }

    public InvalidJsonToServerError(String message) {
        this(message, null);
    }

    public InvalidJsonToServerError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INVALID_JSON;
    }
}
