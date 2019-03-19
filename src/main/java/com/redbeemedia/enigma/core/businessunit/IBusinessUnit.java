package com.redbeemedia.enigma.core.businessunit;

import com.redbeemedia.enigma.core.util.UrlPath;

public interface IBusinessUnit {
    String getCustomerName();
    String getName();
    UrlPath getApiBaseUrl();
    UrlPath getApiBaseUrl(String apiVersion);
}
