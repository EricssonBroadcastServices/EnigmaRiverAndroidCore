package com.redbeemedia.enigma.core.http.mockresponses;

import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpCall;

import java.net.URL;

public class MockOnResponseNoInputstreamResponse extends AbstractMockResponse {
    private HttpStatus httpStatus;

    public MockOnResponseNoInputstreamResponse(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public void doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
        responseHandler.onResponse(httpStatus);
    }
}
