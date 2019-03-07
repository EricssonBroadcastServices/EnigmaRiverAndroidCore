package com.redbeemedia.enigma.core.http;

import com.redbeemedia.enigma.core.session.ISession;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthenticatedExposureApiCall implements IHttpCall {
    private final String requestMethod;
    private ISession session;
    private JSONObject jsonBody;

    public AuthenticatedExposureApiCall(String requestMethod, ISession session) {
        this(requestMethod, session, null);
    }

    public AuthenticatedExposureApiCall(String requestMethod, ISession session, JSONObject jsonObject) {
        this.requestMethod = requestMethod;
        this.session = session;
        this.jsonBody = jsonObject;
    }

    @Override
    public void prepare(IHttpConnection connection) {
        connection.setHeader("Authorization", "Bearer "+session.getSessionToken());
        connection.setHeader("Content-Type", "application/json");
        connection.setHeader("Accept", "application/json");
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
