package com.redbeemedia.enigma.core.http;

import java.io.InputStream;
import java.net.URL;

public interface IHttpHandler {
    void doHttp(URL url, IHttpPreparator preparator, IHttpResponseHandler responseHandler);

    interface IHttpResponseHandler {
        void onResponse(HttpStatus httpStatus, InputStream inputStream);
        void onResponse(HttpStatus httpStatus);
    }
}
