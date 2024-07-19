// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



/**
 * Credentials were valid, but login was denied for other reason.
 */
public abstract class LoginDeniedError extends EnigmaError {
    /*package-protected*/ LoginDeniedError() {
        this(null, null);
    }

    /*package-protected*/ LoginDeniedError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ LoginDeniedError(String message) {
        this(message, null);
    }

    /*package-protected*/ LoginDeniedError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
