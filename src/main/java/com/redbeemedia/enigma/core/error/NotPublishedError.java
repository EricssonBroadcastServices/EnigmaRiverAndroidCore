// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class NotPublishedError extends EntitlementError {
    public NotPublishedError() {
        this(null, null);
    }

    public NotPublishedError(EnigmaError cause) {
        this(null, cause);
    }

    public NotPublishedError(String message) {
        this(message, null);
    }

    public NotPublishedError(String message, EnigmaError cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.NOT_PUBLISHED, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.ASSET_NOT_PUBLISHED;
    }
}
