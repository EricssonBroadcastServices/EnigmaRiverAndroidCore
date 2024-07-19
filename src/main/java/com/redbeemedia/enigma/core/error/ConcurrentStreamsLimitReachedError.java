// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class ConcurrentStreamsLimitReachedError extends EntitlementError {
    public ConcurrentStreamsLimitReachedError() {
        this(null, null);
    }

    public ConcurrentStreamsLimitReachedError(EnigmaError cause) {
        this(null, cause);
    }

    public ConcurrentStreamsLimitReachedError(String message) {
        this(message, null);
    }

    public ConcurrentStreamsLimitReachedError(String message, EnigmaError cause) {
        super(com.redbeemedia.enigma.core.entitlement.EntitlementStatus.CONCURRENT_STREAMS_LIMIT_REACHED, message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.TOO_MANY_CONCURRENT_STREAMS;
    }
}
