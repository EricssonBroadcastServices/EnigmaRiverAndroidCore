// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.epg.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;

import com.redbeemedia.enigma.core.util.JsonReaderUtil;

import java.io.IOException;
import java.util.List;


/*package-protected*/ class ApiChannelEPGResponse implements Parcelable {
    private int totalHitsAllChannels;
    private List<ApiProgramResponse> programs;
    private String channelId;


    protected ApiChannelEPGResponse() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiChannelEPGResponse(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "totalHitsAllChannels":
                    this.totalHitsAllChannels = jsonReader.nextInt();
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


    public int getTotalHitsAllChannels() {
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

    public static final Creator<ApiChannelEPGResponse> CREATOR = new Creator<ApiChannelEPGResponse>() {
        public ApiChannelEPGResponse createFromParcel(Parcel in) {
            ApiChannelEPGResponse object = new ApiChannelEPGResponse();
            object.totalHitsAllChannels = in.readInt();
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
        dest.writeInt(totalHitsAllChannels);
        dest.writeTypedList(programs);
        dest.writeString(channelId);
    }
}
