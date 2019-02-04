package com.redbeemedia.enigma.core.exposure.models.external;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiExternalReference implements Parcelable {
    private String type;
    private String locator;
    private String value;


    protected ApiExternalReference() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiExternalReference(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "type":
                    this.type = jsonReader.nextString();
                    break;
                case "locator":
                    this.locator = jsonReader.nextString();
                    break;
                case "value":
                    this.value = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getType() {
        return this.type;
    }

    public String getLocator() {
        return this.locator;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiExternalReference> CREATOR = new Parcelable.Creator<ApiExternalReference>() {
        public ApiExternalReference createFromParcel(Parcel in) {
            ApiExternalReference object = new ApiExternalReference();
            object.type = in.readString();
            object.locator = in.readString();
            object.value = in.readString();
            return object;
        }

        public ApiExternalReference[] newArray(int size) {
            return new ApiExternalReference[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(locator);
        dest.writeString(value);
    }
}
