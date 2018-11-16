package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.net.MalformedURLException;

public class MockLoginRequest implements ILoginRequest {
    @Override
    public UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException {
        return authenticationBaseUrl.append("auth/mock");
    }

    @Override
    public ILoginResultHandler getResultHandler() {
        return new MockLoginResultHandler();
    }
}
