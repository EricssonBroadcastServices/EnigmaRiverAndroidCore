// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.http;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ExposureApiCall implements IHttpCall {
    private final String requestMethod;
    private JSONObject jsonBody;

    public ExposureApiCall(String requestMethod) {
        this(requestMethod, null);
    }

    public ExposureApiCall(String requestMethod, JSONObject jsonObject) {
        this.requestMethod = requestMethod;
        this.jsonBody = jsonObject;
    }

    @Override
    public void prepare(IHttpConnection connection) {
        connection.setHeader("Content-Type", "application/json");
        connection.setHeader("Accept", "application/json");
        connection.setDoOutput(jsonBody != null);
    }

    @Override
    public String getRequestMethod() {
        return requestMethod;
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        if(jsonBody != null) {
            outputStream.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
