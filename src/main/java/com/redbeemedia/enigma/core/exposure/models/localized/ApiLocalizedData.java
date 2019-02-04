package com.redbeemedia.enigma.core.exposure.models.localized;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import com.redbeemedia.enigma.core.exposure.models.image.ApiImage;
import java.io.IOException;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiLocalizedData implements Parcelable {
    private String longDescription;
    private List<ApiImage> images;
    private String description;
    private String tinyDescription;
    private String shortDescription;
    private String locale;
    private String title;
    private String sortingTitle;


    protected ApiLocalizedData() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiLocalizedData(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "longDescription":
                    this.longDescription = jsonReader.nextString();
                    break;
                case "images":
                    this.images = JsonReaderUtil.readArray(jsonReader, ApiImage.class);
                    break;
                case "description":
                    this.description = jsonReader.nextString();
                    break;
                case "tinyDescription":
                    this.tinyDescription = jsonReader.nextString();
                    break;
                case "shortDescription":
                    this.shortDescription = jsonReader.nextString();
                    break;
                case "locale":
                    this.locale = jsonReader.nextString();
                    break;
                case "title":
                    this.title = jsonReader.nextString();
                    break;
                case "sortingTitle":
                    this.sortingTitle = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getLongDescription() {
        return this.longDescription;
    }

    public List<ApiImage> getImages() {
        return this.images;
    }

    public String getDescription() {
        return this.description;
    }

    public String getTinyDescription() {
        return this.tinyDescription;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public String getLocale() {
        return this.locale;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSortingTitle() {
        return this.sortingTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiLocalizedData> CREATOR = new Parcelable.Creator<ApiLocalizedData>() {
        public ApiLocalizedData createFromParcel(Parcel in) {
            ApiLocalizedData object = new ApiLocalizedData();
            object.longDescription = in.readString();
            object.images = in.createTypedArrayList(ApiImage.CREATOR);
            object.description = in.readString();
            object.tinyDescription = in.readString();
            object.shortDescription = in.readString();
            object.locale = in.readString();
            object.title = in.readString();
            object.sortingTitle = in.readString();
            return object;
        }

        public ApiLocalizedData[] newArray(int size) {
            return new ApiLocalizedData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(longDescription);
        dest.writeTypedList(images);
        dest.writeString(description);
        dest.writeString(tinyDescription);
        dest.writeString(shortDescription);
        dest.writeString(locale);
        dest.writeString(title);
        dest.writeString(sortingTitle);
    }
}
