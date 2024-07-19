// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class InvalidCredentialsError extends CredentialsError {
    public InvalidCredentialsError() {
        this(null, null);
    }

    public InvalidCredentialsError(EnigmaError cause) {
        this(null, cause);
    }

    public InvalidCredentialsError(String message) {
        this(message, null);
    }

    public InvalidCredentialsError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INVALID_CREDENTIALS;
    }
}
