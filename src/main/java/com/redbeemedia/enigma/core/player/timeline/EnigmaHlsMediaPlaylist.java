package com.redbeemedia.enigma.core.player.timeline;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnigmaHlsMediaPlaylist {
    public static final int PLAYLIST_TYPE_UNKNOWN = 0;
    public static final int PLAYLIST_TYPE_VOD = 1;
    public static final int PLAYLIST_TYPE_EVENT = 2;
    public final int playlistType;
    public final long startOffsetUs;
    public final boolean hasPositiveStartOffset;
    public final boolean preciseStart;
    public final long startTimeUs;
    public final boolean hasDiscontinuitySequence;
    public final int discontinuitySequence;
    public final long mediaSequence;
    public final int version;
    public final long targetDurationUs;
    public final long partTargetDurationUs;
    public final boolean hasEndTag;
    public final boolean hasProgramDateTime;
    public final List<Segment> segments;
    public final List<Part> trailingParts;
    public final long durationUs;
    public final String baseUri;
    public final List<String> tags;
    public final boolean hasIndependentSegments;

    public EnigmaHlsMediaPlaylist(int playlistType, String baseUri, List<String> tags, long startOffsetUs, boolean preciseStart, long startTimeUs, boolean hasDiscontinuitySequence, int discontinuitySequence, long mediaSequence, int version, long targetDurationUs, long partTargetDurationUs, boolean hasIndependentSegments, boolean hasEndTag, boolean hasProgramDateTime, long durationUs, boolean hasPositiveStartOffset, List<Segment> segments, List<Part> trailingParts) {
        this.baseUri = baseUri;
        this.tags = new ArrayList<>(tags);
        this.hasIndependentSegments = hasIndependentSegments;
        this.playlistType = playlistType;
        this.startTimeUs = startTimeUs;
        this.preciseStart = preciseStart;
        this.hasDiscontinuitySequence = hasDiscontinuitySequence;
        this.discontinuitySequence = discontinuitySequence;
        this.mediaSequence = mediaSequence;
        this.version = version;
        this.targetDurationUs = targetDurationUs;
        this.partTargetDurationUs = partTargetDurationUs;
        this.hasEndTag = hasEndTag;
        this.hasProgramDateTime = hasProgramDateTime;
        this.segments = new ArrayList<>(segments);
        this.trailingParts = new ArrayList<>(trailingParts);
        this.durationUs = durationUs;
        this.startOffsetUs = startOffsetUs;
        this.hasPositiveStartOffset = hasPositiveStartOffset;
    }

    private static class SegmentBase {
        public final String url;
        public final long durationUs;
        public final int relativeDiscontinuitySequence;
        public final long relativeStartTimeUs;
        @Nullable
        public final String fullSegmentEncryptionKeyUri;
        @Nullable
        public final String encryptionIV;
        public final long byteRangeOffset;
        public final long byteRangeLength;
        public final boolean hasGapTag;

        private SegmentBase(String url, long durationUs, int relativeDiscontinuitySequence, long relativeStartTimeUs, @Nullable String fullSegmentEncryptionKeyUri, @Nullable String encryptionIV, long byteRangeOffset, long byteRangeLength, boolean hasGapTag) {
            this.url = url;
            this.durationUs = durationUs;
            this.relativeDiscontinuitySequence = relativeDiscontinuitySequence;
            this.relativeStartTimeUs = relativeStartTimeUs;
            this.fullSegmentEncryptionKeyUri = fullSegmentEncryptionKeyUri;
            this.encryptionIV = encryptionIV;
            this.byteRangeOffset = byteRangeOffset;
            this.byteRangeLength = byteRangeLength;
            this.hasGapTag = hasGapTag;
        }
    }

    public static final class Part extends SegmentBase {
        public final boolean isIndependent;
        public final boolean isPreload;

        public Part(String url, long durationUs, int relativeDiscontinuitySequence, long relativeStartTimeUs, @Nullable String fullSegmentEncryptionKeyUri, @Nullable String encryptionIV, long byteRangeOffset, long byteRangeLength, boolean hasGapTag, boolean isIndependent, boolean isPreload) {
            super(url, durationUs, relativeDiscontinuitySequence, relativeStartTimeUs, fullSegmentEncryptionKeyUri, encryptionIV, byteRangeOffset, byteRangeLength, hasGapTag);
            this.isIndependent = isIndependent;
            this.isPreload = isPreload;
        }
    }

    public static final class Segment extends SegmentBase {
        public final String title;
        public final List<Part> parts;

        public Segment(String url, long durationUs, int relativeDiscontinuitySequence, long relativeStartTimeUs, @Nullable String fullSegmentEncryptionKeyUri, @Nullable String encryptionIV, long byteRangeOffset, long byteRangeLength, boolean hasGapTag, String title, List<Part> parts) {
            super(url, durationUs, relativeDiscontinuitySequence, relativeStartTimeUs, fullSegmentEncryptionKeyUri, encryptionIV, byteRangeOffset, byteRangeLength, hasGapTag);
            this.title = title;
            this.parts = parts;
        }
    }
}
