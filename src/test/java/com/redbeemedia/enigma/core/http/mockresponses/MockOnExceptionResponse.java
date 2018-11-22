package com.redbeemedia.enigma.core.http.mockresponses;

import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpPreparator;

import java.net.URL;

public class MockOnExceptionResponse implements IHttpHandler {
    private Exception exception;

    public MockOnExceptionResponse(Exception exception) {
        this.exception = exception;
    }

    @Override
    public void doHttp(URL url, IHttpPreparator preparator, IHttpResponseHandler responseHandler) {
        responseHandler.onException(exception);
    }
}
