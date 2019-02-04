package com.redbeemedia.enigma.core.exposure.models.asset;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiAssetRights implements Parcelable {
    private boolean jailbrokenBlocked;
    private boolean downloadBlocked;
    private int minBitrate;
    private boolean streamingBlocked;
    private boolean threeGBlocked;
    private int downloadMaxSecondsAfterPlay;
    private int maxBitrate;
    private boolean sessionShiftEnabled;
    private boolean rwEnabled;
    private boolean amcDebugLogEnabled;
    private int downloadMaxSecondsAfterDownload;
    private boolean locationEnabled;
    private boolean analyticsEnabled;
    private int maxAds;
    private boolean HDMIBlocked;
    private boolean ffEnabled;
    private int minPlayPosition;
    private int maxResHeight;
    private boolean wifiBlocked;
    private boolean fourGBlocked;
    private int maxFileSize;
    private int playCount;
    private boolean airplayBlocked;
    private int maxPlayPosition;
    private String expiration;
    private String activation;
    private int maxResWidth;


    protected ApiAssetRights() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiAssetRights(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "jailbrokenBlocked":
                    this.jailbrokenBlocked = jsonReader.nextBoolean();
                    break;
                case "downloadBlocked":
                    this.downloadBlocked = jsonReader.nextBoolean();
                    break;
                case "minBitrate":
                    this.minBitrate = jsonReader.nextInt();
                    break;
                case "streamingBlocked":
                    this.streamingBlocked = jsonReader.nextBoolean();
                    break;
                case "threeGBlocked":
                    this.threeGBlocked = jsonReader.nextBoolean();
                    break;
                case "downloadMaxSecondsAfterPlay":
                    this.downloadMaxSecondsAfterPlay = jsonReader.nextInt();
                    break;
                case "maxBitrate":
                    this.maxBitrate = jsonReader.nextInt();
                    break;
                case "sessionShiftEnabled":
                    this.sessionShiftEnabled = jsonReader.nextBoolean();
                    break;
                case "rwEnabled":
                    this.rwEnabled = jsonReader.nextBoolean();
                    break;
                case "amcDebugLogEnabled":
                    this.amcDebugLogEnabled = jsonReader.nextBoolean();
                    break;
                case "downloadMaxSecondsAfterDownload":
                    this.downloadMaxSecondsAfterDownload = jsonReader.nextInt();
                    break;
                case "locationEnabled":
                    this.locationEnabled = jsonReader.nextBoolean();
                    break;
                case "analyticsEnabled":
                    this.analyticsEnabled = jsonReader.nextBoolean();
                    break;
                case "maxAds":
                    this.maxAds = jsonReader.nextInt();
                    break;
                case "HDMIBlocked":
                    this.HDMIBlocked = jsonReader.nextBoolean();
                    break;
                case "ffEnabled":
                    this.ffEnabled = jsonReader.nextBoolean();
                    break;
                case "minPlayPosition":
                    this.minPlayPosition = jsonReader.nextInt();
                    break;
                case "maxResHeight":
                    this.maxResHeight = jsonReader.nextInt();
                    break;
                case "wifiBlocked":
                    this.wifiBlocked = jsonReader.nextBoolean();
                    break;
                case "fourGBlocked":
                    this.fourGBlocked = jsonReader.nextBoolean();
                    break;
                case "maxFileSize":
                    this.maxFileSize = jsonReader.nextInt();
                    break;
                case "playCount":
                    this.playCount = jsonReader.nextInt();
                    break;
                case "airplayBlocked":
                    this.airplayBlocked = jsonReader.nextBoolean();
                    break;
                case "maxPlayPosition":
                    this.maxPlayPosition = jsonReader.nextInt();
                    break;
                case "expiration":
                    this.expiration = jsonReader.nextString();
                    break;
                case "activation":
                    this.activation = jsonReader.nextString();
                    break;
                case "maxResWidth":
                    this.maxResWidth = jsonReader.nextInt();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public boolean getJailbrokenBlocked() {
        return this.jailbrokenBlocked;
    }

    public boolean getDownloadBlocked() {
        return this.downloadBlocked;
    }

    public int getMinBitrate() {
        return this.minBitrate;
    }

    public boolean getStreamingBlocked() {
        return this.streamingBlocked;
    }

    public boolean getThreeGBlocked() {
        return this.threeGBlocked;
    }

    public int getDownloadMaxSecondsAfterPlay() {
        return this.downloadMaxSecondsAfterPlay;
    }

    public int getMaxBitrate() {
        return this.maxBitrate;
    }

    public boolean getSessionShiftEnabled() {
        return this.sessionShiftEnabled;
    }

    public boolean getRwEnabled() {
        return this.rwEnabled;
    }

    public boolean getAmcDebugLogEnabled() {
        return this.amcDebugLogEnabled;
    }

    public int getDownloadMaxSecondsAfterDownload() {
        return this.downloadMaxSecondsAfterDownload;
    }

    public boolean getLocationEnabled() {
        return this.locationEnabled;
    }

    public boolean getAnalyticsEnabled() {
        return this.analyticsEnabled;
    }

    public int getMaxAds() {
        return this.maxAds;
    }

    public boolean getHDMIBlocked() {
        return this.HDMIBlocked;
    }

    public boolean getFfEnabled() {
        return this.ffEnabled;
    }

    public int getMinPlayPosition() {
        return this.minPlayPosition;
    }

    public int getMaxResHeight() {
        return this.maxResHeight;
    }

    public boolean getWifiBlocked() {
        return this.wifiBlocked;
    }

    public boolean getFourGBlocked() {
        return this.fourGBlocked;
    }

    public int getMaxFileSize() {
        return this.maxFileSize;
    }

    public int getPlayCount() {
        return this.playCount;
    }

    public boolean getAirplayBlocked() {
        return this.airplayBlocked;
    }

    public int getMaxPlayPosition() {
        return this.maxPlayPosition;
    }

    public String getExpiration() {
        return this.expiration;
    }

    public String getActivation() {
        return this.activation;
    }

    public int getMaxResWidth() {
        return this.maxResWidth;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiAssetRights> CREATOR = new Parcelable.Creator<ApiAssetRights>() {
        public ApiAssetRights createFromParcel(Parcel in) {
            ApiAssetRights object = new ApiAssetRights();
            object.jailbrokenBlocked = (in.readInt() != 0);
            object.downloadBlocked = (in.readInt() != 0);
            object.minBitrate = in.readInt();
            object.streamingBlocked = (in.readInt() != 0);
            object.threeGBlocked = (in.readInt() != 0);
            object.downloadMaxSecondsAfterPlay = in.readInt();
            object.maxBitrate = in.readInt();
            object.sessionShiftEnabled = (in.readInt() != 0);
            object.rwEnabled = (in.readInt() != 0);
            object.amcDebugLogEnabled = (in.readInt() != 0);
            object.downloadMaxSecondsAfterDownload = in.readInt();
            object.locationEnabled = (in.readInt() != 0);
            object.analyticsEnabled = (in.readInt() != 0);
            object.maxAds = in.readInt();
            object.HDMIBlocked = (in.readInt() != 0);
            object.ffEnabled = (in.readInt() != 0);
            object.minPlayPosition = in.readInt();
            object.maxResHeight = in.readInt();
            object.wifiBlocked = (in.readInt() != 0);
            object.fourGBlocked = (in.readInt() != 0);
            object.maxFileSize = in.readInt();
            object.playCount = in.readInt();
            object.airplayBlocked = (in.readInt() != 0);
            object.maxPlayPosition = in.readInt();
            object.expiration = in.readString();
            object.activation = in.readString();
            object.maxResWidth = in.readInt();
            return object;
        }

        public ApiAssetRights[] newArray(int size) {
            return new ApiAssetRights[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(jailbrokenBlocked ? 1 : 0);
        dest.writeInt(downloadBlocked ? 1 : 0);
        dest.writeInt(minBitrate);
        dest.writeInt(streamingBlocked ? 1 : 0);
        dest.writeInt(threeGBlocked ? 1 : 0);
        dest.writeInt(downloadMaxSecondsAfterPlay);
        dest.writeInt(maxBitrate);
        dest.writeInt(sessionShiftEnabled ? 1 : 0);
        dest.writeInt(rwEnabled ? 1 : 0);
        dest.writeInt(amcDebugLogEnabled ? 1 : 0);
        dest.writeInt(downloadMaxSecondsAfterDownload);
        dest.writeInt(locationEnabled ? 1 : 0);
        dest.writeInt(analyticsEnabled ? 1 : 0);
        dest.writeInt(maxAds);
        dest.writeInt(HDMIBlocked ? 1 : 0);
        dest.writeInt(ffEnabled ? 1 : 0);
        dest.writeInt(minPlayPosition);
        dest.writeInt(maxResHeight);
        dest.writeInt(wifiBlocked ? 1 : 0);
        dest.writeInt(fourGBlocked ? 1 : 0);
        dest.writeInt(maxFileSize);
        dest.writeInt(playCount);
        dest.writeInt(airplayBlocked ? 1 : 0);
        dest.writeInt(maxPlayPosition);
        dest.writeString(expiration);
        dest.writeString(activation);
        dest.writeInt(maxResWidth);
    }
}
