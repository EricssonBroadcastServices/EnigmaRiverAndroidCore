package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpPreparator;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
        httpHandler.doHttp(loginUrl, new IHttpPreparator() {
            @Override
            public void prepare(IHttpConnection connection) {
                //loginRequest.prepare(connection);
                //TODO add common stuff
                //TODO set timeout params etc
                connection.setHeader("Content-Type", "application/json");
                connection.setHeader("Accept", "application/json");
            }

            @Override
            public String getRequestMethod() {
                return "POST";
            }

            @Override
            public void writeBodyTo(OutputStream outputStream) {

            }
        }, new ApiLoginResponseHandler(loginRequest));
    }

    /*package-protected*/ UrlPath getBusinessUnitBaseUrl(UrlPath baseUrl) throws MalformedURLException {
        return baseUrl.append("v1/customer").append(customerUnit).append("businessunit").append(businessUnit);
    }

    /*package-protected*/ ISession buildSession(JSONObject jsonObject) {
        //TODO maybe have this method.
        return null;
    }

    private static class ApiLoginResponseHandler implements IHttpHandler.IHttpResponseHandler {
        private ILoginRequest loginRequest;

        public ApiLoginResponseHandler(ILoginRequest loginRequest) {
            this.loginRequest = loginRequest;
        }

        @Override
        public void onResponse(int code, InputStream inputStream) {
            //TODO check if we can construct a session and if that is the case, call loginRequest.onSuccess

            ILoginResultHandler resultHandler = loginRequest.getResultHandler();

            try {
                JSONObject response = new JsonInputStreamParser().parse(inputStream); //TODO reuse parser instance
                //TODO parse data for creating a session
                //TODO then create a session object and feed it to resultHandler
            } catch (JSONException e) {
                //TODO log error?
                resultHandler.onError(Error.FAILED_TO_PARSE_RESPONSE_JSON);
            }
        }

        @Override
        public void onResponse(int code) {
            //TODO error
        }
    }
}
