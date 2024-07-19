// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.http;

import java.io.InputStream;
import java.net.URL;

public interface IHttpHandler {
    IHttpTask doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler);
    void doHttpBlocking(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) throws InterruptedException;

    interface IHttpResponseHandler {
        void onResponse(HttpStatus httpStatus);
        void onResponse(HttpStatus httpStatus, InputStream inputStream);
        void onException(Exception e);
    }
}
