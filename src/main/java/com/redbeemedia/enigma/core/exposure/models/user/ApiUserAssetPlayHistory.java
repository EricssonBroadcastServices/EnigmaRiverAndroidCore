package com.redbeemedia.enigma.core.exposure.models.user;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiUserAssetPlayHistory implements Parcelable {
    private int lastViewedTime;
    private String errorMessage;
    private String channelId;
    private int lastViewedOffset;
    private String programId;


    private ApiUserAssetPlayHistory() {}//Private constructor for Parcelable.Creator

    public ApiUserAssetPlayHistory(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "lastViewedTime":
                    this.lastViewedTime = jsonReader.nextInt();
                    break;
                case "errorMessage":
                    this.errorMessage = jsonReader.nextString();
                    break;
                case "channelId":
                    this.channelId = jsonReader.nextString();
                    break;
                case "lastViewedOffset":
                    this.lastViewedOffset = jsonReader.nextInt();
                    break;
                case "programId":
                    this.programId = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public int getLastViewedTime() {
        return this.lastViewedTime;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public int getLastViewedOffset() {
        return this.lastViewedOffset;
    }

    public String getProgramId() {
        return this.programId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiUserAssetPlayHistory> CREATOR = new Parcelable.Creator<ApiUserAssetPlayHistory>() {
        public ApiUserAssetPlayHistory createFromParcel(Parcel in) {
            ApiUserAssetPlayHistory object = new ApiUserAssetPlayHistory();
            object.lastViewedTime = in.readInt();
            object.errorMessage = in.readString();
            object.channelId = in.readString();
            object.lastViewedOffset = in.readInt();
            object.programId = in.readString();
            return object;
        }

        public ApiUserAssetPlayHistory[] newArray(int size) {
            return new ApiUserAssetPlayHistory[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(lastViewedTime);
        dest.writeString(errorMessage);
        dest.writeString(channelId);
        dest.writeInt(lastViewedOffset);
        dest.writeString(programId);
    }
}
