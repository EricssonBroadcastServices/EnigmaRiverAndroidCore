package com.redbeemedia.enigma.core.error;

import com.redbeemedia.enigma.core.http.HttpStatus;


public class UnexpectedHttpStatusError extends HttpResponseError {
    private HttpStatus httpStatus;

    public UnexpectedHttpStatusError(HttpStatus httpStatus) {
        this(httpStatus, null);
    }

    public UnexpectedHttpStatusError(HttpStatus httpStatus, EnigmaError cause) {
        super(String.valueOf(httpStatus), cause);
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
