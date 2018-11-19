package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MockLoginRequest implements ILoginRequest {
    private Map<String,String> headers = new LinkedHashMap<>();

    @Override
    public UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException {
        return authenticationBaseUrl.append("auth/mock");
    }

    @Override
    public ILoginResultHandler getResultHandler() {
        return new MockLoginResultHandler();
    }

    @Override
    public void prepare(IHttpConnection connection) {
        for(Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setHeader(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String getRequestMethod() {
        return "POST";
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) {
    }

    public MockLoginRequest addHeader(String header, String value) {
        headers.put(header, value);
        return this;
    }
}
