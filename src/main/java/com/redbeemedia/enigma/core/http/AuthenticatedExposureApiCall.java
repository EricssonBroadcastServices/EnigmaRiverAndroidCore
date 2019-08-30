package com.redbeemedia.enigma.core.http;

import com.redbeemedia.enigma.core.session.ISession;

import org.json.JSONObject;

public class AuthenticatedExposureApiCall extends ExposureApiCall {
    private ISession session;

    public AuthenticatedExposureApiCall(String requestMethod, ISession session) {
        this(requestMethod, session, null);
    }

    public AuthenticatedExposureApiCall(String requestMethod, ISession session, JSONObject jsonObject) {
        super(requestMethod, jsonObject);
        this.session = session;
    }

    @Override
    public void prepare(IHttpConnection connection) {
        connection.setHeader("Authorization", "Bearer "+session.getSessionToken());
        super.prepare(connection);
    }
}
