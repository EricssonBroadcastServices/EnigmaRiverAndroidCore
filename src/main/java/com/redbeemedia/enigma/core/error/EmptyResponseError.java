// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class EmptyResponseError extends HttpResponseError {
    public EmptyResponseError() {
        this(null, null);
    }

    public EmptyResponseError(EnigmaError cause) {
        this(null, cause);
    }

    public EmptyResponseError(String message) {
        this(message, null);
    }

    public EmptyResponseError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.EMPTY_RESPONSE;
    }
}
