package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.util.UrlPath;

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
}
