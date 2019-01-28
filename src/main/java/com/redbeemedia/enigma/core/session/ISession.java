package com.redbeemedia.enigma.core.session;

import android.os.Parcelable;

import com.redbeemedia.enigma.core.util.UrlPath;

public interface ISession extends Parcelable {
    String getSessionToken();
    String getCustomerUnitName();
    String getBusinessUnitName();
    UrlPath getApiBaseUrl();
    UrlPath getApiBaseUrl(String apiVersion);
}
