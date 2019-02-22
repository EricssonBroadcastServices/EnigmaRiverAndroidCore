package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.exposure.models.channel.ApiChannelEPGResponse;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.session.Session;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class GetEpgDataRequestTest {

    @Test
    public void testOnSuccess() {
        final Flag onSuccessCalled = new Flag();
        final Flag onErrorCalled = new Flag();
        final List<ApiChannelEPGResponse> successObject = new ArrayList<>();
        IExposureResultHandler<List<ApiChannelEPGResponse>> responseHandler = new IExposureResultHandler<List<ApiChannelEPGResponse>>() {
            @Override
            public void onSuccess(List<ApiChannelEPGResponse> result) {
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
        GetEpgDataRequest getEpgDataRequest = new GetEpgDataRequest(0, 1, responseHandler);
        onSuccessCalled.assertNotSet();
        onErrorCalled.assertNotSet();
        getEpgDataRequest.onSuccess(successObject);
        onSuccessCalled.assertSet();
        onErrorCalled.assertNotSet();
    }

    @Test
    public void testOnError() {
        final Flag onSuccessCalled = new Flag();
        final Flag onErrorCalled = new Flag();
        final List<ApiChannelEPGResponse> successObject = new ArrayList<>();
        IExposureResultHandler<List<ApiChannelEPGResponse>> responseHandler = new IExposureResultHandler<List<ApiChannelEPGResponse>>() {
            @Override
            public void onSuccess(List<ApiChannelEPGResponse> result) {
                onSuccessCalled.setFlag();
            }

            @Override
            public void onError(Error error) {
                onErrorCalled.setFlag();
            }
        };
        GetEpgDataRequest getEpgDataRequest = new GetEpgDataRequest(0, 1, responseHandler);
        onSuccessCalled.assertNotSet();
        onErrorCalled.assertNotSet();
        getEpgDataRequest.onError(new UnexpectedError("Fail!"));
        onSuccessCalled.assertNotSet();
        onErrorCalled.assertSet();
    }

    @Test
    public void testGetUrl() throws MalformedURLException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("http://unittest.ericsson.net:443"));
        GetEpgDataRequest request = new GetEpgDataRequest(0,1,new MockExposureResultHandler<List<ApiChannelEPGResponse>>());
        UrlPath urlPath1 = request.getUrl(new Session("sessToken", "cU", "bU"));
        Assert.assertEquals("http://unittest.ericsson.net:443/v1/customer/cU/businessunit/bU/epg?from=0&to=1", urlPath1.toURL().toString());

        request = request.setIncludeUserData(true);
        UrlPath urlPath2 = request.getUrl(new Session("sessToken", "cU", "bU"));
        Assert.assertEquals("http://unittest.ericsson.net:443/v1/customer/cU/businessunit/bU/epg?from=0&to=1&includeUserData=true", urlPath2.toURL().toString());
    }

    @Test
    public void testHttpCallMethod() {
        GetEpgDataRequest request = new GetEpgDataRequest(0,1,new MockExposureResultHandler<List<ApiChannelEPGResponse>>());
        IHttpCall httpCall = request.getHttpCall(new MockSession());
        Assert.assertEquals("GET", httpCall.getRequestMethod());
    }
}
