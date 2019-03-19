package com.redbeemedia.enigma.core.testutil;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.HttpStatus;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class HttpToErrorValidator {
    private final List<ErrorExpectation> errorExpectations = Collections.unmodifiableList(buildErrorExpectations());

    private List<ErrorExpectation> buildErrorExpectations() {
        final List<ErrorExpectation> errorExpectations = new ArrayList<>();
        onRegisterErrorExpectation(new IExpectationRegistry() {
            @Override
            public void registerExpectedType(int code, String message, Class<? extends Error> errorType) {
                errorExpectations.add(new ErrorExpectation(new EnigmaErrorResponse(code, message), new InstanceOfMatcher(errorType) {
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("(For "+code+" "+message+") ");
                        super.describeTo(description);
                    }
                }));
            }
        });
        return errorExpectations;
    }

    protected abstract void onRegisterErrorExpectation(IExpectationRegistry registry);


    public List<ErrorExpectation> getErrorExpectations() {
        return errorExpectations;
    }


    private static class EnigmaErrorResponse {
        private int code;
        private String message;

        public EnigmaErrorResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
    public static class ErrorExpectation {
        private EnigmaErrorResponse errorResponse;
        private Matcher<Error> errorMatcher;

        public ErrorExpectation(EnigmaErrorResponse errorResponse, Matcher<Error> errorMatcher) {
            this.errorResponse = errorResponse;
            this.errorMatcher = errorMatcher;
        }

        public Matcher<Error> getErrorMatcher() {
            return errorMatcher;
        }

        public void respond(IHttpResponseHandler playResponseHandler) throws JSONException {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("httpCode", errorResponse.code);
            jsonResponse.put("message", errorResponse.message);
            playResponseHandler.onResponse(new HttpStatus(errorResponse.code, errorResponse.message), new ByteArrayInputStream(jsonResponse.toString().getBytes(StandardCharsets.UTF_8)));
        }
    }

    public interface IExpectationRegistry {
        void registerExpectedType(int code, String message, Class<? extends Error> errorType);
    }

    public interface IHttpResponseHandler {
        void onResponse(HttpStatus status, InputStream inputStream);
    }
}
