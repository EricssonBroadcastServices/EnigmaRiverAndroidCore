package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.exposure.models.epg.ApiEpgSearchHits;
import com.redbeemedia.enigma.core.exposure.query.IQueryParameter;
import com.redbeemedia.enigma.core.exposure.query.QueryParameterBuilder;
import com.redbeemedia.enigma.core.util.UrlPath;

public class EpgSearchRequest extends AbstractExposureRequest<ApiEpgSearchHits> {
    private String query; //Note that query is sent as a path parameter to this endpoint, and not a query parameter

    private IQueryParameter<Long> fromMillis = QueryParameterBuilder.create(qps, Long.class, "from");
    private IQueryParameter<Long> toMillis = QueryParameterBuilder.create(qps, Long.class, "to");
    private IQueryParameter<Integer> pageSize = QueryParameterBuilder.create(qps, Integer.class, "pageSize");
    private IQueryParameter<Integer> pageNumber = QueryParameterBuilder.create(qps, Integer.class, "pageNumber");

    public EpgSearchRequest(long from, long to, String query, IExposureResultHandler<ApiEpgSearchHits> resultHandler) {
        super("GET", parseObjectMethod(ApiEpgSearchHits.class), resultHandler);
        this.fromMillis.setValue(from);
        this.toMillis.setValue(to);
        //TODO add utility for query construction.
        this.query = query;
    }

    public EpgSearchRequest setPageSize(int pageSize) {
        this.pageSize.setValue(pageSize);
        return this;
    }

    public EpgSearchRequest setPageNumber(int pageNumber) {
        this.pageNumber.setValue(pageNumber);
        return this;
    }

    @Override
    public UrlPath getUrl(IBusinessUnit businessUnit) {
        return qps.applyAll(businessUnit.getApiBaseUrl().append("content/search/epg").append(query));
    }
}
