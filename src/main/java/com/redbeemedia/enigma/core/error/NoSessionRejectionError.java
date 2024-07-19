// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class NoSessionRejectionError extends PlayRequestRejectedError {
    public NoSessionRejectionError() {
        this(null);
    }

    public NoSessionRejectionError(EnigmaError cause) {
        super("No session found", cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INTERNAL_ERROR;
    }
}
