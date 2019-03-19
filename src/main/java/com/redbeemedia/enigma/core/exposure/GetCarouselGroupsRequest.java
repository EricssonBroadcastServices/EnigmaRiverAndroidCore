package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.util.List;

public class GetCarouselGroupsRequest extends AbstractExposureRequest<List<String>> {
    public GetCarouselGroupsRequest(IExposureResultHandler<List<String>> resultHandler) {
        super("GET", parseListMethod(String.class), resultHandler);
    }

    @Override
    public UrlPath getUrl(IBusinessUnit businessUnit) {
        return businessUnit.getApiBaseUrl().append("/carouselgroup");
    }
}
