package com.redbeemedia.enigma.core.exposure.models.subtitle;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiSubtitleTrackInfo implements Parcelable {
    private int fileSize;
    private String language;


    private ApiSubtitleTrackInfo() {}//Private constructor for Parcelable.Creator

    public ApiSubtitleTrackInfo(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "fileSize":
                    this.fileSize = jsonReader.nextInt();
                    break;
                case "language":
                    this.language = jsonReader.nextString();
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

    public String getLanguage() {
        return this.language;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiSubtitleTrackInfo> CREATOR = new Parcelable.Creator<ApiSubtitleTrackInfo>() {
        public ApiSubtitleTrackInfo createFromParcel(Parcel in) {
            ApiSubtitleTrackInfo object = new ApiSubtitleTrackInfo();
            object.fileSize = in.readInt();
            object.language = in.readString();
            return object;
        }

        public ApiSubtitleTrackInfo[] newArray(int size) {
            return new ApiSubtitleTrackInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fileSize);
        dest.writeString(language);
    }
}
