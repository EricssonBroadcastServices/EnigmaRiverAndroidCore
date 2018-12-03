package com.redbeemedia.enigma.core.asset;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Media implements Parcelable {

    private String mediaId;
    private String drm;
    private String format;
    private long durationMillis;
    private String status;

    public Media() {}

    public String getMediaId() {
        return mediaId;
    }

    public String getDrm() {
        return drm;
    }

    public String getFormat() {
        return format;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public String getStatus() {
        return status;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public void setDrm(String drm) {
        this.drm = drm;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static long getDuration(final List<Media> media, final long fallback) {
        final long duration;
        if (media != null && !media.isEmpty()) {
            duration = media.get(0).getDurationMillis();
        } else {
            duration = fallback;
        }

        return duration;
    }

    // <editor-fold description="PARCELABLE IMPLEMENTATION">
    protected Media(final Parcel in) {
        mediaId = in.readString();
        drm = in.readString();
        format = in.readString();
        durationMillis = in.readLong();
        status = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mediaId);
        dest.writeString(drm);
        dest.writeString(format);
        dest.writeLong(durationMillis);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(final Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(final int size) {
            return new Media[size];
        }
    };
    // </editor-fold>
}
