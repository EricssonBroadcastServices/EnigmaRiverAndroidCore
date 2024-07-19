// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class InvalidSessionTokenError extends CredentialsError {
    public InvalidSessionTokenError() {
        this(null, null);
    }

    public InvalidSessionTokenError(EnigmaError cause) {
        this(null, cause);
    }

    public InvalidSessionTokenError(String message) {
        this(message, null);
    }

    public InvalidSessionTokenError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INVALID_SESSION_TOKEN;
    }
}
