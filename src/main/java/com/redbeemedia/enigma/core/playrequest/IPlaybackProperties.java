package com.redbeemedia.enigma.core.playrequest;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.format.IMediaFormatSelector;
import com.redbeemedia.enigma.core.time.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface IPlaybackProperties {
    PlayFrom getPlayFrom();
    IMediaFormatSelector getMediaFormatSelector();
    MaterialProfile getMaterialProfile();

    @Nullable
    AdobePrimetime getAdobePrimetime();

    /** If true, analytics events will be sent during playback. */
    boolean enableAnalytics();

    class PlayFrom implements Parcelable {
        public static final PlayFrom PLAYER_DEFAULT = new PlayFrom(PlayFromPreference.LIVE_EDGE, PlayFromPreference.BEGINNING);
        public static final PlayFrom BEGINNING = new PlayFrom(PlayFromPreference.BEGINNING);
        public static final PlayFrom LIVE_EDGE = new PlayFrom(PlayFromPreference.LIVE_EDGE, PlayFromPreference.BEGINNING);
        public static final PlayFrom BOOKMARK = new PlayFrom(PlayFromPreference.BOOKMARK, PlayFromPreference.LIVE_EDGE, PlayFromPreference.BEGINNING);
        public static final PlayFromOffset OFFSET(Duration offset) {
            return new PlayFromOffset(offset);
        }

        private final List<PlayFromPreference> preferences;

        public PlayFrom(PlayFromPreference ... preferences) {
            this(Arrays.asList(preferences));
        }

        public PlayFrom(List<PlayFromPreference> preferences) {
            this.preferences = Collections.unmodifiableList(new ArrayList<>(preferences));
            for(PlayFromPreference preference : preferences) {
                if(preference == null) {
                    throw new NullPointerException();
                } else if(preference == PlayFromPreference.OFFSET) {
                    if(!(this instanceof PlayFromOffset)) {
                        throw new IllegalStateException("This PlayFromPreference.OFFSET is only allowed i PlayFromOffset");
                    }
                }
            }
        }

        public List<PlayFromPreference> getPreferences() {
            return preferences;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PlayFrom && this.preferences.equals(((PlayFrom) obj).preferences);
        }

        @Override
        public int hashCode() {
            return preferences.hashCode();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(preferences.size());
            for(PlayFromPreference preference : preferences) {
                dest.writeString(preference.name());
            }
        }

        public static final Creator<PlayFrom> CREATOR = new Creator<PlayFrom>() {
            @Override
            public PlayFrom createFromParcel(Parcel source) {
                int numberOfPreferences = source.readInt();
                List<PlayFromPreference> preferences = new ArrayList<>();
                for(int i = 0; i < numberOfPreferences; ++i) {
                    preferences.add(PlayFromPreference.valueOf(source.readString()));
                }
                return new PlayFrom(preferences);
            }

            @Override
            public PlayFrom[] newArray(int size) {
                return new PlayFrom[size];
            }
        };

        public enum PlayFromPreference {
            BEGINNING,
            BOOKMARK,
            LIVE_EDGE,
            OFFSET;
        }
    }

    class PlayFromOffset extends PlayFrom {
        private final Duration offset;

        protected PlayFromOffset(Duration offset) {
            super(PlayFromPreference.OFFSET);
            this.offset = Objects.requireNonNull(offset);
        }

        public Duration getOffset() {
            return offset;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof PlayFromOffset) && ((PlayFromOffset) obj).offset.equals(this.offset) && super.equals(obj);
        }
    }
}
