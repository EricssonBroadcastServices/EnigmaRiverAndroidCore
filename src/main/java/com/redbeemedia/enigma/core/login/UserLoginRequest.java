package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.util.device.DeviceInfo;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class UserLoginRequest extends AbstractLoginRequest implements ILoginRequest {
    private String username;
    private String password;

    public UserLoginRequest(String username, String password, ILoginResultHandler resultHandler) {
        super("auth/login", "POST", resultHandler);
        this.username = username;
        this.password = password;
    }


    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        IDeviceInfo deviceInfo = EnigmaRiverContext.getDeviceInfo();

        try {
            JSONObject body = new JSONObject();
            body.put("deviceId", deviceInfo.getDeviceId());
            body.put("device", DeviceInfo.getDeviceInfoJson(deviceInfo));
            body.put("username", username);
            body.put("password", password);

            outputStream.write(body.toString().getBytes("utf-8"));
        } catch (JSONException e){
            throw new IOException(e);
        }
    }
}
