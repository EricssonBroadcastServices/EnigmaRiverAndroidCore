package com.redbeemedia.enigma.core.exposure.models.user;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiUserAssetData implements Parcelable {
    private ApiUserAssetPlayHistory playHistory;


    private ApiUserAssetData() {}//Private constructor for Parcelable.Creator

    public ApiUserAssetData(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "playHistory":
                    this.playHistory = new ApiUserAssetPlayHistory(jsonReader);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public ApiUserAssetPlayHistory getPlayHistory() {
        return this.playHistory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiUserAssetData> CREATOR = new Parcelable.Creator<ApiUserAssetData>() {
        public ApiUserAssetData createFromParcel(Parcel in) {
            ApiUserAssetData object = new ApiUserAssetData();
            object.playHistory = in.readParcelable(ApiUserAssetPlayHistory.class.getClassLoader());
            return object;
        }

        public ApiUserAssetData[] newArray(int size) {
            return new ApiUserAssetData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(playHistory, flags);
    }
}
