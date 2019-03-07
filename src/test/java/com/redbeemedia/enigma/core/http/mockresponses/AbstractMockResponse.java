package com.redbeemedia.enigma.core.http.mockresponses;

import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;

import java.net.URL;

public abstract class AbstractMockResponse implements IHttpHandler {
    @Override
    public void doHttpBlocking(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
        this.doHttp(url, httpCall, responseHandler);
    }
}
