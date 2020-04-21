package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.net.MalformedURLException;

public interface ILoginRequest extends IHttpCall {
    /**
     * @deprecated Use {@link #getTargetUrl(IBusinessUnit)} instead
     * @param authenticationBaseUrl
     * @return
     * @throws MalformedURLException
     */
    @Deprecated
    UrlPath getTargetUrl(UrlPath authenticationBaseUrl) throws MalformedURLException;

    UrlPath getTargetUrl(IBusinessUnit businessUnit) throws MalformedURLException;

    ILoginResultHandler getResultHandler();
}
