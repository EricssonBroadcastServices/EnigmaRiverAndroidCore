// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

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
        assertStatus("FORBIDDEN", false, EntitlementStatus.FORBIDDEN);
        assertStatus("NOT_AVAILABLE", false, EntitlementStatus.NOT_AVAILABLE);
        assertStatus("BLOCKED", false, EntitlementStatus.BLOCKED);
        assertStatus("GEO_BLOCKED", false, EntitlementStatus.GEO_BLOCKED);
        assertStatus("CONCURRENT_STREAMS_LIMIT_REACHED", false, EntitlementStatus.CONCURRENT_STREAMS_LIMIT_REACHED);
        assertStatus("NOT_PUBLISHED", false, EntitlementStatus.NOT_PUBLISHED);
        assertStatus("NOT_ENTITLED", false, EntitlementStatus.NOT_ENTITLED);
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
