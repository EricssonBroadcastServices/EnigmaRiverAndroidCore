package com.redbeemedia.enigma.core.asset;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Publication implements Parcelable {

    private String publicationDate;
    private String fromDate;
    private String toDate;
    private List<String> countries;
    private List<String> services;
    private List<String> products;
    private String publicationId;
    private Object customData;

    public Publication() {}

    public String getPublicationDate() {
        return publicationDate;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public List<String> getCountries() {
        return countries;
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getProducts() {
        return products != null ? Collections.unmodifiableList(products) : new ArrayList<String>();
    }

    public String getPublicationId() {
        return publicationId;
    }

    public Object getCustomData() {
        return customData;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public void setProducts(List<String> products) {
        this.products = products;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public void setCustomData(Object customData) {
        this.customData = customData;
    }

    // <editor-fold description="PARCEL IMPLEMENTATION">
    protected Publication(final Parcel in) {
        publicationDate = in.readString();
        fromDate = in.readString();
        toDate = in.readString();
        countries = in.createStringArrayList();
        services = in.createStringArrayList();
        products = in.createStringArrayList();
        publicationId = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(publicationDate);
        dest.writeString(fromDate);
        dest.writeString(toDate);
        dest.writeStringList(countries);
        dest.writeStringList(services);
        dest.writeStringList(products);
        dest.writeString(publicationId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Publication> CREATOR = new Creator<Publication>() {
        @Override
        public Publication createFromParcel(final Parcel in) {
            return new Publication(in);
        }

        @Override
        public Publication[] newArray(final int size) {
            return new Publication[size];
        }
    };
    // </editor-fold>
}
