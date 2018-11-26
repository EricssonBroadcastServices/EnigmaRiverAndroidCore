package com.redbeemedia.enigma.core.login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class AnonymousLoginRequest extends AbstractLoginRequest implements ILoginRequest {
    public AnonymousLoginRequest(ILoginResultHandler resultHandler) {
        super("auth/anonymous", "POST", resultHandler);
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        try {
            JSONObject body = new JSONObject();
            addDeviceAndDeviceId(body);
            outputStream.write(body.toString().getBytes("utf-8"));
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }
}
