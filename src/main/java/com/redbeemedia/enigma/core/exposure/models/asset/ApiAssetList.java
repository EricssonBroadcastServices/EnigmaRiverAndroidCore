package com.redbeemedia.enigma.core.exposure.models.asset;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import java.io.IOException;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiAssetList implements Parcelable {
    private long pageNumber;
    private long pageSize;
    private long totalCount;
    private List<ApiAsset> items;


    protected ApiAssetList() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiAssetList(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "pageNumber":
                    this.pageNumber = jsonReader.nextLong();
                    break;
                case "pageSize":
                    this.pageSize = jsonReader.nextLong();
                    break;
                case "totalCount":
                    this.totalCount = jsonReader.nextLong();
                    break;
                case "items":
                    this.items = JsonReaderUtil.readArray(jsonReader, ApiAsset.class);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public long getPageNumber() {
        return this.pageNumber;
    }

    public long getPageSize() {
        return this.pageSize;
    }

    public long getTotalCount() {
        return this.totalCount;
    }

    public List<ApiAsset> getItems() {
        return this.items;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiAssetList> CREATOR = new Parcelable.Creator<ApiAssetList>() {
        public ApiAssetList createFromParcel(Parcel in) {
            ApiAssetList object = new ApiAssetList();
            object.pageNumber = in.readLong();
            object.pageSize = in.readLong();
            object.totalCount = in.readLong();
            object.items = in.createTypedArrayList(ApiAsset.CREATOR);
            return object;
        }

        public ApiAssetList[] newArray(int size) {
            return new ApiAssetList[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(pageNumber);
        dest.writeLong(pageSize);
        dest.writeLong(totalCount);
        dest.writeTypedList(items);
    }
}
