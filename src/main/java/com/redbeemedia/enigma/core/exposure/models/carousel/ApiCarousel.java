package com.redbeemedia.enigma.core.exposure.models.carousel;

import android.os.Parcel;
import com.redbeemedia.enigma.core.exposure.models.asset.ApiAssetList;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import java.io.IOException;
import com.redbeemedia.enigma.core.exposure.models.localized.ApiLocalizedTitle;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiCarousel implements Parcelable {
    private String carouselId;
    private long sortOrder;
    private List<ApiLocalizedTitle> titles;
    private ApiAssetList items;


    protected ApiCarousel() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiCarousel(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "carouselId":
                    this.carouselId = jsonReader.nextString();
                    break;
                case "sortOrder":
                    this.sortOrder = jsonReader.nextLong();
                    break;
                case "titles":
                    this.titles = JsonReaderUtil.readArray(jsonReader, ApiLocalizedTitle.class);
                    break;
                case "items":
                    this.items = new ApiAssetList(jsonReader);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getCarouselId() {
        return this.carouselId;
    }

    public long getSortOrder() {
        return this.sortOrder;
    }

    public List<ApiLocalizedTitle> getTitles() {
        return this.titles;
    }

    public ApiAssetList getItems() {
        return this.items;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiCarousel> CREATOR = new Parcelable.Creator<ApiCarousel>() {
        public ApiCarousel createFromParcel(Parcel in) {
            ApiCarousel object = new ApiCarousel();
            object.carouselId = in.readString();
            object.sortOrder = in.readLong();
            object.titles = in.createTypedArrayList(ApiLocalizedTitle.CREATOR);
            object.items = in.readParcelable(ApiAssetList.class.getClassLoader());
            return object;
        }

        public ApiCarousel[] newArray(int size) {
            return new ApiCarousel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(carouselId);
        dest.writeLong(sortOrder);
        dest.writeTypedList(titles);
        dest.writeParcelable(items, flags);
    }
}
