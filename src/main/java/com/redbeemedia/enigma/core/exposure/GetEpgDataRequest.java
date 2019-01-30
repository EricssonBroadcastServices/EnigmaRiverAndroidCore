package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.util.UrlPath;

public class GetEpgDataRequest implements IGetEpgDataRequest {
    private String fromMillis;
    private String toMillis;
    private Boolean includeUserData = null;
    private IGetEpgDataResultHandler resultHandler;

    public GetEpgDataRequest(long fromMillis, long toMillis, IGetEpgDataResultHandler resultHandler) {
        this.fromMillis = String.valueOf(fromMillis);
        this.toMillis = String.valueOf(toMillis);
        this.resultHandler = resultHandler;
    }

    @Override
    public UrlPath appendQueryParameters(UrlPath basePath) {
        UrlPath path = basePath.append("?from=").append(fromMillis).append("&to=").append(toMillis);
        if(includeUserData != null) {
            path = path.append("&includeUserData="+includeUserData.toString());
        }
        return path;
    }

    @Override
    public IGetEpgDataResultHandler getResultHandler() {
        return resultHandler;
    }

    public GetEpgDataRequest setIncludeUserData(boolean value) {
        this.includeUserData = Boolean.valueOf(value);
        return this;
    }
}
