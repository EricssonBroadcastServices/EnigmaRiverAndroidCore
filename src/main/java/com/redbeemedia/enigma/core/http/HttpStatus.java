// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.http;

import java.net.HttpURLConnection;

public class HttpStatus {

    private final int code;
    private final String message;
    public static final int HTTP_STATUS_CODE_NO_CONTENT = 204;

    public HttpStatus(final int code,
                      final String message)
    {
        this.code = code;
        this.message = message;
    }

    public int getResponseCode() { return code; }

    public String getResponseMessage() {
        return message;
    }

    public boolean isError() {
        return code != HttpURLConnection.HTTP_OK;
    }

    /** Returns true if the status code is 'No Content (204)` and if message is null. */
    public boolean hasNoContent() { return code == HTTP_STATUS_CODE_NO_CONTENT && message == null; }

    @Override
    public String toString() {
        return new StringBuilder(String.valueOf(code)).append(" ").append(message).toString();
    }
}