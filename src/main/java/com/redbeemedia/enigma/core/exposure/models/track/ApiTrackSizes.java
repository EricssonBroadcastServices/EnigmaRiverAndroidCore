package com.redbeemedia.enigma.core.exposure.models.track;

import com.redbeemedia.enigma.core.exposure.models.subtitle.ApiSubtitleTrackInfo;
import android.os.Parcel;
import com.redbeemedia.enigma.core.exposure.models.audio.ApiAudioTrackInfo;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import java.io.IOException;
import com.redbeemedia.enigma.core.exposure.models.video.ApiVideoTrackInfo;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiTrackSizes implements Parcelable {
    private List<ApiAudioTrackInfo> audioTracks;
    private List<ApiSubtitleTrackInfo> subtitleTracks;
    private List<ApiVideoTrackInfo> videoTracks;


    protected ApiTrackSizes() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiTrackSizes(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "audioTracks":
                    this.audioTracks = JsonReaderUtil.readArray(jsonReader, ApiAudioTrackInfo.class);
                    break;
                case "subtitleTracks":
                    this.subtitleTracks = JsonReaderUtil.readArray(jsonReader, ApiSubtitleTrackInfo.class);
                    break;
                case "videoTracks":
                    this.videoTracks = JsonReaderUtil.readArray(jsonReader, ApiVideoTrackInfo.class);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public List<ApiAudioTrackInfo> getAudioTracks() {
        return this.audioTracks;
    }

    public List<ApiSubtitleTrackInfo> getSubtitleTracks() {
        return this.subtitleTracks;
    }

    public List<ApiVideoTrackInfo> getVideoTracks() {
        return this.videoTracks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiTrackSizes> CREATOR = new Parcelable.Creator<ApiTrackSizes>() {
        public ApiTrackSizes createFromParcel(Parcel in) {
            ApiTrackSizes object = new ApiTrackSizes();
            object.audioTracks = in.createTypedArrayList(ApiAudioTrackInfo.CREATOR);
            object.subtitleTracks = in.createTypedArrayList(ApiSubtitleTrackInfo.CREATOR);
            object.videoTracks = in.createTypedArrayList(ApiVideoTrackInfo.CREATOR);
            return object;
        }

        public ApiTrackSizes[] newArray(int size) {
            return new ApiTrackSizes[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(audioTracks);
        dest.writeTypedList(subtitleTracks);
        dest.writeTypedList(videoTracks);
    }
}
