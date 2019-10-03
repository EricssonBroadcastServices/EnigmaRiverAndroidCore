package com.redbeemedia.enigma.core.entitlement;

import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.ISO8601Util;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimeZone;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class EntitlementRequest implements IEntitlementRequest {
    private final ISession session;
    private final String assetId;
    private String time = null;

    public EntitlementRequest(ISession session, String assetId) {
        if(session == null) {
            throw new NullPointerException("session is null");
        }
        if(assetId == null) {
            throw new NullPointerException("assetId is null");
        }
        this.session = session;
        this.assetId = assetId;
    }

    public EntitlementRequest setTime(long utcMillis) {
        ISO8601Util.IISO8601Writer writer = ISO8601Util.newWriter(TimeZone.getTimeZone("UTC"));
        this.time = writer.toIso8601(utcMillis);
        return this;
    }

    @Override
    public void doHttpCall(IHttpHandler httpHandler, IHttpHandler.IHttpResponseHandler responseHandler) throws MalformedURLException {
        UrlPath urlPath = session.getBusinessUnit().getApiBaseUrl("v2").append("/entitlement/").append(assetId).append("/entitle");
        if(time != null) {
            urlPath = urlPath.append("?time="+time);
        }
        URL url = urlPath.toURL();
        httpHandler.doHttp(url, new AuthenticatedExposureApiCall("GET", session), responseHandler);
    }

    @Override
    public String getAssetId() {
        return assetId;
    }
}
