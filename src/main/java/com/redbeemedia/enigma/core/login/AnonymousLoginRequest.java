// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AnonymousLoginRequest extends AbstractLoginRequest implements ILoginRequest {
    public AnonymousLoginRequest(ILoginResultHandler resultHandler) {
        super("v2", "auth/anonymous", "POST", resultHandler);
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        try {
            JSONObject body = new JSONObject();
            addDeviceAndDeviceId(body);
            outputStream.write(body.toString().getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }
}
