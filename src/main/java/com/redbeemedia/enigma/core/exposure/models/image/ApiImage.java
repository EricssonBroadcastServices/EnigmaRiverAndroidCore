package com.redbeemedia.enigma.core.exposure.models.image;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiImage implements Parcelable {
    private String orientation;
    private long width;
    private String type;
    private String url;
    private long height;


    protected ApiImage() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiImage(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "orientation":
                    this.orientation = jsonReader.nextString();
                    break;
                case "width":
                    this.width = jsonReader.nextLong();
                    break;
                case "type":
                    this.type = jsonReader.nextString();
                    break;
                case "url":
                    this.url = jsonReader.nextString();
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


    public String getOrientation() {
        return this.orientation;
    }

    public long getWidth() {
        return this.width;
    }

    public String getType() {
        return this.type;
    }

    public String getUrl() {
        return this.url;
    }

    public long getHeight() {
        return this.height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiImage> CREATOR = new Parcelable.Creator<ApiImage>() {
        public ApiImage createFromParcel(Parcel in) {
            ApiImage object = new ApiImage();
            object.orientation = in.readString();
            object.width = in.readLong();
            object.type = in.readString();
            object.url = in.readString();
            object.height = in.readLong();
            return object;
        }

        public ApiImage[] newArray(int size) {
            return new ApiImage[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orientation);
        dest.writeLong(width);
        dest.writeString(type);
        dest.writeString(url);
        dest.writeLong(height);
    }
}
