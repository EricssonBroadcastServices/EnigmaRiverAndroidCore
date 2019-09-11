package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.error.DeviceLimitReachedError;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.InvalidCredentialsError;
import com.redbeemedia.enigma.core.error.InvalidJsonToServerError;
import com.redbeemedia.enigma.core.error.InvalidSessionTokenError;
import com.redbeemedia.enigma.core.error.JsonResponseError;
import com.redbeemedia.enigma.core.error.SessionLimitExceededError;
import com.redbeemedia.enigma.core.error.UnexpectedHttpStatusError;
import com.redbeemedia.enigma.core.error.UnknownBusinessUnitError;
import com.redbeemedia.enigma.core.error.UnknownDeviceIdError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.testutil.HttpToErrorValidator;
import com.redbeemedia.enigma.core.testutil.InstanceOfMatcher;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class LoginResponseHandlerTest {
    private static HttpToErrorValidator errorValidator = new HttpToErrorValidator() {
        /**
         * Add new expected errors here.
         *
         * @param registry
         */
        @Override
        protected void onRegisterErrorExpectation(IExpectationRegistry registry) {
            registry.registerExpectedType(400, "DEVICE_LIMIT_EXCEEDED", DeviceLimitReachedError.class);
            registry.registerExpectedType(400, "SESSION_LIMIT_EXCEEDED", SessionLimitExceededError.class);
            registry.registerExpectedType(400, "UNKNOWN_DEVICE_ID", UnknownDeviceIdError.class);
            registry.registerExpectedType(400, "INVALID_JSON", InvalidJsonToServerError.class);
            registry.registerExpectedType(400, "Other message", UnexpectedHttpStatusError.class);
            registry.registerExpectedType(401, "INVALID_SESSION_TOKEN", InvalidSessionTokenError.class);
            registry.registerExpectedType(401, "INCORRECT_CREDENTIALS", InvalidCredentialsError.class);
            registry.registerExpectedType(401, "Other message", UnexpectedHttpStatusError.class);
            registry.registerExpectedType(404, "Any message", UnknownBusinessUnitError.class);
            registry.registerExpectedType(504, "Any message", UnexpectedHttpStatusError.class);
        }
    };

    @Test
    public void testLoginErrors() throws JSONException {
        for (final HttpToErrorValidator.ErrorExpectation errorExpectation : errorValidator.getErrorExpectations()) {
            final Flag onSuccessCalled = new Flag();
            final Counter errorCounter = new Counter();
            LoginResponseHandler responseHandler = new LoginResponseHandler("cU", "bU", "mockUrl",null, new MockLoginRequest() {
                @Override
                public ILoginResultHandler getResultHandler() {
                    return new ILoginResultHandler() {
                        @Override
                        public void onSuccess(ISession session) {
                            onSuccessCalled.setFlag();
                        }

                        @Override
                        public void onError(Error error) {
                            errorCounter.count();
                            Assert.assertThat(error, errorExpectation.getErrorMatcher());
                        }
                    };
                }
            });
            errorExpectation.respond(new LoginResponseAdapter(responseHandler));
            onSuccessCalled.assertNotSet("onSuccess was not expected to be called");
            errorCounter.assertOnce();
        }
    }

    @Test
    public void testInvalidJsonResponse() {
        final Flag onSuccessCalled = new Flag();
        final Counter errorCounter = new Counter();
        LoginResponseHandler loginResponseHandler = new LoginResponseHandler("cU", "bU", "mockUrl",null, new MockLoginRequest() {
            @Override
            public ILoginResultHandler getResultHandler() {
                return new ILoginResultHandler() {
                    @Override
                    public void onSuccess(ISession session) {
                        onSuccessCalled.setFlag();
                    }

                    @Override
                    public void onError(Error error) {
                        errorCounter.count();
                        Assert.assertThat(error, new InstanceOfMatcher<>(JsonResponseError.class));
                    }
                };
            }
        });
        loginResponseHandler.onResponse(new HttpStatus(400, "Fail!"), new ByteArrayInputStream(new byte[]{23,42}));
        onSuccessCalled.assertNotSet("onSuccess was not expected to be called");
        errorCounter.assertOnce();
    }

    private static class LoginResponseAdapter implements HttpToErrorValidator.IHttpResponseHandler {
        private LoginResponseHandler responseHandler;

        public LoginResponseAdapter(LoginResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }

        @Override
        public void onResponse(HttpStatus status, InputStream inputStream) {
            responseHandler.onResponse(status, inputStream);
        }
    }
}
