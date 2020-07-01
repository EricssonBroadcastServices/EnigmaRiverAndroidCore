package com.redbeemedia.enigma.core.format;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class EnigmaMediaFormat {
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
        return obj instanceof EnigmaMediaFormat && equals(((EnigmaMediaFormat) obj).streamFormat, ((EnigmaMediaFormat) obj).drmTechnology);
    }

    @Override
    public int hashCode() {
        return streamFormat.hashCode()*37 + drmTechnology.hashCode();
    }

    public boolean equals(StreamFormat streamFormat, DrmTechnology drmTechnology) {
        return this.streamFormat == streamFormat && this.drmTechnology == drmTechnology;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(streamFormat);
        stringBuilder.append(" ");
        stringBuilder.append(drmTechnology);
        return stringBuilder.toString();
    }

    /**
     * <p>Convenience method for creating an EnigmaMediaFormat.</p>
     *
     * <p>Usage example:</p>
     * <pre>
     * EnigmaMediaFormat unencryptedDash = EnigmaMediaFormat.DASH().unenc();
     * EnigmaMediaFormat dashWidevine = EnigmaMediaFormat.DASH().widevine();
     * </pre>
     *
     * @see #HLS()
     * @see #SMOOTHSTREAMING()
     * @return Construction continuation interface
     */
    public static EnigmaMediaFormatBuilder_DrmTechnologySelector DASH() {
        return new EnigmaMediaFormatBuilder_DrmTechnologySelector(StreamFormat.DASH);
    }

    /**
     * <p>Convenience method for creating an EnigmaMediaFormat.</p>
     *
     * <p>Usage example:</p>
     * <pre>
     * EnigmaMediaFormat unencryptedHls = EnigmaMediaFormat.HLS().unenc();
     * EnigmaMediaFormat hlsFairplay = EnigmaMediaFormat.HLS().fairplay();
     * </pre>
     *
     * @see #DASH()
     * @see #SMOOTHSTREAMING()
     * @return Construction continuation interface
     */
    public static EnigmaMediaFormatBuilder_DrmTechnologySelector HLS() {
        return new EnigmaMediaFormatBuilder_DrmTechnologySelector(StreamFormat.HLS);
    }

    /**
     * <p>Convenience method for creating an EnigmaMediaFormat.</p>
     *
     * <p>Usage example:</p>
     * <pre>
     * EnigmaMediaFormat unencryptedSmoothstreaming = EnigmaMediaFormat.SMOOTHSTREAMING().unenc();
     * EnigmaMediaFormat smoothstreamingPlayready = EnigmaMediaFormat.SMOOTHSTREAMING().playready();
     * </pre>
     *
     * @see #DASH()
     * @see #HLS()
     * @return Construction continuation interface
     */
    public static EnigmaMediaFormatBuilder_DrmTechnologySelector SMOOTHSTREAMING() {
        return new EnigmaMediaFormatBuilder_DrmTechnologySelector(StreamFormat.SMOOTHSTREAMING);
    }

    public static class EnigmaMediaFormatBuilder_DrmTechnologySelector {
        private final StreamFormat streamFormat;

        private EnigmaMediaFormatBuilder_DrmTechnologySelector(StreamFormat streamFormat) {
            this.streamFormat = streamFormat;
        }

        public EnigmaMediaFormat unenc() {
            return new EnigmaMediaFormat(streamFormat, DrmTechnology.NONE);
        }

        public EnigmaMediaFormat widevine() {
            return new EnigmaMediaFormat(streamFormat, DrmTechnology.WIDEVINE);
        }

        public EnigmaMediaFormat fairplay() {
            return new EnigmaMediaFormat(streamFormat, DrmTechnology.FAIRPLAY);
        }

        public EnigmaMediaFormat playready() {
            return new EnigmaMediaFormat(streamFormat, DrmTechnology.PLAYREADY);
        }
    }


    public static class StreamFormat {
        private StreamFormat() {}
        public static final StreamFormat DASH = new StreamFormat();
        public static final StreamFormat HLS = new StreamFormat();
        public static final StreamFormat SMOOTHSTREAMING = new StreamFormat();

        @Override
        public String toString() {
            return getName(StreamFormat.class, this);
        }
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

        @Override
        public String toString() {
            return getName(DrmTechnology.class, this);
        }
    }

    private static String getName(Class<?> containerClass, Object value) {
        for(Field field : containerClass.getDeclaredFields()) {
            boolean accessible = field.isAccessible();
            try {
                field.setAccessible(true);
                if(Modifier.isStatic(field.getModifiers())) {
                    try {
                        if(field.get(null) == value) {
                            return field.getName();
                        }
                    } catch (IllegalAccessException e) {
                        //Ignore and continue
                    }
                }
            } finally {
                field.setAccessible(accessible);
            }
        }
        return value.getClass().getName() + "@" + Integer.toHexString(value.hashCode());
    }
}
