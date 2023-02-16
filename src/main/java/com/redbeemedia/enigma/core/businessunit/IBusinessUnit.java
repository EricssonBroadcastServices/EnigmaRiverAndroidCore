package com.redbeemedia.enigma.core.businessunit;

import android.os.Parcelable;

import com.redbeemedia.enigma.core.util.UrlPath;

public interface IBusinessUnit extends Parcelable {
    String getCustomerName();
    String getName();
    UrlPath getApiBaseUrl();
    UrlPath getApiBaseUrl(String apiVersion);
    UrlPath createAnalyticsUrl(String analyticsUrl);
}
