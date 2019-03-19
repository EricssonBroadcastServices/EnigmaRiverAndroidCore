package com.redbeemedia.enigma.core.http;

import java.net.HttpURLConnection;

public class HttpStatus {

    private final int code;
    private final String message;

    public HttpStatus(final int code,
                      final String message)
    {
        this.code = code;
        this.message = message;
    }

    public int getResponseCode() {
        return code;
    }

    public String getResponseMessage() {
        return message;
    }

    public boolean isError() {
        return code != HttpURLConnection.HTTP_OK;
    }

    @Override
    public String toString() {
        return new StringBuilder(String.valueOf(code)).append(" ").append(message).toString();
    }
}