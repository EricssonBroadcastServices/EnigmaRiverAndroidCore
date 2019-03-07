package com.redbeemedia.enigma.core.http.mockresponses;

import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpCall;

import java.net.URL;

public class MockOnExceptionResponse extends AbstractMockResponse {
    private Exception exception;

    public MockOnExceptionResponse(Exception exception) {
        this.exception = exception;
    }

    @Override
    public void doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
        responseHandler.onException(exception);
    }
}
