package com.redbeemedia.enigma.core.http.mockresponses;

import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpCall;

import java.io.ByteArrayInputStream;
import java.net.URL;

public class MockOnResponseResponse extends AbstractMockResponse {
    private HttpStatus httpStatus;
    private byte[] data;

    public MockOnResponseResponse(HttpStatus httpStatus, byte[] data) {
        this.httpStatus = httpStatus;
        this.data = data;
    }

    @Override
    public void doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
        responseHandler.onResponse(httpStatus, new ByteArrayInputStream(data));
    }
}
