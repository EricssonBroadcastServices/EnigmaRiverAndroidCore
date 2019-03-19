package com.redbeemedia.enigma.core.exposure.models.user;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiUserAssetPlayHistory implements Parcelable {
    private long lastViewedTime;
    private String errorMessage;
    private String channelId;
    private long lastViewedOffset;
    private String programId;


    protected ApiUserAssetPlayHistory() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiUserAssetPlayHistory(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "lastViewedTime":
                    this.lastViewedTime = jsonReader.nextLong();
                    break;
                case "errorMessage":
                    this.errorMessage = jsonReader.nextString();
                    break;
                case "channelId":
                    this.channelId = jsonReader.nextString();
                    break;
                case "lastViewedOffset":
                    this.lastViewedOffset = jsonReader.nextLong();
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


    public long getLastViewedTime() {
        return this.lastViewedTime;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public long getLastViewedOffset() {
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
            object.lastViewedTime = in.readLong();
            object.errorMessage = in.readString();
            object.channelId = in.readString();
            object.lastViewedOffset = in.readLong();
            object.programId = in.readString();
            return object;
        }

        public ApiUserAssetPlayHistory[] newArray(int size) {
            return new ApiUserAssetPlayHistory[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(lastViewedTime);
        dest.writeString(errorMessage);
        dest.writeString(channelId);
        dest.writeLong(lastViewedOffset);
        dest.writeString(programId);
    }
}
