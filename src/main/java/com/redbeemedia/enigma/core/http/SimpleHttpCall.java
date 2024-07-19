// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SimpleHttpCall implements IHttpCall {
    private final String requestMethod;

    public SimpleHttpCall(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    @Override
    public void prepare(IHttpConnection connection) {
    }

    @Override
    public String getRequestMethod() {
        return requestMethod;
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
    }

    public static IHttpCall GET() {
        return new SimpleHttpCall("GET");
    }

    public static IHttpCall POST(byte[] data) {
        if(data == null) {
            return POST((Object) null);
        }
        return new SimpleHttpCall("POST") {
            @Override
            public void writeBodyTo(OutputStream outputStream) throws IOException {
                outputStream.write(data);
            }
        };
    }

    public static IHttpCall POST(Object data) {
        return POST(data == null ? new byte[0] : data.toString().getBytes(StandardCharsets.UTF_8));
    }
}
