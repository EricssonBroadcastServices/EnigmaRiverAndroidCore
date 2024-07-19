// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;

import java.io.IOException;
import java.lang.Exception;
import java.io.Writer;


public class UnexpectedError extends EnigmaError {
    private Exception exception = null;

    public UnexpectedError() {
        this(null, null, null);
    }

    public UnexpectedError(EnigmaError cause) {
        this(null, null, cause);
    }

    public UnexpectedError(String message) {
        this(null, message, null);
    }

    public UnexpectedError(String message, EnigmaError cause) {
        this(null, message, cause);
    }

    public UnexpectedError(Exception exception) {
        this(exception, null, null);
    }

    public UnexpectedError(Exception exception, EnigmaError cause) {
        this(exception, null, cause);
    }

    public UnexpectedError(Exception exception, String message) {
        this(exception, message, null);
    }

    public UnexpectedError(Exception exception, String message, EnigmaError cause) {
        super(message, cause);
        this.exception = exception;
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.UNEXPECTED;
    }

    public Exception getException() {
        return this.exception;
    }

    @Override
    public void writeTrace(Writer writer) throws IOException {
        super.writeTrace(writer);
        addExceptionStackTrace(writer, exception);
    }
}
