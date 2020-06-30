package com.redbeemedia.enigma.core.format;

import org.junit.Assert;
import org.junit.Test;

public class MediaFormatPreferenceSpecTest extends MediaFormatPreferenceTestUtil {
    @Test
    public void testApplyToEmpty() {
        MediaFormatPreferenceSpec preference = new MediaFormatPreferenceSpec(
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.NONE),
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.HLS, EnigmaMediaFormat.DrmTechnology.NONE),
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.WIDEVINE)
                );
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();
        preferenceList = preference.applyPreference(preferenceList);

        assertOrder(preferenceList,
                format().dash().none(),
                format().hls().none(),
                format().dash().widevine()
                );
    }

    @Test
    public void testApplyDisjunct() {
        MediaFormatPreferenceSpec preference = new MediaFormatPreferenceSpec(
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.NONE),
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.HLS, EnigmaMediaFormat.DrmTechnology.NONE),
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.WIDEVINE)
        );
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();
        preferenceList.putLast(format().ss().fairplay());
        preferenceList.putLast(format().ss().widevine());
        preferenceList.putLast(format().ss().playready());
        preferenceList.putLast(format().ss().none());

        assertOrder(preferenceList,
                format().ss().fairplay(),
                format().ss().widevine(),
                format().ss().playready(),
                format().ss().none()
        );

        preferenceList = preference.applyPreference(preferenceList);

        assertOrder(preferenceList,
                format().dash().none(),
                format().hls().none(),
                format().dash().widevine(),
                format().ss().fairplay(),
                format().ss().widevine(),
                format().ss().playready(),
                format().ss().none()
        );
    }

    @Test
    public void testApplyOverlapping() {
        MediaFormatPreferenceSpec preference = new MediaFormatPreferenceSpec(
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.NONE),
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.HLS, EnigmaMediaFormat.DrmTechnology.NONE),
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.WIDEVINE),
                new EnigmaMediaFormat(EnigmaMediaFormat.StreamFormat.SMOOTHSTREAMING, EnigmaMediaFormat.DrmTechnology.PLAYREADY)
        );
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();
        preferenceList.putLast(format().dash().none());
        preferenceList.putLast(format().ss().none());
        preferenceList.putLast(format().hls().none());
        preferenceList.putLast(format().ss().widevine());

        assertOrder(preferenceList,
                format().dash().none(),
                format().ss().none(),
                format().hls().none(),
                format().ss().widevine()
        );

        preferenceList = preference.applyPreference(preferenceList);

        assertOrder(preferenceList,
                format().dash().none(),
                format().hls().none(),
                format().dash().widevine(),
                format().ss().playready(),
                format().ss().none(),
                format().ss().widevine()
        );
    }

    @Test
    public void testApplyReorder() {
        MediaFormatPreferenceSpec preference = new MediaFormatPreferenceSpec(
                format().hls().widevine(),
                format().ss().playready(),
                format().dash().fairplay(),
                format().hls().playready(),
                format().ss().none(),
                format().dash().none(),
                format().ss().widevine(),
                format().dash().playready(),
                format().ss().fairplay(),
                format().hls().fairplay(),
                format().dash().widevine(),
                format().hls().none()
        );


        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();
        preferenceList.putLast(format().dash().fairplay());
        preferenceList.putLast(format().dash().none());
        preferenceList.putLast(format().dash().playready());
        preferenceList.putLast(format().dash().widevine());
        preferenceList.putLast(format().hls().fairplay());
        preferenceList.putLast(format().hls().none());
        preferenceList.putLast(format().hls().playready());
        preferenceList.putLast(format().hls().widevine());
        preferenceList.putLast(format().ss().fairplay());
        preferenceList.putLast(format().ss().none());
        preferenceList.putLast(format().ss().playready());
        preferenceList.putLast(format().ss().widevine());

        assertOrder(preferenceList,
                format().dash().fairplay(),
                format().dash().none(),
                format().dash().playready(),
                format().dash().widevine(),
                format().hls().fairplay(),
                format().hls().none(),
                format().hls().playready(),
                format().hls().widevine(),
                format().ss().fairplay(),
                format().ss().none(),
                format().ss().playready(),
                format().ss().widevine()
        );

        preferenceList = preference.applyPreference(preferenceList);

        assertOrder(preferenceList,
                format().hls().widevine(),
                format().ss().playready(),
                format().dash().fairplay(),
                format().hls().playready(),
                format().ss().none(),
                format().dash().none(),
                format().ss().widevine(),
                format().dash().playready(),
                format().ss().fairplay(),
                format().hls().fairplay(),
                format().dash().widevine(),
                format().hls().none()
        );
    }

    @Test
    public void testBuildWithDuplication() {
        try {
            MediaFormatPreferenceSpec preferenceSpec = new MediaFormatPreferenceSpec(
                    format().hls().none(),
                    format().dash().none(),
                    format().hls().none(),
                    format().ss().widevine());
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testBuildWithNull() {
        try {
            MediaFormatPreferenceSpec preferenceSpec = new MediaFormatPreferenceSpec(
                    format().hls().none(),
                    format().dash().none(),
                    null,
                    format().ss().widevine());
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}
