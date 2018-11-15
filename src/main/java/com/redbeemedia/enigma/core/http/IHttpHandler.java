package com.redbeemedia.enigma.core.http;

import java.io.InputStream;
import java.net.URL;

public interface IHttpHandler {
    void post(URL url, IHttpPreparator preparator, IHttpHandlerResponse responseHandler);

    interface IHttpHandlerResponse {
        void onResponse(int code, InputStream inputStream);
        void onResponse(int code);
    }
}
