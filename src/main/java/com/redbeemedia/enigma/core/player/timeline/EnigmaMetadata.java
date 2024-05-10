package com.redbeemedia.enigma.core.player.timeline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class EnigmaMetadata {
    private final List<Entry> entries;
    public final long presentationTimeUs;

    public EnigmaMetadata(long presentationTimeUs, List<Entry> entries) {
        this.presentationTimeUs = presentationTimeUs;
        this.entries = entries;
    }

    public int length() {
        return this.entries.size();
    }

    public Entry get(int index) {
        return entries.get(index);
    }

    public static class Entry {
        Format format;
        byte[] bytes;

        public Entry(Format format, byte[] bytes) {
            this.format = format;
            this.bytes = bytes;
        }

        @Nullable
        Format getWrappedMetadataFormat() {
            return format;
        }

        @Nullable
        byte[] getWrappedMetadataBytes() {
            return bytes;
        }
    }

    public static class Format {
        public static final int NO_VALUE = -1;
        public static final long OFFSET_SAMPLE_RELATIVE = Long.MAX_VALUE;
        @Nullable
        public final String id;
        @Nullable
        public final String label;
        public final int selectionFlags;
        public final int roleFlags;
        public final int bitrate;
        @Nullable
        public final String codecs;
        @Nullable
        public final String containerMimeType;
        @Nullable
        public final String sampleMimeType;
        public final int maxInputSize;
        public final List<byte[]> initializationData;
        public final long subsampleOffsetUs;
        public final int width;
        public final int height;
        public final float frameRate;
        public final int rotationDegrees;
        public final float pixelWidthHeightRatio;
        public final int stereoMode;
        @Nullable
        public final byte[] projectionData;
        public final int channelCount;
        public final int sampleRate;
        public final int pcmEncoding;
        public final int encoderDelay;
        public final int encoderPadding;
        @Nullable
        public final String language;
        public final int accessibilityChannel;

        public Format(@Nullable String id, @Nullable String label, int selectionFlags, int roleFlags, int bitrate, @Nullable String codecs, @Nullable String containerMimeType, @Nullable String sampleMimeType, int maxInputSize, @Nullable List<byte[]> initializationData, long subsampleOffsetUs, int width, int height, float frameRate, int rotationDegrees, float pixelWidthHeightRatio, @Nullable byte[] projectionData, int stereoMode, int channelCount, int sampleRate, int pcmEncoding, int encoderDelay, int encoderPadding, @Nullable String language, int accessibilityChannel) {
            this.id = id;
            this.label = label;
            this.selectionFlags = selectionFlags;
            this.roleFlags = roleFlags;
            this.bitrate = bitrate;
            this.codecs = codecs;
            this.containerMimeType = containerMimeType;
            this.sampleMimeType = sampleMimeType;
            this.maxInputSize = maxInputSize;
            this.initializationData = initializationData;
            this.subsampleOffsetUs = subsampleOffsetUs;
            this.width = width;
            this.height = height;
            this.frameRate = frameRate;
            this.rotationDegrees = rotationDegrees;
            this.pixelWidthHeightRatio = pixelWidthHeightRatio;
            this.projectionData = projectionData;
            this.stereoMode = stereoMode;
            this.channelCount = channelCount;
            this.sampleRate = sampleRate;
            this.pcmEncoding = pcmEncoding;
            this.encoderDelay = encoderDelay;
            this.encoderPadding = encoderPadding;
            this.language = language;
            this.accessibilityChannel = accessibilityChannel;
        }

        @NonNull
        public String toString() {
            return "Format(" + this.id + ", " + this.label + ", " + this.containerMimeType + ", " + this.sampleMimeType + ", " + this.codecs + ", " + this.bitrate + ", " + this.language + ", [" + this.width + ", " + this.height + ", " + this.frameRate + "], [" + this.channelCount + ", " + this.sampleRate + "])";
        }
    }
}
