package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;
import com.redbeemedia.enigma.core.session.ISession;
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
        public void onResponse(int code, InputStream inputStream) {
            //TODO check if we can construct a session and if that is the case, call loginRequest.onSuccess

            ILoginResultHandler resultHandler = loginRequest.getResultHandler();

            if(code != HttpsURLConnection.HTTP_OK) {
                throw new RuntimeException("httpcode: "+code);
//                resultHandler.onError(Error.TODO);
//                return;
            }

            try {
                //TODO reuse parser instance
                JSONObject response = new JsonInputStreamParser().parse(inputStream);
                //TODO parse data for creating a session
                String sessionToken = response.getString("sessionToken");

                //TODO then create a session object and feed it to resultHandler
                ISession session = new TempSessionImpl(sessionToken);
                resultHandler.onSuccess(session);
            } catch (JSONException e) {
                throw new RuntimeException(e);
//                //TODO log error?
//                //TODO provide way to actually get the exception that caused this in the result handler?
//                resultHandler.onError(Error.FAILED_TO_PARSE_RESPONSE_JSON);
            }
        }

        @Override
        public void onResponse(int code) {
            ILoginResultHandler resultHandler = loginRequest.getResultHandler();
            resultHandler.onError(Error.EMPTY_RESPONSE);
        }
    }

    //TODO refactor out this class
    private class TempSessionImpl implements ISession {
        private String sessionToken;

        public TempSessionImpl(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        @Override
        public String getSessionToken() {
            return sessionToken;
        }

        @Override
        public String getCustomerUnitName() {
            return customerUnit;
        }

        @Override
        public String getBusinessUnitName() {
            return businessUnit;
        }
    }
}
