// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

public class ApiKeyLoginRequest implements ILoginRequest {
    private final String username;
    private final String apiKey;
    private final ILoginResultHandler resultHandler;

    public ApiKeyLoginRequest(String username, String apiKey, ILoginResultHandler resultHandler) {
        this.username = username;
        this.apiKey = apiKey;
        this.resultHandler = resultHandler;
    }

    @Override
    public void prepare(IHttpConnection connection) {
        connection.setHeader("Content-Type", "application/json");
        connection.setHeader("Accept", "application/json");

        connection.setHeader("EMP-Auth", apiKey);
    }

    @Override
    public String getRequestMethod() {
        return "POST";
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            JSONObject deviceRegistration = new JSONObject();
            IDeviceInfo deviceInfo = EnigmaRiverContext.getDeviceInfo();
            deviceRegistration.put("deviceId", deviceInfo.getDeviceId());
            deviceRegistration.put("name", deviceInfo.getName());
            body.put("device", deviceRegistration);
            outputStream.write(body.toString().getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            throw new IOException("Failed to construct json", e);
        }
    }

    @Override
    public UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException {
        throw new UnsupportedOperationException("Use getTargetUrl(IBusinessUnit) instead");
    }

    @Override
    public UrlPath getTargetUrl(IBusinessUnit businessUnit) throws MalformedURLException {
        return businessUnit.getApiBaseUrl("v2").append("/auth/session");
    }

    @Override
    public ILoginResultHandler getResultHandler() {
        return resultHandler;
    }
}
