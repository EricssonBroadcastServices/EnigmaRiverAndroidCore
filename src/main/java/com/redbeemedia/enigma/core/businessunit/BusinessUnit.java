package com.redbeemedia.enigma.core.businessunit;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.util.UrlPath;

public class BusinessUnit implements IBusinessUnit {
    private final String customerUnitName;
    private final String businessUnitName;

    public BusinessUnit(String customerUnitName, String businessUnitName) {
        this.customerUnitName = customerUnitName;
        this.businessUnitName = businessUnitName;
    }

    @Override
    public String getCustomerName() {
        return customerUnitName;
    }

    @Override
    public String getName() {
        return businessUnitName;
    }

    @Override
    public UrlPath getApiBaseUrl() {
        return getApiBaseUrl("v1");
    }

    @Override
    public UrlPath getApiBaseUrl(String apiVersion) {
        return EnigmaRiverContext.getExposureBaseUrl().append(apiVersion).append("customer").append(customerUnitName).append("businessunit").append(businessUnitName);
    }
}
