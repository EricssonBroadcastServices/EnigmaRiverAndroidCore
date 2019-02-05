package com.redbeemedia.enigma.core.exposure.models.video;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiVideoTrackInfo implements Parcelable {
    private int fileSize;
    private String targetBitrate;
    private int height;


    protected ApiVideoTrackInfo() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiVideoTrackInfo(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "fileSize":
                    this.fileSize = jsonReader.nextInt();
                    break;
                case "targetBitrate":
                    this.targetBitrate = jsonReader.nextString();
                    break;
                case "height":
                    this.height = jsonReader.nextInt();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public int getFileSize() {
        return this.fileSize;
    }

    public String getTargetBitrate() {
        return this.targetBitrate;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiVideoTrackInfo> CREATOR = new Parcelable.Creator<ApiVideoTrackInfo>() {
        public ApiVideoTrackInfo createFromParcel(Parcel in) {
            ApiVideoTrackInfo object = new ApiVideoTrackInfo();
            object.fileSize = in.readInt();
            object.targetBitrate = in.readString();
            object.height = in.readInt();
            return object;
        }

        public ApiVideoTrackInfo[] newArray(int size) {
            return new ApiVideoTrackInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fileSize);
        dest.writeString(targetBitrate);
        dest.writeInt(height);
    }
}
