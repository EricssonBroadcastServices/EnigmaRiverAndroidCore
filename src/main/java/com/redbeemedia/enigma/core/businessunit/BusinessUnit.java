package com.redbeemedia.enigma.core.businessunit;

import android.os.Parcel;
import android.os.Parcelable;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.util.Objects;

public class BusinessUnit implements IBusinessUnit {

    private final String customerUnitName;
    private final String businessUnitName;

    public BusinessUnit(String customerUnitName, String businessUnitName) {
        this.customerUnitName = customerUnitName;
        this.businessUnitName = businessUnitName;
    }

    private BusinessUnit(Parcel parcel) {
        this.customerUnitName = parcel.readString();
        this.businessUnitName = parcel.readString();
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BusinessUnit && equalsBusinessUnit((BusinessUnit) obj);
    }

    private boolean equalsBusinessUnit(BusinessUnit businessUnit) {
        return Objects.equals(this.customerUnitName, businessUnit.customerUnitName)
                && Objects.equals(this.businessUnitName, businessUnit.businessUnitName);
    }

    @Override
    public int hashCode() {
        return (customerUnitName != null ? customerUnitName.hashCode()*31 : 0)+(businessUnitName != null ? businessUnitName.hashCode() : 0);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public BusinessUnit createFromParcel(Parcel in) {
            return new BusinessUnit(in);
        }

        public BusinessUnit[] newArray(int size) {
            return new BusinessUnit[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(customerUnitName);
        dest.writeString(businessUnitName);
    }
}
