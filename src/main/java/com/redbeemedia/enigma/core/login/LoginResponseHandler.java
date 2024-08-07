// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.error.DeviceLimitReachedError;
import com.redbeemedia.enigma.core.error.EmptyResponseError;
import com.redbeemedia.enigma.core.error.HttpResourceNotFoundError;
import com.redbeemedia.enigma.core.error.InvalidCredentialsError;
import com.redbeemedia.enigma.core.error.InvalidJsonToServerError;
import com.redbeemedia.enigma.core.error.InvalidSessionTokenError;
import com.redbeemedia.enigma.core.error.JsonResponseError;
import com.redbeemedia.enigma.core.error.MigratedUserError;
import com.redbeemedia.enigma.core.error.SessionLimitExceededError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.error.UnexpectedHttpStatusError;
import com.redbeemedia.enigma.core.error.UnknownBusinessUnitError;
import com.redbeemedia.enigma.core.error.UnknownDeviceIdError;
import com.redbeemedia.enigma.core.http.ExposureHttpError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.Session;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.ProxyCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import javax.net.ssl.HttpsURLConnection;

/*package-protected*/ class LoginResponseHandler implements IHttpHandler.IHttpResponseHandler {
    private String customerUnit;
    private String businessUnit;
    private String requestUrl;
    private IHandler callbackHandler;
    private ILoginRequest loginRequest;

    /*package-protected*/ LoginResponseHandler(String customerUnit, String businessUnit, String requestUrl, IHandler callbackHandler, ILoginRequest loginRequest) {
        this.customerUnit = customerUnit;
        this.businessUnit = businessUnit;
        this.requestUrl = requestUrl;
        this.callbackHandler = callbackHandler;
        this.loginRequest = loginRequest;
    }

    @Override
    public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
        ILoginResultHandler resultHandler = getResultHandler();

        try {
            JSONObject response = JsonInputStreamParser.obtain().parse(inputStream);
            if (ExposureHttpError.isError(httpStatus.getResponseCode())) {
                ExposureHttpError httpError = new ExposureHttpError(response);
                if (httpError.getHttpCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                    if (httpError.getMessage().equals("DEVICE_LIMIT_EXCEEDED")) {
                        resultHandler.onError(new DeviceLimitReachedError());
                    } else if (httpError.getMessage().equals("SESSION_LIMIT_EXCEEDED")) {
                        resultHandler.onError(new SessionLimitExceededError());
                    } else if (httpError.getMessage().equals("UNKNOWN_DEVICE_ID")) {
                        resultHandler.onError(new UnknownDeviceIdError());
                    } else if (httpError.getMessage().equals("INVALID_JSON")) {
                        resultHandler.onError(new InvalidJsonToServerError());
                    } else {
                        resultHandler.onError(new UnexpectedHttpStatusError(new HttpStatus(httpError.getHttpCode(), httpError.getMessage())));
                    }
                } else if (httpError.getHttpCode() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                    if (httpError.getMessage().equals("INVALID_SESSION_TOKEN") || httpError.getMessage().equals("NO_SESSION_TOKEN")) {
                        resultHandler.onError(new InvalidSessionTokenError());
                    } else if (httpError.getMessage().equals("INCORRECT_CREDENTIALS")) {
                        resultHandler.onError(new InvalidCredentialsError());
                    } else if (httpError.getMessage().equals("MIGRATED_USER")) {
                        resultHandler.onError(new MigratedUserError());
                    } else {
                        resultHandler.onError(new UnexpectedHttpStatusError(new HttpStatus(httpError.getHttpCode(), httpError.getMessage())));
                    }
                } else if (httpError.getHttpCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                    resultHandler.onError(new UnknownBusinessUnitError());
                } else if (httpError.getHttpCode() == 422) {
                    resultHandler.onError(new InvalidJsonToServerError("Server responded "+httpError));
                } else {
                    resultHandler.onError(new UnexpectedHttpStatusError(new HttpStatus(httpError.getHttpCode(), httpError.getMessage())));
                }
            } else if (httpStatus.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                String userId = response.optString("userId");
                if (loginRequest instanceof ResumeLoginRequest) {
                    ISession session = new Session(((ResumeLoginRequest) loginRequest).getSessionToken(), new BusinessUnit(customerUnit, businessUnit),userId);
                    resultHandler.onSuccess(session);
                } else {
                    String sessionToken = response.getString("sessionToken");
                    ISession session = new Session(sessionToken, new BusinessUnit(customerUnit, businessUnit),userId);
                    resultHandler.onSuccess(session);
                }
            } else {
                resultHandler.onError(new UnexpectedHttpStatusError(httpStatus));
            }
        } catch (JSONException e) {
            if(httpStatus.getResponseCode() == 404) {
                resultHandler.onError(new HttpResourceNotFoundError(requestUrl, new UnexpectedError(e)));
            } else {
                resultHandler.onError(new JsonResponseError("Failed to parse login response.", new UnexpectedError(e)));
            }
        }
    }

    @Override
    public void onResponse(HttpStatus status) {
        ILoginResultHandler resultHandler = getResultHandler();
        resultHandler.onError(new EmptyResponseError());
    }

    @Override
    public void onException(Exception e) {
        ILoginResultHandler resultHandler = getResultHandler();
        resultHandler.onError(new UnexpectedError(e));
    }

    private ILoginResultHandler getResultHandler() {
        if(callbackHandler != null) {
            return ProxyCallback.createCallbackOnThread(callbackHandler, ILoginResultHandler.class, loginRequest.getResultHandler());
        } else {
            return loginRequest.getResultHandler();
        }
    }
}