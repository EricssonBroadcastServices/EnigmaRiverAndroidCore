package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.http.IHttpConnection;

import java.io.OutputStream;

//TODO: change the name
public class ReLoginRequest extends AbstractLoginRequest implements ILoginRequest {
    private String sessionToken;

    public ReLoginRequest(String sessionToken, ILoginResultHandler resultHandler) {
        super("auth/session", "GET", resultHandler);
        this.sessionToken = sessionToken;
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) {
    }

    @Override
    public void prepare(IHttpConnection connection) {
        connection.setHeader("Authorization", "Bearer " + sessionToken);
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
