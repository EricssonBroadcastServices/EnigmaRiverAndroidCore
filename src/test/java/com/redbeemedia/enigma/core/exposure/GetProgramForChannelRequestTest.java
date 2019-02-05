package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.exposure.models.program.ApiProgramResponse;
import com.redbeemedia.enigma.core.exposure.models.program.MockApiProgramResponse;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.session.Session;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

public class GetProgramForChannelRequestTest {

    @Test
    public void testOnSuccess() {
        final Flag onSuccessCalled = new Flag();
        final Flag onErrorCalled = new Flag();
        final ApiProgramResponse successObject = new MockApiProgramResponse();
        IExposureResultHandler<ApiProgramResponse> responseHandler = new IExposureResultHandler<ApiProgramResponse>() {
            @Override
            public void onSuccess(ApiProgramResponse result) {
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
        GetProgramForChannelRequest epgSearchRequest = new GetProgramForChannelRequest("mockChannelId", "mockProgramId", responseHandler);
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
        final ApiProgramResponse successObject = new MockApiProgramResponse();
        IExposureResultHandler<ApiProgramResponse> responseHandler = new IExposureResultHandler<ApiProgramResponse>() {
            @Override
            public void onSuccess(ApiProgramResponse result) {
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
        GetProgramForChannelRequest request = new GetProgramForChannelRequest("mockChannelId", "mockProgramId",responseHandler);
        onSuccessCalled.assertNotSet();
        onErrorCalled.assertNotSet();
        request.onError(Error.UNEXPECTED_ERROR);
        onSuccessCalled.assertNotSet();
        onErrorCalled.assertSet();
    }

    @Test
    public void testGetUrl() throws MalformedURLException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("http://unittest.ericsson.net:443"));
        GetProgramForChannelRequest request = new GetProgramForChannelRequest("mockChannelId", "mockProgramId",new MockExposureResultHandler<ApiProgramResponse>());
        UrlPath urlPath = request.getUrl(new Session("sessToken", "cU", "bU"));
        Assert.assertEquals("http://unittest.ericsson.net:443/v1/customer/cU/businessunit/bU/epg/mockChannelId/program/mockProgramId?", urlPath.toURL().toString());

        request = request.setIncludeUserData(false);
        urlPath = request.getUrl(new Session("sessToken", "cU", "bU"));
        Assert.assertEquals("http://unittest.ericsson.net:443/v1/customer/cU/businessunit/bU/epg/mockChannelId/program/mockProgramId?includeUserData=false", urlPath.toURL().toString());
    }

    @Test
    public void testHttpCallMethod() {
        GetProgramForChannelRequest request = new GetProgramForChannelRequest("mockChannelId", "mockProgramId", new MockExposureResultHandler<>());
        IHttpCall httpCall = request.getHttpCall(new MockSession());
        Assert.assertEquals("GET", httpCall.getRequestMethod());
    }
}
