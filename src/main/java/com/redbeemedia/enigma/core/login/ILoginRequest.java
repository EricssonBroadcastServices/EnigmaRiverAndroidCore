package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.net.MalformedURLException;

public interface ILoginRequest extends IHttpCall {
    UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException;
    ILoginResultHandler getResultHandler();
}
