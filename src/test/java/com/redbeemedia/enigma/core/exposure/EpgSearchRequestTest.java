package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.exposure.models.epg.ApiEpgSearchHits;
import com.redbeemedia.enigma.core.exposure.models.epg.MockApiEpgSearchHits;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.session.Session;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

public class EpgSearchRequestTest {

    @Test
    public void testOnSuccess() {
        final Flag onSuccessCalled = new Flag();
        final Flag onErrorCalled = new Flag();
        final ApiEpgSearchHits successObject = new MockApiEpgSearchHits();
        IExposureResultHandler<ApiEpgSearchHits> responseHandler = new IExposureResultHandler<ApiEpgSearchHits>() {
            @Override
            public void onSuccess(ApiEpgSearchHits result) {
                if(successObject == result) {
                    onSuccessCalled.setFlag();
                } else {
                    onErrorCalled.setFlag();
                }
            }

            @Override
            public void onError(Error error) {
                onErrorCalled.setFlag();
            }
        };
        EpgSearchRequest epgSearchRequest = new EpgSearchRequest(0, 1, "queryString:yes",responseHandler);
        onSuccessCalled.assertNotSet();
        onErrorCalled.assertNotSet();
        epgSearchRequest.onSuccess(successObject);
        onSuccessCalled.assertSet();
        onErrorCalled.assertNotSet();
    }

    @Test
    public void testOnError() {
        final Flag onSuccessCalled = new Flag();
        final Flag onErrorCalled = new Flag();
        final ApiEpgSearchHits successObject = new MockApiEpgSearchHits();
        IExposureResultHandler<ApiEpgSearchHits> responseHandler = new IExposureResultHandler<ApiEpgSearchHits>() {
            @Override
            public void onSuccess(ApiEpgSearchHits result) {
                if(successObject == result) {
                    onSuccessCalled.setFlag();
                } else {
                    onErrorCalled.setFlag();
                }
            }

            @Override
            public void onError(Error error) {
                onErrorCalled.setFlag();
            }
        };
        EpgSearchRequest request = new EpgSearchRequest(0, 1, "queryString:yes",responseHandler);
        onSuccessCalled.assertNotSet();
        onErrorCalled.assertNotSet();
        request.onError(new UnexpectedError("Fail!"));
        onSuccessCalled.assertNotSet();
        onErrorCalled.assertSet();
    }

    @Test
    public void testGetUrl() throws MalformedURLException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("http://unittest.ericsson.net:443"));
        EpgSearchRequest request = new EpgSearchRequest(0,1,"queryString:yes",new MockExposureResultHandler<ApiEpgSearchHits>());
        UrlPath urlPath = request.getUrl(new BusinessUnit("cU", "bU"));
        Assert.assertEquals("http://unittest.ericsson.net:443/v1/customer/cU/businessunit/bU/content/search/epg/queryString:yes?from=0&to=1", urlPath.toURL().toString());

        request = request.setPageNumber(2);
        urlPath = request.getUrl(new BusinessUnit("cU", "bU"));
        Assert.assertEquals("http://unittest.ericsson.net:443/v1/customer/cU/businessunit/bU/content/search/epg/queryString:yes?from=0&to=1&pageNumber=2", urlPath.toURL().toString());

        request = request.setPageSize(33);
        urlPath = request.getUrl(new BusinessUnit("cU", "bU"));
        Assert.assertEquals("http://unittest.ericsson.net:443/v1/customer/cU/businessunit/bU/content/search/epg/queryString:yes?from=0&to=1&pageSize=33&pageNumber=2", urlPath.toURL().toString());
    }

    @Test
    public void testHttpCallMethod() {
        EpgSearchRequest request = new EpgSearchRequest(0,1, "query:something", new MockExposureResultHandler<>());
        IHttpCall httpCall = request.getHttpCall(new MockSession());
        Assert.assertEquals("GET", httpCall.getRequestMethod());
    }
}
