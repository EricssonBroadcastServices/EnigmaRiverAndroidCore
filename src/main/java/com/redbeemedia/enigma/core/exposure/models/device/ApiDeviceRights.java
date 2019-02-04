package com.redbeemedia.enigma.core.exposure.models.device;

import com.redbeemedia.enigma.core.exposure.models.asset.ApiAssetRights;
import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiDeviceRights implements Parcelable {
    private String os;
    private String osVersion;
    private ApiAssetRights rights;
    private String model;
    private String type;
    private String manufacturer;


    protected ApiDeviceRights() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiDeviceRights(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "os":
                    this.os = jsonReader.nextString();
                    break;
                case "osVersion":
                    this.osVersion = jsonReader.nextString();
                    break;
                case "rights":
                    this.rights = new ApiAssetRights(jsonReader);
                    break;
                case "model":
                    this.model = jsonReader.nextString();
                    break;
                case "type":
                    this.type = jsonReader.nextString();
                    break;
                case "manufacturer":
                    this.manufacturer = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getOs() {
        return this.os;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    public ApiAssetRights getRights() {
        return this.rights;
    }

    public String getModel() {
        return this.model;
    }

    public String getType() {
        return this.type;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiDeviceRights> CREATOR = new Parcelable.Creator<ApiDeviceRights>() {
        public ApiDeviceRights createFromParcel(Parcel in) {
            ApiDeviceRights object = new ApiDeviceRights();
            object.os = in.readString();
            object.osVersion = in.readString();
            object.rights = in.readParcelable(ApiAssetRights.class.getClassLoader());
            object.model = in.readString();
            object.type = in.readString();
            object.manufacturer = in.readString();
            return object;
        }

        public ApiDeviceRights[] newArray(int size) {
            return new ApiDeviceRights[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(os);
        dest.writeString(osVersion);
        dest.writeParcelable(rights, flags);
        dest.writeString(model);
        dest.writeString(type);
        dest.writeString(manufacturer);
    }
}
