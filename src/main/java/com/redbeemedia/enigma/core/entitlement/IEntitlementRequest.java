package com.redbeemedia.enigma.core.entitlement;

import com.redbeemedia.enigma.core.http.IHttpHandler;

import java.net.MalformedURLException;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IEntitlementRequest {
    String getAssetId();
    void doHttpCall(IHttpHandler httpHandler, IHttpHandler.IHttpResponseHandler responseHandler) throws MalformedURLException;
}
