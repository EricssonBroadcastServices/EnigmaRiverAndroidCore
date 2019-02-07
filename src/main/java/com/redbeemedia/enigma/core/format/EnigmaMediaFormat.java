package com.redbeemedia.enigma.core.format;

import java.util.ArrayList;
import java.util.List;

public final class EnigmaMediaFormat {
    public static final EnigmaMediaFormat DASH_UNENCRYPTED = new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.NONE);
    public static final EnigmaMediaFormat DASH_CENC = new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.WIDEVINE);

    private final StreamFormat streamFormat;
    private final DrmTechnology drmTechnology;

    public EnigmaMediaFormat(StreamFormat streamFormat, DrmTechnology drmTechnology) {
        if(streamFormat == null || drmTechnology == null) {
            throw new NullPointerException();
        }
        this.streamFormat = streamFormat;
        this.drmTechnology = drmTechnology;
    }

    public StreamFormat getStreamFormat() {
        return streamFormat;
    }

    public DrmTechnology getDrmTechnology() {
        return drmTechnology;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EnigmaMediaFormat && ((EnigmaMediaFormat) obj).streamFormat == this.streamFormat && ((EnigmaMediaFormat) obj).drmTechnology == this.drmTechnology;
    }

    @Override
    public int hashCode() {
        return streamFormat.hashCode()*37 + drmTechnology.hashCode();
    }

    public static class StreamFormat {
        private StreamFormat() {}
        public static final StreamFormat DASH = new StreamFormat();
        public static final StreamFormat HLS = new StreamFormat();
        public static final StreamFormat SMOOTHSTREAMING = new StreamFormat();
    }

    public static class DrmTechnology {
        private static List<DrmTechnology> all = new ArrayList<>(4);

        private final String key;

        private DrmTechnology(String key) {
            this.key = key;
            all.add(this);
        }

        public static final DrmTechnology NONE = new DrmTechnology(null);
        public static final DrmTechnology WIDEVINE = new DrmTechnology("com.widevine.alpha");
        public static final DrmTechnology FAIRPLAY = new DrmTechnology("com.apple.fairplay");
        public static final DrmTechnology PLAYREADY = new DrmTechnology("com.microsoft.playready");


        private static final DrmTechnology[] values = all.toArray(new DrmTechnology[0]);

        public static DrmTechnology[] values() {
            return values;
        }

        public String getKey() {
            return key;
        }
    }
}
