package com.redbeemedia.enigma.core.http;

import java.io.InputStream;
import java.net.URL;

public interface IHttpHandler {
    void doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler);

    interface IHttpResponseHandler {
        void onResponse(HttpStatus httpStatus);
        void onResponse(HttpStatus httpStatus, InputStream inputStream);
        void onException(Exception e);
    }
}
