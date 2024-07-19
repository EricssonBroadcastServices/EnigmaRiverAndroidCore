// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class AssetNotAvailableForDeviceError extends AssetNotAvailableError {
    public AssetNotAvailableForDeviceError() {
        this(null, null);
    }

    public AssetNotAvailableForDeviceError(EnigmaError cause) {
        this(null, cause);
    }

    public AssetNotAvailableForDeviceError(String message) {
        this(message, null);
    }

    public AssetNotAvailableForDeviceError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.DEVICE_DENIED;
    }
}
