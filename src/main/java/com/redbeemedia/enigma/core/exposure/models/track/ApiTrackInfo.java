package com.redbeemedia.enigma.core.exposure.models.track;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiTrackInfo implements Parcelable {
    private long fileSize;
    private String targetBitrate;


    protected ApiTrackInfo() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiTrackInfo(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "fileSize":
                    this.fileSize = jsonReader.nextLong();
                    break;
                case "targetBitrate":
                    this.targetBitrate = jsonReader.nextString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiTrackInfo> CREATOR = new Parcelable.Creator<ApiTrackInfo>() {
        public ApiTrackInfo createFromParcel(Parcel in) {
            ApiTrackInfo object = new ApiTrackInfo();
            object.fileSize = in.readLong();
            object.targetBitrate = in.readString();
            return object;
        }

        public ApiTrackInfo[] newArray(int size) {
            return new ApiTrackInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(fileSize);
        dest.writeString(targetBitrate);
    }
}
