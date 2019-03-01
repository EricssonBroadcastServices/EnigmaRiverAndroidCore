package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.AnonymousIpBlockedError;
import com.redbeemedia.enigma.core.error.AssetGeoBlockedError;
import com.redbeemedia.enigma.core.error.AssetNotAvailableForDeviceError;
import com.redbeemedia.enigma.core.error.AssetNotEnabledError;
import com.redbeemedia.enigma.core.error.AssetRestrictedError;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.InternalError;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.InvalidJsonToServerError;
import com.redbeemedia.enigma.core.error.InvalidSessionTokenError;
import com.redbeemedia.enigma.core.error.LicenceExpiredError;
import com.redbeemedia.enigma.core.error.NotEntitledToAssetError;
import com.redbeemedia.enigma.core.error.ServerTimeoutError;
import com.redbeemedia.enigma.core.error.TooManyConcurrentStreamsError;
import com.redbeemedia.enigma.core.error.TooManyConcurrentSvodStreamsError;
import com.redbeemedia.enigma.core.error.TooManyConcurrentTvodStreamsError;
import com.redbeemedia.enigma.core.error.UnexpectedHttpStatusError;
import com.redbeemedia.enigma.core.error.UnknownBusinessUnitError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.testutil.HttpToErrorValidator;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class PlayResponseHandlerTest {
    private static HttpToErrorValidator errorValidator = new HttpToErrorValidator() {
        /**
         * Add new expected errors here.
         * @param registry
         */
        @Override
        protected void onRegisterErrorExpectation(IExpectationRegistry registry) {
            registry.registerExpectedType(400,  "Any message",InvalidJsonToServerError.class);
            registry.registerExpectedType(401,  "Any message",InvalidSessionTokenError.class);
            registry.registerExpectedType(403,  "NOT_AVAILABLE_IN_FORMAT",InternalError.class);
            registry.registerExpectedType(403,  "FORBIDDEN",UnknownBusinessUnitError.class);
            registry.registerExpectedType(403,  "NOT_ENTITLED",NotEntitledToAssetError.class);
            registry.registerExpectedType(403,  "DEVICE_BLOCKED",AssetNotAvailableForDeviceError.class);
            registry.registerExpectedType(403,  "NOT_ENABLED",AssetNotEnabledError.class);
            registry.registerExpectedType(403,  "GEO_BLOCKED",AssetGeoBlockedError.class);
            registry.registerExpectedType(403,  "LICENSE_EXPIRED",LicenceExpiredError.class);
            registry.registerExpectedType(403,  "CONCURRENT_STREAMS_LIMIT_REACHED",TooManyConcurrentStreamsError.class);
            registry.registerExpectedType(403,  "CONCURRENT_STREAMS_TVOD_LIMIT_REACHED",TooManyConcurrentTvodStreamsError.class);
            registry.registerExpectedType(403,  "CONCURRENT_STREAMS_SVOD_LIMIT_REACHED",TooManyConcurrentSvodStreamsError.class);
            registry.registerExpectedType(403,  "ANONYMOUS_IP_BLOCKED",AnonymousIpBlockedError.class);
            registry.registerExpectedType(403,  "Other message",AssetRestrictedError.class);
            registry.registerExpectedType(404,  "UNKNOWN_BUSINESS_UNIT",UnknownBusinessUnitError.class);
            registry.registerExpectedType(404,  "UNKNOWN_ASSET",InvalidAssetError.class);
            registry.registerExpectedType(404,  "Other message",UnexpectedHttpStatusError.class);
            registry.registerExpectedType(422,  "Any message",InvalidJsonToServerError.class);
            registry.registerExpectedType(500,  "Any message",ServerTimeoutError.class);
        }
    };

    @Test
    public void testPlayResponseJsonException() throws JSONException {
        final Flag onSuccessCalled = new Flag();
        final Counter errorCounter = new Counter();
        PlayResponseHandler playResponseHandler = new PlayResponseHandler("MOCK_ASSET_ID") {
            @Override
            protected void onSuccess(JSONObject jsonObject) throws JSONException {
                onSuccessCalled.setFlag();
            }

            @Override
            protected void onError(Error error) {
                errorCounter.count();
            }
        };

        respondFaulty(playResponseHandler, 400, "NOT OK");

        errorCounter.assertOnce();
        onSuccessCalled.assertNotSet("onSuccess was not expected to be called");
    }


    @Test
    public void testPlayResponseErrors() throws JSONException {
        for (final HttpToErrorValidator.ErrorExpectation errorExpectation : errorValidator.getErrorExpectations()) {
            final Flag onSuccessCalled = new Flag();
            final Counter errorCounter = new Counter();
            PlayResponseHandler playResponseHandler = new PlayResponseHandler("MOCK_ASSET_ID") {
                @Override
                protected void onSuccess(JSONObject jsonObject) throws JSONException {
                    onSuccessCalled.setFlag();
                }

                @Override
                protected void onError(Error error) {
                    errorCounter.count();
                    Assert.assertThat(error, errorExpectation.getErrorMatcher());
                }
            };
            errorExpectation.respond(new PlayResponseAdapter(playResponseHandler));
            onSuccessCalled.assertNotSet("onSuccess was not expected to be called");
            errorCounter.assertOnce();
        }
    }

    private static void respondFaulty(PlayResponseHandler playResponseHandler, int code, String message) throws JSONException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("code", code);
        jsonResponse.put("massage", message);
        try {
            playResponseHandler.onResponse(new HttpStatus(code, message), new ByteArrayInputStream(jsonResponse.toString().getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class PlayResponseAdapter implements HttpToErrorValidator.IHttpResponseHandler {
        private PlayResponseHandler playResponseHandler;

        public PlayResponseAdapter(PlayResponseHandler playResponseHandler) {
            this.playResponseHandler = playResponseHandler;
        }

        @Override
        public void onResponse(HttpStatus status, InputStream inputStream) {
            playResponseHandler.onResponse(status, inputStream);
        }
    }
}
