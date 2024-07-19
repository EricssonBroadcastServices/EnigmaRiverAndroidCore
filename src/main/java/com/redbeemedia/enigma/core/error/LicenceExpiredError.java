// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class LicenceExpiredError extends AssetRestrictedError {
    public LicenceExpiredError() {
        this(null, null);
    }

    public LicenceExpiredError(EnigmaError cause) {
        this(null, cause);
    }

    public LicenceExpiredError(String message) {
        this(message, null);
    }

    public LicenceExpiredError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.LICENCE_EXPIRED;
    }
}
