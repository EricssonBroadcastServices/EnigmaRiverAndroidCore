package com.redbeemedia.enigma.core.exposure.models.program;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import com.redbeemedia.enigma.core.exposure.models.asset.ApiAsset;
import java.io.IOException;


public class ApiProgramResponse implements Parcelable {
    private boolean blackout;
    private boolean vodAvailable;
    private String assetId;
    private boolean catchup;
    private String created;
    private boolean catchupBlocked;
    private String startTime;
    private String endTime;
    private ApiAsset asset;
    private String channelId;
    private String programId;
    private String changed;


    protected ApiProgramResponse() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiProgramResponse(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "blackout":
                    this.blackout = jsonReader.nextBoolean();
                    break;
                case "vodAvailable":
                    this.vodAvailable = jsonReader.nextBoolean();
                    break;
                case "assetId":
                    this.assetId = jsonReader.nextString();
                    break;
                case "catchup":
                    this.catchup = jsonReader.nextBoolean();
                    break;
                case "created":
                    this.created = jsonReader.nextString();
                    break;
                case "catchupBlocked":
                    this.catchupBlocked = jsonReader.nextBoolean();
                    break;
                case "startTime":
                    this.startTime = jsonReader.nextString();
                    break;
                case "endTime":
                    this.endTime = jsonReader.nextString();
                    break;
                case "asset":
                    this.asset = new ApiAsset(jsonReader);
                    break;
                case "channelId":
                    this.channelId = jsonReader.nextString();
                    break;
                case "programId":
                    this.programId = jsonReader.nextString();
                    break;
                case "changed":
                    this.changed = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public boolean getBlackout() {
        return this.blackout;
    }

    public boolean getVodAvailable() {
        return this.vodAvailable;
    }

    public String getAssetId() {
        return this.assetId;
    }

    public boolean getCatchup() {
        return this.catchup;
    }

    public String getCreated() {
        return this.created;
    }

    public boolean getCatchupBlocked() {
        return this.catchupBlocked;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public ApiAsset getAsset() {
        return this.asset;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public String getProgramId() {
        return this.programId;
    }

    public String getChanged() {
        return this.changed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiProgramResponse> CREATOR = new Parcelable.Creator<ApiProgramResponse>() {
        public ApiProgramResponse createFromParcel(Parcel in) {
            ApiProgramResponse object = new ApiProgramResponse();
            object.blackout = (in.readInt() != 0);
            object.vodAvailable = (in.readInt() != 0);
            object.assetId = in.readString();
            object.catchup = (in.readInt() != 0);
            object.created = in.readString();
            object.catchupBlocked = (in.readInt() != 0);
            object.startTime = in.readString();
            object.endTime = in.readString();
            object.asset = in.readParcelable(ApiAsset.class.getClassLoader());
            object.channelId = in.readString();
            object.programId = in.readString();
            object.changed = in.readString();
            return object;
        }

        public ApiProgramResponse[] newArray(int size) {
            return new ApiProgramResponse[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(blackout ? 1 : 0);
        dest.writeInt(vodAvailable ? 1 : 0);
        dest.writeString(assetId);
        dest.writeInt(catchup ? 1 : 0);
        dest.writeString(created);
        dest.writeInt(catchupBlocked ? 1 : 0);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeParcelable(asset, flags);
        dest.writeString(channelId);
        dest.writeString(programId);
        dest.writeString(changed);
    }
}
