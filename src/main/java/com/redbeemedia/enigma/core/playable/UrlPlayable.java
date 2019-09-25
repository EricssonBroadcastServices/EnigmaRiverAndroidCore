package com.redbeemedia.enigma.core.playable;

import android.os.Parcel;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlPlayable implements IPlayable {
    private final URL url;

    public UrlPlayable(String url) throws MalformedURLException {
        this(new URL(url));
    }

    public UrlPlayable(URL url) {
        this.url = url;
    }

    @Override
    public void useWith(IPlayableHandler playableHandler) {
        playableHandler.startUsingUrl(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(url);
    }

    public static final Creator<UrlPlayable> CREATOR = new Creator<UrlPlayable>() {
        @Override
        public UrlPlayable createFromParcel(Parcel source) {
            URL url = (URL) source.readSerializable();
            return new UrlPlayable(url);
        }

        @Override
        public UrlPlayable[] newArray(int size) {
            return new UrlPlayable[size];
        }
    };
}
