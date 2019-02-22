package com.redbeemedia.enigma.core.exposure.models.media;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiMedia implements Parcelable {
    private String format;
    private String name;
    private long width;
    private long durationMillis;
    private String mediaId;
    private String drm;
    private String programId;
    private long height;
    private String status;


    protected ApiMedia() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiMedia(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "format":
                    this.format = jsonReader.nextString();
                    break;
                case "name":
                    this.name = jsonReader.nextString();
                    break;
                case "width":
                    this.width = jsonReader.nextLong();
                    break;
                case "durationMillis":
                    this.durationMillis = jsonReader.nextLong();
                    break;
                case "mediaId":
                    this.mediaId = jsonReader.nextString();
                    break;
                case "drm":
                    this.drm = jsonReader.nextString();
                    break;
                case "programId":
                    this.programId = jsonReader.nextString();
                    break;
                case "height":
                    this.height = jsonReader.nextLong();
                    break;
                case "status":
                    this.status = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getFormat() {
        return this.format;
    }

    public String getName() {
        return this.name;
    }

    public long getWidth() {
        return this.width;
    }

    public long getDurationMillis() {
        return this.durationMillis;
    }

    public String getMediaId() {
        return this.mediaId;
    }

    public String getDrm() {
        return this.drm;
    }

    public String getProgramId() {
        return this.programId;
    }

    public long getHeight() {
        return this.height;
    }

    public String getStatus() {
        return this.status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiMedia> CREATOR = new Parcelable.Creator<ApiMedia>() {
        public ApiMedia createFromParcel(Parcel in) {
            ApiMedia object = new ApiMedia();
            object.format = in.readString();
            object.name = in.readString();
            object.width = in.readLong();
            object.durationMillis = in.readLong();
            object.mediaId = in.readString();
            object.drm = in.readString();
            object.programId = in.readString();
            object.height = in.readLong();
            object.status = in.readString();
            return object;
        }

        public ApiMedia[] newArray(int size) {
            return new ApiMedia[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(format);
        dest.writeString(name);
        dest.writeLong(width);
        dest.writeLong(durationMillis);
        dest.writeString(mediaId);
        dest.writeString(drm);
        dest.writeString(programId);
        dest.writeLong(height);
        dest.writeString(status);
    }
}
