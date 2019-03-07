package com.redbeemedia.enigma.core.exposure.models.localized;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiLocalizedTitle implements Parcelable {
    private String locale;
    private String title;


    protected ApiLocalizedTitle() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiLocalizedTitle(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "locale":
                    this.locale = jsonReader.nextString();
                    break;
                case "title":
                    this.title = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getLocale() {
        return this.locale;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiLocalizedTitle> CREATOR = new Parcelable.Creator<ApiLocalizedTitle>() {
        public ApiLocalizedTitle createFromParcel(Parcel in) {
            ApiLocalizedTitle object = new ApiLocalizedTitle();
            object.locale = in.readString();
            object.title = in.readString();
            return object;
        }

        public ApiLocalizedTitle[] newArray(int size) {
            return new ApiLocalizedTitle[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(locale);
        dest.writeString(title);
    }
}
