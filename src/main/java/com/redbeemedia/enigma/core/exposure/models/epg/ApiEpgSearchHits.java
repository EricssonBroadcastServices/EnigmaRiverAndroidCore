package com.redbeemedia.enigma.core.exposure.models.epg;

import com.redbeemedia.enigma.core.exposure.models.channel.ApiChannelEPGResponse;
import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import java.io.IOException;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiEpgSearchHits implements Parcelable {
    private long pageNumber;
    private String suggestion;
    private long pageSize;
    private long totalCount;
    private List<ApiChannelEPGResponse> items;


    protected ApiEpgSearchHits() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiEpgSearchHits(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "pageNumber":
                    this.pageNumber = jsonReader.nextLong();
                    break;
                case "suggestion":
                    this.suggestion = jsonReader.nextString();
                    break;
                case "pageSize":
                    this.pageSize = jsonReader.nextLong();
                    break;
                case "totalCount":
                    this.totalCount = jsonReader.nextLong();
                    break;
                case "items":
                    this.items = JsonReaderUtil.readArray(jsonReader, ApiChannelEPGResponse.class);
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

    public String getSuggestion() {
        return this.suggestion;
    }

    public long getPageSize() {
        return this.pageSize;
    }

    public long getTotalCount() {
        return this.totalCount;
    }

    public List<ApiChannelEPGResponse> getItems() {
        return this.items;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiEpgSearchHits> CREATOR = new Parcelable.Creator<ApiEpgSearchHits>() {
        public ApiEpgSearchHits createFromParcel(Parcel in) {
            ApiEpgSearchHits object = new ApiEpgSearchHits();
            object.pageNumber = in.readLong();
            object.suggestion = in.readString();
            object.pageSize = in.readLong();
            object.totalCount = in.readLong();
            object.items = in.createTypedArrayList(ApiChannelEPGResponse.CREATOR);
            return object;
        }

        public ApiEpgSearchHits[] newArray(int size) {
            return new ApiEpgSearchHits[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(pageNumber);
        dest.writeString(suggestion);
        dest.writeLong(pageSize);
        dest.writeLong(totalCount);
        dest.writeTypedList(items);
    }
}
