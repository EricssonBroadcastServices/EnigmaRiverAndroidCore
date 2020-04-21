package com.redbeemedia.enigma.core.login;

import android.os.Handler;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;

import java.net.MalformedURLException;
import java.net.URL;

public class EnigmaLogin {
    private IBusinessUnit businessUnit;
    private IHandler callbackHandler = null;

    public EnigmaLogin(String customerName, String businessUnitName) {
        this(new BusinessUnit(customerName, businessUnitName));
    }

    public EnigmaLogin(IBusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public EnigmaLogin setCallbackHandler(IHandler handler) {
        this.callbackHandler = handler;
        return this;
    }

    public EnigmaLogin setCallbackHandler(Handler handler) {
        return this.setCallbackHandler(new HandlerWrapper(handler));
    }


    public void login(final ILoginRequest loginRequest) {
        URL loginUrl;
        try {
            loginUrl = loginRequest.getTargetUrl(businessUnit).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        IHttpHandler httpHandler = EnigmaRiverContext.getHttpHandler();
        httpHandler.doHttp(loginUrl, loginRequest, new LoginResponseHandler(businessUnit.getCustomerName(), businessUnit.getName(), loginUrl.toString(), callbackHandler, loginRequest));
    }
}