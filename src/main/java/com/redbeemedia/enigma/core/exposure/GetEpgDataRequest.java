package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.exposure.models.channel.ApiChannelEPGResponse;
import com.redbeemedia.enigma.core.exposure.query.IQueryParameter;
import com.redbeemedia.enigma.core.exposure.query.QueryParameterBuilder;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.util.List;

public class GetEpgDataRequest extends AbstractExposureRequest<List<ApiChannelEPGResponse>> {
    private IQueryParameter<Long> fromMillis = QueryParameterBuilder.create(qps, Long.class, "from");
    private IQueryParameter<Long> toMillis = QueryParameterBuilder.create(qps, Long.class, "to");
    private IQueryParameter<Boolean> includeUserData = QueryParameterBuilder.create(qps, Boolean.class, "includeUserData");

    public GetEpgDataRequest(long fromMillis, long toMillis, IExposureResultHandler<List<ApiChannelEPGResponse>> resultHandler) {
        super("GET", parseListMethod(ApiChannelEPGResponse.class), resultHandler);
        this.fromMillis.setValue(fromMillis);
        this.toMillis.setValue(toMillis);
    }

    public GetEpgDataRequest setIncludeUserData(boolean includeUserData) {
        this.includeUserData.setValue(includeUserData);
        return this;
    }

    @Override
    public UrlPath getUrl(ISession session) {
        return qps.applyAll(session.getApiBaseUrl().append("epg"));
    }
}
