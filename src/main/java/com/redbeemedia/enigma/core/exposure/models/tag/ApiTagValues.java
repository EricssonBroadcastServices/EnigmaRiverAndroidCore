package com.redbeemedia.enigma.core.exposure.models.tag;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiTagValues implements Parcelable {
    private String tagId;


    private ApiTagValues() {}//Private constructor for Parcelable.Creator

    public ApiTagValues(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "tagId":
                    this.tagId = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getTagId() {
        return this.tagId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiTagValues> CREATOR = new Parcelable.Creator<ApiTagValues>() {
        public ApiTagValues createFromParcel(Parcel in) {
            ApiTagValues object = new ApiTagValues();
            object.tagId = in.readString();
            return object;
        }

        public ApiTagValues[] newArray(int size) {
            return new ApiTagValues[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tagId);
    }
}
