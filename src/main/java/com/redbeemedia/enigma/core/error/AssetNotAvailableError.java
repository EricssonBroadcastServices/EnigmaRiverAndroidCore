// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



/**
 * Subclasses of this error is used when an asset is valid, but for
 * some reason can't be played.
 */
public abstract class AssetNotAvailableError extends EnigmaError {
    /*package-protected*/ AssetNotAvailableError() {
        this(null, null);
    }

    /*package-protected*/ AssetNotAvailableError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ AssetNotAvailableError(String message) {
        this(message, null);
    }

    /*package-protected*/ AssetNotAvailableError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
