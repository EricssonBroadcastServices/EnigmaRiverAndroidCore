package com.redbeemedia.enigma.core.error;

import com.redbeemedia.enigma.core.http.HttpStatus;


public class UnexpectedHttpStatusError extends HttpResponseError {
    private HttpStatus httpStatus;

    public UnexpectedHttpStatusError(HttpStatus httpStatus) {
        this(httpStatus, null, null);
    }

    public UnexpectedHttpStatusError(HttpStatus httpStatus, Error cause) {
        this(httpStatus, null, cause);
    }

    public UnexpectedHttpStatusError(HttpStatus httpStatus, String message) {
        this(httpStatus, message, null);
    }

    public UnexpectedHttpStatusError(HttpStatus httpStatus, String message, Error cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.NETWORK_ERROR;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
