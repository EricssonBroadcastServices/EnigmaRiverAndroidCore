package com.redbeemedia.enigma.core.playrequest;

import android.os.Parcel;
import android.os.Parcelable;


public final class PlaybackProperties implements IPlaybackProperties, Parcelable {
    private PlayFrom playFrom;

    public PlaybackProperties() {
        this.playFrom = PlayFrom.PLAYER_DEFAULT;
    }

    @Override
    public PlayFrom getPlayFrom() {
        return playFrom;
    }

    public PlaybackProperties setPlayFrom(PlayFrom playFrom) {
        if(playFrom == null) {
            throw new NullPointerException();
        }
        this.playFrom = playFrom;
        return this;
    }

    @Override
    public int hashCode() {
        return playFrom.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlaybackProperties && equals((PlaybackProperties) obj);
    }

    private boolean equals(PlaybackProperties obj) {
        return obj.playFrom == this.playFrom;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<PlaybackProperties> CREATOR = new Creator<PlaybackProperties>() {
        @Override
        public PlaybackProperties createFromParcel(Parcel source) {
            PlaybackProperties playbackProperties = new PlaybackProperties();
            playbackProperties.playFrom = source.readParcelable(null);
            return playbackProperties;
        }

        @Override
        public PlaybackProperties[] newArray(int size) {
            return new PlaybackProperties[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(playFrom, flags);
    }
}
