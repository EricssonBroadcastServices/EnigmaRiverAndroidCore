// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public abstract class CredentialsError extends EnigmaError {
    /*package-protected*/ CredentialsError() {
        this(null, null);
    }

    /*package-protected*/ CredentialsError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ CredentialsError(String message) {
        this(message, null);
    }

    /*package-protected*/ CredentialsError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
