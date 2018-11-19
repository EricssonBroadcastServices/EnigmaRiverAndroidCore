package com.redbeemedia.enigma.core.login;

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
        //TODO refactor:
        String body = "{\"deviceId\":\"9159b635e19ff412\",\"device\":{\"height\":1794,\"width\":1080,\"model\":\"Android SDK built for x86\",\"name\":\"\",\"os\":\"Android\",\"osVersion\":\"9\",\"manufacturer\":\"Google\",\"deviceId\":\"9159b635e19ff412\",\"type\":\"MOBILE\"},\"rememberMe\":true,\"username\":\""+username+"\",\"password\":\""+password+"\"}";
        outputStream.write(body.getBytes("utf-8"));
    }
}
