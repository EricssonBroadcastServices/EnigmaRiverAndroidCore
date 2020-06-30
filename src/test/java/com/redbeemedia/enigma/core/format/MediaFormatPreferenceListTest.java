package com.redbeemedia.enigma.core.format;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MediaFormatPreferenceListTest extends MediaFormatPreferenceTestUtil {
    @Test
    public void testPutFirst() {
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceList.getList().isEmpty());

        preferenceList.putFirst(format().hls().none());
        preferenceList.putFirst(format().hls().fairplay());


        assertOrder(preferenceList,
                format().hls().fairplay(),
                format().hls().none()
        );

        preferenceList.putFirst(format().dash().widevine());
        preferenceList.putFirst(format().hls().none()); // Should move unenc HLS to first

        assertOrder(preferenceList,
                format().hls().none(),
                format().dash().widevine(),
                format().hls().fairplay()
        );
    }

    @Test
    public void testPutLast() {
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceList.getList().isEmpty());

        preferenceList.putLast(format().hls().none());
        preferenceList.putLast(format().ss().none());

        assertOrder(preferenceList,
                format().hls().none(),
                format().ss().none()
        );

        preferenceList.putLast(format().dash().widevine());
        preferenceList.putLast(format().ss().none()); // Should move unenc smoothstreaming to last

        assertOrder(preferenceList,
                format().hls().none(),
                format().dash().widevine(),
                format().ss().none()
        );
    }

    @Test
    public void testPutIndex() {
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceList.getList().isEmpty());

        try {
            preferenceList.put(1, format().hls().none());
            Assert.fail("Expected exception");
        } catch (Exception e) {
            //Expected
        }
        Assert.assertTrue(preferenceList.getList().isEmpty());
        preferenceList.put(0, format().hls().none());
        preferenceList.put(1, format().dash().widevine());

        assertOrder(preferenceList,
                format().hls().none(),
                format().dash().widevine()
        );

        preferenceList.put(1, format().dash().none());
        preferenceList.put(-1, format().ss().none()); // Should put unenc smoothstreaming to last

        assertOrder(preferenceList,
                format().hls().none(),
                format().dash().none(),
                format().dash().widevine(),
                format().ss().none()
        );

        preferenceList.put(-1, format().hls().none());
        preferenceList.put(-1, format().dash().widevine());

        assertOrder(preferenceList,
                format().dash().none(),
                format().ss().none(),
                format().hls().none(),
                format().dash().widevine()
        );

        preferenceList.put(2, format().ss().none());

        assertOrder(preferenceList,
                format().dash().none(),
                format().ss().none(),
                format().hls().none(),
                format().dash().widevine()
        );

        preferenceList.put(3, format().ss().none());

        assertOrder(preferenceList,
                format().dash().none(),
                format().hls().none(),
                format().ss().none(),
                format().dash().widevine()
        );

        preferenceList.put(1, format().ss().none());

        assertOrder(preferenceList,
                format().dash().none(),
                format().ss().none(),
                format().hls().none(),
                format().dash().widevine()
        );
    }

    @Test
    public void testEqualsSimple() {
        MediaFormatPreferenceList preferenceListA = new MediaFormatPreferenceList();
        MediaFormatPreferenceList preferenceListB = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceListA.equals(preferenceListB));

        preferenceListA.putLast(format().dash().widevine());
        preferenceListA.putLast(format().dash().none());
        preferenceListA.putLast(format().hls().none());

        preferenceListB.putLast(format().dash().widevine());
        preferenceListB.putLast(format().dash().none());
        preferenceListB.putLast(format().hls().none());

        Assert.assertTrue(preferenceListA.equals(preferenceListB));
    }

    @Test
    public void testEqualsComplexInsertion() {
        MediaFormatPreferenceList preferenceListA = new MediaFormatPreferenceList();
        MediaFormatPreferenceList preferenceListB = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceListA.equals(preferenceListB));

        preferenceListA.put(0, format().dash().playready());
        preferenceListA.putFirst(format().hls().playready());
        preferenceListA.put(-1, format().hls().none());
        preferenceListA.put(1, format().ss().playready());
        preferenceListA.putLast(format().ss().widevine());
        preferenceListA.putLast(format().dash().none());

        assertOrder(preferenceListA,
                format().hls().playready(),
                format().ss().playready(),
                format().dash().playready(),
                format().hls().none(),
                format().ss().widevine(),
                format().dash().none());

        Assert.assertFalse(preferenceListA.equals(preferenceListB));

        preferenceListB.putLast(format().ss().widevine());
        preferenceListB.putLast(format().ss().playready());
        preferenceListB.putLast(format().dash().none());
        preferenceListB.putFirst(format().ss().playready());
        preferenceListB.putFirst(format().dash().playready());
        preferenceListB.put(1, format().hls().none());
        preferenceListB.put(0, format().hls().playready());
        preferenceListB.put(1, format().ss().playready());

        assertOrder(preferenceListB,
                format().hls().playready(),
                format().ss().playready(),
                format().dash().playready(),
                format().hls().none(),
                format().ss().widevine(),
                format().dash().none());

        Assert.assertTrue(preferenceListA.equals(preferenceListB));
    }

    @Test
    public void testInsertIntoExisting() {
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();

        preferenceList.put(0, format().dash().playready());
        preferenceList.putFirst(format().hls().playready());
        preferenceList.put(-1, format().hls().none());
        preferenceList.put(1, format().ss().playready());
        preferenceList.putLast(format().ss().widevine());
        preferenceList.putLast(format().dash().none());

        assertOrder(preferenceList,
                format().hls().playready(),
                format().ss().playready(),
                format().dash().playready(),
                format().hls().none(),
                format().ss().widevine(),
                format().dash().none());

        preferenceList.put(0, format().ss().widevine());
        preferenceList.put(1, format().dash().playready());
        preferenceList.put(2, format().dash().none());
        preferenceList.put(3, format().hls().playready());

        assertOrder(preferenceList,
                format().ss().widevine(),
                format().dash().playready(),
                format().dash().none(),
                format().hls().playready(),
                format().ss().playready(),
                format().hls().none()
        );
    }
}
