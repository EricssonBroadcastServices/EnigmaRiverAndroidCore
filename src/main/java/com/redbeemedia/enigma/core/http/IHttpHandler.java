package com.redbeemedia.enigma.core.http;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.net.URL;

public interface IHttpHandler {
    void doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler);
    void doHttpBlocking(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) throws InterruptedException;

    interface IHttpResponseHandler {
        void onResponse(@NonNull HttpStatus httpStatus);
        void onResponse(@NonNull HttpStatus httpStatus, @NonNull InputStream inputStream);
        void onException(@NonNull Exception e);
    }
}
