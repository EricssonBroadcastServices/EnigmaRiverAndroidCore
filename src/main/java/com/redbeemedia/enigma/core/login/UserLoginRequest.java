package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

public class UserLoginRequest extends AbstractLoginRequest implements ILoginRequest {
    private String username;
    private String password;

    public UserLoginRequest(String username, String password, ILoginResultHandler resultHandler) {
        super("auth/login", "POST", resultHandler);
        this.username = username;
        this.password = password;
    }

    @Override
    public UrlPath getTargetUrl(IBusinessUnit businessUnit) throws MalformedURLException {
        return getTargetUrl(businessUnit.getApiBaseUrl("v3"));
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        try {
            JSONObject body = new JSONObject();
            addDeviceAndDeviceId(body);
            body.put("username", username);
            body.put("password", password);

            outputStream.write(body.toString().getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e){
            throw new IOException(e);
        }
    }
}
