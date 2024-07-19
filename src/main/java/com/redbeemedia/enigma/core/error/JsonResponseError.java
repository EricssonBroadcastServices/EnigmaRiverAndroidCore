// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



/**
 * The response from the server was not on the expected json format.
 */
public class JsonResponseError extends HttpResponseError {
    public JsonResponseError() {
        this(null, null);
    }

    public JsonResponseError(EnigmaError cause) {
        this(null, cause);
    }

    public JsonResponseError(String message) {
        this(message, null);
    }

    public JsonResponseError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INVALID_JSON_RESPONSE;
    }
}
