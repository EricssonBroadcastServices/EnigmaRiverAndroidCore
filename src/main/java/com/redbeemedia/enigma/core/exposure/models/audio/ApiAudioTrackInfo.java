package com.redbeemedia.enigma.core.exposure.models.audio;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import com.redbeemedia.enigma.core.exposure.models.track.ApiTrackInfo;
import java.io.IOException;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiAudioTrackInfo implements Parcelable {
    private List<ApiTrackInfo> trackInfoList;
    private String language;


    private ApiAudioTrackInfo() {}//Private constructor for Parcelable.Creator

    public ApiAudioTrackInfo(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "trackInfoList":
                    this.trackInfoList = JsonReaderUtil.readArray(jsonReader, ApiTrackInfo.class);
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


    public List<ApiTrackInfo> getTrackInfoList() {
        return this.trackInfoList;
    }

    public String getLanguage() {
        return this.language;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiAudioTrackInfo> CREATOR = new Parcelable.Creator<ApiAudioTrackInfo>() {
        public ApiAudioTrackInfo createFromParcel(Parcel in) {
            ApiAudioTrackInfo object = new ApiAudioTrackInfo();
            object.trackInfoList = in.createTypedArrayList(ApiTrackInfo.CREATOR);
            object.language = in.readString();
            return object;
        }

        public ApiAudioTrackInfo[] newArray(int size) {
            return new ApiAudioTrackInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(trackInfoList);
        dest.writeString(language);
    }
}
