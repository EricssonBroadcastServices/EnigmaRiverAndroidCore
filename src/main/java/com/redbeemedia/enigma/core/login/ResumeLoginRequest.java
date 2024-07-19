// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.io.OutputStream;
import java.net.MalformedURLException;

public class ResumeLoginRequest extends AbstractLoginRequest implements ILoginRequest {
    private String sessionToken;

    public ResumeLoginRequest(String sessionToken, ILoginResultHandler resultHandler) {
        super("v2", "auth/session", "GET", resultHandler);
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
