package com.redbeemedia.enigma.core.playrequest;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;
import java.util.TreeMap;

public interface IPlaybackProperties {
    PlayFrom getPlayFrom();

    class PlayFrom implements Parcelable {
        private static Map<String,PlayFrom> registry = new TreeMap<>();

        public static final PlayFrom PLAYER_DEFAULT = new PlayerDefault();
        public static final PlayFrom BEGINNING = new Beginning();
        public static final PlayFrom BOOKMARK = new Bookmark();
//        public static final PlayFrom LIVE_EDGE = new LiveEdge();

        private final String id;

        protected PlayFrom(String id) {
            this.id = id;
            registry.put(id, this);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
        }

        public static final Creator<PlayFrom> CREATOR = new Creator<PlayFrom>() {
            @Override
            public PlayFrom createFromParcel(Parcel source) {
                String id = source.readString();
                PlayFrom playFrom = registry.get(id);
                if(playFrom == null) {
                    throw new IllegalArgumentException("No PlayFrom with id '"+id+"'");
                }
                return playFrom;
            }

            @Override
            public PlayFrom[] newArray(int size) {
                return new PlayFrom[size];
            }
        };

        private static class PlayerDefault extends PlayFrom {
            protected PlayerDefault() {
                super("playerDefault");
            }
        }

        private static class Beginning extends PlayFrom {

            protected Beginning() {
                super("beginning");
            }
        }
        private static class Bookmark extends PlayFrom {

            protected Bookmark() {
                super("bookmark");
            }
        }
    }
}
