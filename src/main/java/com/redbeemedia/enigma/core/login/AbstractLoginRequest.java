package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.device.DeviceInfo;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

/*package-protected*/ abstract class AbstractLoginRequest implements ILoginRequest {
    private final String path;
    private final String requestMethod;
    private final ILoginResultHandler resultHandler;

    public AbstractLoginRequest(String path, String requestMethod,ILoginResultHandler resultHandler) {
        this.path = path;
        this.requestMethod = requestMethod;
        this.resultHandler = resultHandler;
    }

    @Override
    public UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException {
        return authenticationBaseUrl.append(path);
    }

    @Override
    public ILoginResultHandler getResultHandler() {
        return resultHandler;
    }

    @Override
    public String getRequestMethod() {
        return requestMethod;
    }

    @Override
    public void prepare(IHttpConnection connection) {
        connection.setHeader("Content-Type", "application/json");
        connection.setHeader("Accept", "application/json");
    }

    protected void addDeviceAndDeviceId(JSONObject jsonObject) throws JSONException {
        IDeviceInfo deviceInfo = EnigmaRiverContext.getDeviceInfo();
        jsonObject.put("deviceId", deviceInfo.getDeviceId());
        jsonObject.put("device", DeviceInfo.getDeviceInfoJson(deviceInfo));
    }
}
