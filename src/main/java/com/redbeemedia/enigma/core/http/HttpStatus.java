package com.redbeemedia.enigma.core.http;

public class HttpStatus {
    public final int code;
    public final String message;

    public HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
