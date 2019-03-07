package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.exposure.models.carousel.ApiCarousel;
import com.redbeemedia.enigma.core.exposure.query.IQueryParameter;
import com.redbeemedia.enigma.core.exposure.query.QueryParameterBuilder;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.util.List;

public class GetCarouselsByGroupRequest extends AbstractExposureRequest<List<ApiCarousel>> {
    private final String groupId;
    private IQueryParameter<String> parentalRatings = QueryParameterBuilder.create(qps, String.class, "parentalRatings");
    private IQueryParameter<Integer> pageSize = QueryParameterBuilder.create(qps, Integer.class, "pageSize", 20);
    private IQueryParameter<Integer> pageNumber = QueryParameterBuilder.create(qps, Integer.class, "pageNumber", 1);
    private IQueryParameter<FieldSet> fieldSet = QueryParameterBuilder.create(qps, FieldSet.class, "fieldSet", FieldSet.PARTIAL);
    private IQueryParameter<String> excludeFields = QueryParameterBuilder.create(qps, String.class, "excludeFields");
    private IQueryParameter<String> includeFields = QueryParameterBuilder.create(qps, String.class, "includeFields");
    private IQueryParameter<String> service = QueryParameterBuilder.create(qps, String.class, "service");
    private IQueryParameter<Boolean> onlyPublished = QueryParameterBuilder.create(qps, Boolean.class, "onlyPublished");

    public GetCarouselsByGroupRequest(String groupId, IExposureResultHandler<List<ApiCarousel>> resultHandler) {
        super("GET", parseListMethod(ApiCarousel.class), resultHandler);
        this.groupId = groupId;
    }

    @Override
    public UrlPath getUrl(ISession session) {
        return qps.applyAll(session.getApiBaseUrl().append("/carouselgroup/").append(groupId));
    }


    public GetCarouselsByGroupRequest setParentalRatings(String parentalRatings) {
        this.parentalRatings.setValue(parentalRatings);
        return this;
    }

    public GetCarouselsByGroupRequest setPageSize(int pageSize) {
        this.pageSize.setValue(pageSize);
        return this;
    }

    public GetCarouselsByGroupRequest setPageNumber(int pageNumber) {
        this.pageNumber.setValue(pageNumber);
        return this;
    }

    public GetCarouselsByGroupRequest setFieldSet(FieldSet fieldSet) {
        this.fieldSet.setValue(fieldSet);
        return this;
    }

    public GetCarouselsByGroupRequest setExcludeFields(String excludeFields) {
        this.excludeFields.setValue(excludeFields);
        return this;
    }

    public GetCarouselsByGroupRequest setIncludeFields(String includeFields) {
        this.includeFields.setValue(includeFields);
        return this;
    }

    public GetCarouselsByGroupRequest setService(String service) {
        this.service.setValue(service);
        return this;
    }

    public GetCarouselsByGroupRequest setOnlyPublished(boolean onlyPublished) {
        this.onlyPublished.setValue(onlyPublished);
        return this;
    }
}
