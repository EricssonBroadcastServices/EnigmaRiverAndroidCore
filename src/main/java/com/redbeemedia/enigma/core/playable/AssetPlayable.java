package com.redbeemedia.enigma.core.playable;

import android.os.Parcel;
import android.text.TextUtils;

public final class AssetPlayable implements IAssetPlayable {
    private final String assetId;

    public AssetPlayable(final String assetId) {
        if(assetId == null) {
            throw new NullPointerException("assetId was null");
        }
        this.assetId = assetId;
    }

    @Override
    public void useWith(final IPlayableHandler playableHandler) {
        if (!TextUtils.isEmpty(assetId)) {
            playableHandler.startUsingAssetId(assetId);
        }
    }

    @Override
    public String getAssetId() {
        return assetId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(assetId);
    }

    public static final Creator<AssetPlayable> CREATOR = new Creator<AssetPlayable>() {
        @Override
        public AssetPlayable createFromParcel(Parcel source) {
            String assetId = source.readString();
            return new AssetPlayable(assetId);
        }

        @Override
        public AssetPlayable[] newArray(int size) {
            return new AssetPlayable[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AssetPlayable && ((AssetPlayable) obj).assetId.equals(this.assetId);
    }

    @Override
    public int hashCode() {
        return assetId.hashCode();
    }
}
