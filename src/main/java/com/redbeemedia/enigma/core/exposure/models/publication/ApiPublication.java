package com.redbeemedia.enigma.core.exposure.models.publication;

import com.redbeemedia.enigma.core.exposure.models.device.ApiDeviceRights;
import com.redbeemedia.enigma.core.exposure.models.asset.ApiAssetRights;
import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import java.io.IOException;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiPublication implements Parcelable {
    private String fromDate;
    private List<ApiDeviceRights> devices;
    private ApiAssetRights rights;
    private String toDate;
    private List<String> countries;
    private List<String> services;
    private String publicationId;
    private String publicationDate;
    private List<String> products;


    private ApiPublication() {}//Private constructor for Parcelable.Creator

    public ApiPublication(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "fromDate":
                    this.fromDate = jsonReader.nextString();
                    break;
                case "devices":
                    this.devices = JsonReaderUtil.readArray(jsonReader, ApiDeviceRights.class);
                    break;
                case "rights":
                    this.rights = new ApiAssetRights(jsonReader);
                    break;
                case "toDate":
                    this.toDate = jsonReader.nextString();
                    break;
                case "countries":
                    this.countries = JsonReaderUtil.readArray(jsonReader, String.class);
                    break;
                case "services":
                    this.services = JsonReaderUtil.readArray(jsonReader, String.class);
                    break;
                case "publicationId":
                    this.publicationId = jsonReader.nextString();
                    break;
                case "publicationDate":
                    this.publicationDate = jsonReader.nextString();
                    break;
                case "products":
                    this.products = JsonReaderUtil.readArray(jsonReader, String.class);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getFromDate() {
        return this.fromDate;
    }

    public List<ApiDeviceRights> getDevices() {
        return this.devices;
    }

    public ApiAssetRights getRights() {
        return this.rights;
    }

    public String getToDate() {
        return this.toDate;
    }

    public List<String> getCountries() {
        return this.countries;
    }

    public List<String> getServices() {
        return this.services;
    }

    public String getPublicationId() {
        return this.publicationId;
    }

    public String getPublicationDate() {
        return this.publicationDate;
    }

    public List<String> getProducts() {
        return this.products;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiPublication> CREATOR = new Parcelable.Creator<ApiPublication>() {
        public ApiPublication createFromParcel(Parcel in) {
            ApiPublication object = new ApiPublication();
            object.fromDate = in.readString();
            object.devices = in.createTypedArrayList(ApiDeviceRights.CREATOR);
            object.rights = in.readParcelable(ApiAssetRights.class.getClassLoader());
            object.toDate = in.readString();
            object.countries = in.createStringArrayList();
            object.services = in.createStringArrayList();
            object.publicationId = in.readString();
            object.publicationDate = in.readString();
            object.products = in.createStringArrayList();
            return object;
        }

        public ApiPublication[] newArray(int size) {
            return new ApiPublication[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fromDate);
        dest.writeTypedList(devices);
        dest.writeParcelable(rights, flags);
        dest.writeString(toDate);
        dest.writeStringList(countries);
        dest.writeStringList(services);
        dest.writeString(publicationId);
        dest.writeString(publicationDate);
        dest.writeStringList(products);
    }
}
