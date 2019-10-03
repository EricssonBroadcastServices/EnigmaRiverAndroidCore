package com.redbeemedia.enigma.core.entitlement;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.testutil.Counter;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

public class EntitlementProviderTest {
    @Test
    public void testEntitlementCheck() {
        assertStatus("SUCCESS", true, EntitlementStatus.SUCCESS);
        assertStatus("NOT_ENTITLED", false, EntitlementStatus.NOT_ENTITLED);
        assertStatus("GEO_BLOCKED", false, EntitlementStatus.GEO_BLOCKED);
        assertStatus("DOWNLOAD_BLOCKED", false, EntitlementStatus.DOWNLOAD_BLOCKED);
        assertStatus("DEVICE_BLOCKED", false, EntitlementStatus.DEVICE_BLOCKED);
        assertStatus("LICENSE_EXPIRED", false, EntitlementStatus.LICENSE_EXPIRED);
        assertStatus("NOT_AVAILABLE_IN_FORMAT", false, EntitlementStatus.NOT_AVAILABLE_IN_FORMAT);
        assertStatus("CONCURRENT_STREAMS_LIMIT_REACHED", false, EntitlementStatus.CONCURRENT_STREAMS_LIMIT_REACHED);
        assertStatus("NOT_ENABLED", false, EntitlementStatus.NOT_ENABLED);
        assertStatus("GAP_IN_EPG", false, EntitlementStatus.GAP_IN_EPG);
        assertStatus("EPG_PLAY_MAX_HOURS", false, EntitlementStatus.EPG_PLAY_MAX_HOURS);
        assertStatus("ANONYMOUS_IP_BLOCKED", false, EntitlementStatus.ANONYMOUS_IP_BLOCKED);
        assertStatus("undefined_value", false, null);
    }

    private void assertStatus(final String jsonStatus, boolean successExpectation, EntitlementStatus expectedStatus) {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        EntitlementProvider entitlementProvider = new EntitlementProvider(new MockHttpHandler());
        final Counter onResponseCalled = new Counter();
        entitlementProvider.checkEntitlement(new IEntitlementRequest() {
            @Override
            public String getAssetId() {
                return "mockMock";
            }

            @Override
            public void doHttpCall(IHttpHandler httpHandler, IHttpHandler.IHttpResponseHandler responseHandler) throws MalformedURLException {
                byte[] data = ("{\"status\":\""+jsonStatus+"\"}").getBytes(StandardCharsets.UTF_8);
                responseHandler.onResponse(new HttpStatus(200, "OK"), new ByteArrayInputStream(data));
            }
        }, new IEntitlementResponseHandler() {
            @Override
            public void onResponse(EntitlementData entitlementData) {
                Assert.assertEquals(successExpectation, entitlementData.isSuccess());
                Assert.assertEquals(expectedStatus, entitlementData.getStatus());
                onResponseCalled.count();
            }

            @Override
            public void onError(EnigmaError error) {
                Assert.fail(error.getTrace());
            }
        });
        onResponseCalled.assertOnce();
    }
}
