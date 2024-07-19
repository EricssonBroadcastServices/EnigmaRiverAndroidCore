// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



/**
 * This error is use when the asset could not be played due to a
 * setting in the backend or customer portal.
 */
public class AssetRestrictedError extends AssetNotAvailableError {
    public AssetRestrictedError() {
        this(null, null);
    }

    public AssetRestrictedError(EnigmaError cause) {
        this(null, cause);
    }

    public AssetRestrictedError(String message) {
        this(message, null);
    }

    public AssetRestrictedError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.ASSET_RESTRICTED;
    }
}
