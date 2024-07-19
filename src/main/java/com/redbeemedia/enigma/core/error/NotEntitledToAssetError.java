// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class NotEntitledToAssetError extends AssetNotAvailableError {
    public NotEntitledToAssetError() {
        this(null, null);
    }

    public NotEntitledToAssetError(EnigmaError cause) {
        this(null, cause);
    }

    public NotEntitledToAssetError(String message) {
        this(message, null);
    }

    public NotEntitledToAssetError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.NOT_ENTITLED;
    }
}
