package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.http.IHttpPreparator;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.net.MalformedURLException;

public interface ILoginRequest extends IHttpPreparator {
    UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException;
    ILoginResultHandler getResultHandler();
}
