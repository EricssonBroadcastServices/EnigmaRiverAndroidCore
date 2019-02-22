package com.redbeemedia.enigma.core.error;



/**
 * The business unit name did not correspond to a business unit for
 * the given customer unit.
 */
public class UnknownBusinessUnitError extends Error {
    public UnknownBusinessUnitError() {
        this(null, null);
    }

    public UnknownBusinessUnitError(Error cause) {
        this(null, cause);
    }

    public UnknownBusinessUnitError(String message) {
        this(message, null);
    }

    public UnknownBusinessUnitError(String message, Error cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.UNKNOWN_BUSINESS_UNIT;
    }
}
