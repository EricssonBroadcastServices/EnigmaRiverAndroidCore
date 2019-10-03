package com.redbeemedia.enigma.core.error;

import java.lang.String;


/**
 * Server responded with 404 - Not found
 */
public class HttpResourceNotFoundError extends HttpResponseError {
    private String resourceUrl;

    public HttpResourceNotFoundError(String resourceUrl) {
        this(resourceUrl, null);
    }

    public HttpResourceNotFoundError(String resourceUrl, EnigmaError cause) {
        super("URL: "+resourceUrl, cause);
        this.resourceUrl = resourceUrl;
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.NETWORK_ERROR;
    }

    public String getResourceUrl() {
        return this.resourceUrl;
    }
}
