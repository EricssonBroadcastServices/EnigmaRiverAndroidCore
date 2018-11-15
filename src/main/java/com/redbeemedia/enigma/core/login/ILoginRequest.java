package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.http.IHttpPreparator;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.net.MalformedURLException;

public interface ILoginRequest /*extends IHttpPreparator*/ {

//    void onError(...);
    UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException;

    void onSuccess(ISession session);
}
