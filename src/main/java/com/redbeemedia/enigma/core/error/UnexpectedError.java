package com.redbeemedia.enigma.core.error;

import java.io.IOException;
import java.lang.Exception;
import java.io.Writer;


public class UnexpectedError extends Error {
    private Exception exception = null;

    public UnexpectedError() {
        this(null, null, null);
    }

    public UnexpectedError(Error cause) {
        this(null, null, cause);
    }

    public UnexpectedError(String message) {
        this(null, message, null);
    }

    public UnexpectedError(String message, Error cause) {
        this(null, message, cause);
    }

    public UnexpectedError(Exception exception) {
        this(exception, null, null);
    }

    public UnexpectedError(Exception exception, Error cause) {
        this(exception, null, cause);
    }

    public UnexpectedError(Exception exception, String message) {
        this(exception, message, null);
    }

    public UnexpectedError(Exception exception, String message, Error cause) {
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
