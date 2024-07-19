// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public abstract class IllegalControlError extends PlaybackError {
    /*package-protected*/ IllegalControlError() {
        this(null, null);
    }

    /*package-protected*/ IllegalControlError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ IllegalControlError(String message) {
        this(message, null);
    }

    /*package-protected*/ IllegalControlError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
