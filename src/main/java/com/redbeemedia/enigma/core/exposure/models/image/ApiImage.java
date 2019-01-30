package com.redbeemedia.enigma.core.exposure.models.image;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiImage implements Parcelable {
    private String orientation;
    private int width;
    private String type;
    private String url;
    private int height;


    private ApiImage() {}//Private constructor for Parcelable.Creator

    public ApiImage(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "orientation":
                    this.orientation = jsonReader.nextString();
                    break;
                case "width":
                    this.width = jsonReader.nextInt();
                    break;
                case "type":
                    this.type = jsonReader.nextString();
                    break;
                case "url":
                    this.url = jsonReader.nextString();
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


    public String getOrientation() {
        return this.orientation;
    }

    public int getWidth() {
        return this.width;
    }

    public String getType() {
        return this.type;
    }

    public String getUrl() {
        return this.url;
    }

    public int getHeight() {
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
            object.width = in.readInt();
            object.type = in.readString();
            object.url = in.readString();
            object.height = in.readInt();
            return object;
        }

        public ApiImage[] newArray(int size) {
            return new ApiImage[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orientation);
        dest.writeInt(width);
        dest.writeString(type);
        dest.writeString(url);
        dest.writeInt(height);
    }
}
