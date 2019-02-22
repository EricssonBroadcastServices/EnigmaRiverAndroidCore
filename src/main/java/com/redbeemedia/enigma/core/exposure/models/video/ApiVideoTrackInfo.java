package com.redbeemedia.enigma.core.exposure.models.video;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiVideoTrackInfo implements Parcelable {
    private long fileSize;
    private String targetBitrate;
    private long height;


    protected ApiVideoTrackInfo() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiVideoTrackInfo(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "fileSize":
                    this.fileSize = jsonReader.nextLong();
                    break;
                case "targetBitrate":
                    this.targetBitrate = jsonReader.nextString();
                    break;
                case "height":
                    this.height = jsonReader.nextLong();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public long getFileSize() {
        return this.fileSize;
    }

    public String getTargetBitrate() {
        return this.targetBitrate;
    }

    public long getHeight() {
        return this.height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiVideoTrackInfo> CREATOR = new Parcelable.Creator<ApiVideoTrackInfo>() {
        public ApiVideoTrackInfo createFromParcel(Parcel in) {
            ApiVideoTrackInfo object = new ApiVideoTrackInfo();
            object.fileSize = in.readLong();
            object.targetBitrate = in.readString();
            object.height = in.readLong();
            return object;
        }

        public ApiVideoTrackInfo[] newArray(int size) {
            return new ApiVideoTrackInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(fileSize);
        dest.writeString(targetBitrate);
        dest.writeLong(height);
    }
}
