package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.exposure.models.carousel.ApiCarousel;
import com.redbeemedia.enigma.core.exposure.query.IQueryParameter;
import com.redbeemedia.enigma.core.exposure.query.QueryParameterBuilder;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;


public class GetCarouselByIdRequest extends AbstractExposureRequest<ApiCarousel> {
    private final String groupId;
    private final String carouselId;
    private IQueryParameter<String> parentalRatings = QueryParameterBuilder.create(qps, String.class, "parentalRatings");
    private IQueryParameter<Integer> pageSize = QueryParameterBuilder.create(qps, Integer.class, "pageSize", 20);
    private IQueryParameter<Integer> pageNumber = QueryParameterBuilder.create(qps, Integer.class, "pageNumber", 1);
    private IQueryParameter<FieldSet> fieldSet = QueryParameterBuilder.create(qps, FieldSet.class, "fieldSet", FieldSet.PARTIAL);
    private IQueryParameter<String> excludeFields = QueryParameterBuilder.create(qps, String.class, "excludeFields");
    private IQueryParameter<String> includeFields = QueryParameterBuilder.create(qps, String.class, "includeFields");
    private IQueryParameter<String> service = QueryParameterBuilder.create(qps, String.class, "service");
    private IQueryParameter<Boolean> onlyPublished = QueryParameterBuilder.create(qps, Boolean.class, "onlyPublished");

    public GetCarouselByIdRequest(String groupId, String carouselId, IExposureResultHandler<ApiCarousel> resultHandler) {
        super("GET", parseObjectMethod(ApiCarousel.class), resultHandler);
        this.groupId = groupId;
        this.carouselId = carouselId;
    }

    @Override
    public UrlPath getUrl(IBusinessUnit businessUnit) {
        return qps.applyAll(businessUnit.getApiBaseUrl().append("carouselgroup").append(groupId).append("carousel").append(carouselId));
    }

    public GetCarouselByIdRequest setParentalRatings(String parentalRatings) {
        this.parentalRatings.setValue(parentalRatings);
        return this;
    }

    public GetCarouselByIdRequest setPageSize(int pageSize) {
        this.pageSize.setValue(pageSize);
        return this;
    }

    public GetCarouselByIdRequest setPageNumber(int pageNumber) {
        this.pageNumber.setValue(pageNumber);
        return this;
    }

    public GetCarouselByIdRequest setFieldSet(FieldSet fieldSet) {
        this.fieldSet.setValue(fieldSet);
        return this;
    }

    public GetCarouselByIdRequest setExcludeFields(String excludeFields) {
        this.excludeFields.setValue(excludeFields);
        return this;
    }

    public GetCarouselByIdRequest setIncludeFields(String includeFields) {
        this.includeFields.setValue(includeFields);
        return this;
    }

    public GetCarouselByIdRequest setService(String service) {
        this.service.setValue(service);
        return this;
    }

    public GetCarouselByIdRequest setOnlyPublished(boolean onlyPublished) {
        this.onlyPublished.setValue(onlyPublished);
        return this;
    }
}
