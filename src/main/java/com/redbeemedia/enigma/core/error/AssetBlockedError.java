// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class AssetBlockedError extends EntitlementError {
    public AssetBlockedError() {
        this(null, null);
    }

    public AssetBlockedError(EnigmaError cause) {
        this(null, cause);
    }

    public AssetBlockedError(String message) {
        this(message, null);
    }

    public AssetBlockedError(String message, EnigmaError cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.BLOCKED, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.ASSET_BLOCKED;
    }
}
