package com.redbeemedia.enigma.core.http.mockresponses;

import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpPreparator;

import java.net.URL;

public class MockOnResponseNoInputstreamResponse implements IHttpHandler {
    private HttpStatus httpStatus;

    public MockOnResponseNoInputstreamResponse(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public void doHttp(URL url, IHttpPreparator preparator, IHttpResponseHandler responseHandler) {
        responseHandler.onResponse(httpStatus);
    }
}
