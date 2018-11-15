package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.net.MalformedURLException;

/*package-protected*/ abstract class AbstractLoginRequest implements ILoginRequest {
    private final String path;

    public AbstractLoginRequest(String path) {
        this.path = path;
    }

    @Override
    public void onSuccess(ISession session) {
        //TODO don't have here!
    }

    @Override
    public UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException {
        return authenticationBaseUrl.append(path);
    }
}
