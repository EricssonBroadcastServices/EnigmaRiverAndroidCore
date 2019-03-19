package com.redbeemedia.enigma.core.exposure.models.channel;

import com.redbeemedia.enigma.core.exposure.models.program.ApiProgramResponse;
import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import java.io.IOException;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiChannelEPGResponse implements Parcelable {
    private long totalHitsAllChannels;
    private List<ApiProgramResponse> programs;
    private String channelId;


    protected ApiChannelEPGResponse() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiChannelEPGResponse(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "totalHitsAllChannels":
                    this.totalHitsAllChannels = jsonReader.nextLong();
                    break;
                case "programs":
                    this.programs = JsonReaderUtil.readArray(jsonReader, ApiProgramResponse.class);
                    break;
                case "channelId":
                    this.channelId = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public long getTotalHitsAllChannels() {
        return this.totalHitsAllChannels;
    }

    public List<ApiProgramResponse> getPrograms() {
        return this.programs;
    }

    public String getChannelId() {
        return this.channelId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiChannelEPGResponse> CREATOR = new Parcelable.Creator<ApiChannelEPGResponse>() {
        public ApiChannelEPGResponse createFromParcel(Parcel in) {
            ApiChannelEPGResponse object = new ApiChannelEPGResponse();
            object.totalHitsAllChannels = in.readLong();
            object.programs = in.createTypedArrayList(ApiProgramResponse.CREATOR);
            object.channelId = in.readString();
            return object;
        }

        public ApiChannelEPGResponse[] newArray(int size) {
            return new ApiChannelEPGResponse[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(totalHitsAllChannels);
        dest.writeTypedList(programs);
        dest.writeString(channelId);
    }
}
