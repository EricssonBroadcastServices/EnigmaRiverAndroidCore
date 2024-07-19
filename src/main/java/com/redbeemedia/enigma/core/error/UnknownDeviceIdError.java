// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class UnknownDeviceIdError extends LoginDeniedError {
    public UnknownDeviceIdError() {
        this(null, null);
    }

    public UnknownDeviceIdError(EnigmaError cause) {
        this(null, cause);
    }

    public UnknownDeviceIdError(String message) {
        this(message, null);
    }

    public UnknownDeviceIdError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.UNKNOWN_DEVICE_ID;
    }
}
