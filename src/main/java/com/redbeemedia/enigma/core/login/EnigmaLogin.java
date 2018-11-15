package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.IHttpPreparator;
import com.redbeemedia.enigma.core.util.UrlPath;

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
        httpHandler.post(loginUrl, new IHttpPreparator() {
            @Override
            public void prepare(IHttpConnection connection) {
                //loginRequest.prepare(connection);
                //TODO add common stuff
                //TODO set timeout params etc
                connection.setHeader("Content-Type", "application/json");
                connection.setHeader("Accept", "application/json");
            }

            @Override
            public void writeBodyTo(OutputStream outputStream) {

            }
        }, new IHttpHandler.IHttpHandlerResponse() {
            @Override
            public void onResponse(int code, InputStream inputStream) {
                //TODO don't do this
                //TODO check if we can construct a session and if that is the case, call loginRequest.onSuccess
            }
        });
    }

    /*package-protected*/ UrlPath getBusinessUnitBaseUrl(UrlPath baseUrl) throws MalformedURLException {
        return baseUrl.append("v1/customer").append(customerUnit).append("businessunit").append(businessUnit);
    }
}
