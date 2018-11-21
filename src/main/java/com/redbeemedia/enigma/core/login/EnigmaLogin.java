package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.error.ExposureHttpError;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.Session;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class EnigmaLogin {
    private String customerUnit;
    private String businessUnit;

    public EnigmaLogin(String customerUnit, String businessUnit) {
        this.customerUnit = customerUnit;
        this.businessUnit = businessUnit;
    }


    public void login(final ILoginRequest loginRequest) throws MalformedURLException {
        UrlPath url = getBusinessUnitBaseUrl(EnigmaRiverContext.getExposureBaseUrl());

        URL loginUrl = loginRequest.getTargetUrl(url).toURL();

        IHttpHandler httpHandler = EnigmaRiverContext.getHttpHandler();
        httpHandler.doHttp(loginUrl, loginRequest, new ApiLoginResponseHandler(loginRequest));
    }

    /*package-protected*/ UrlPath getBusinessUnitBaseUrl(UrlPath baseUrl) throws MalformedURLException {
        return baseUrl.append("v1/customer").append(customerUnit).append("businessunit").append(businessUnit);
    }

    //TODO Try to break out this and add unit tests. (If we don't do any logic (like parsing) here then don't refactor out)
    private class ApiLoginResponseHandler implements IHttpHandler.IHttpResponseHandler {
        private ILoginRequest loginRequest;

        public ApiLoginResponseHandler(ILoginRequest loginRequest) {
            this.loginRequest = loginRequest;
        }

        @Override
        public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
            //TODO check if we can construct a session and if that is the case, call loginRequest.onSuccess

            ILoginResultHandler resultHandler = loginRequest.getResultHandler();

            try {
                JSONObject response = JsonInputStreamParser.obtain().parse(inputStream);
                if (ExposureHttpError.isError(httpStatus.code)) {
                    ExposureHttpError httpError = ExposureHttpError.getHttpError(response);
                    if (httpError.getHttpCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {

                    } else if (httpError.getHttpCode() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                        resultHandler.onError(Error.INCORRECT_CREDENTIALS);
                    } else if (httpError.getHttpCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                        resultHandler.onError(Error.UNKNOWN_ASSET);
                    }
                } else if (httpStatus.code == HttpsURLConnection.HTTP_OK) {
                    String sessionToken = response.getString("sessionToken");
                    ISession session = new Session(sessionToken, customerUnit, businessUnit);
                    resultHandler.onSuccess(session);
                } else {
                    resultHandler.onError(Error.NETWORK_ERROR);
                }
            } catch (JSONException e) {
                resultHandler.onError(Error.FAILED_TO_PARSE_RESPONSE_JSON);
            }
        }

        @Override
        public void onResponse(HttpStatus status) {
            ILoginResultHandler resultHandler = loginRequest.getResultHandler();
            resultHandler.onError(Error.EMPTY_RESPONSE);
        }

        @Override
        public void onException(Exception e) {
            //TODO: handle exception
        }
    }
}
