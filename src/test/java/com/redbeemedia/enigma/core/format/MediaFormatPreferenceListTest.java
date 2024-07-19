// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.format;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MediaFormatPreferenceListTest {
    @Test
    public void testPutFirst() {
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceList.getList().isEmpty());

        preferenceList.putFirst(EnigmaMediaFormat.HLS().unenc());
        preferenceList.putFirst(EnigmaMediaFormat.HLS().fairplay());


        assertOrder(preferenceList,
                EnigmaMediaFormat.HLS().fairplay(),
                EnigmaMediaFormat.HLS().unenc()
        );

        preferenceList.putFirst(EnigmaMediaFormat.DASH().widevine());
        preferenceList.putFirst(EnigmaMediaFormat.HLS().unenc()); // Should move unenc HLS to first

        assertOrder(preferenceList,
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.HLS().fairplay()
        );
    }

    @Test
    public void testPutLast() {
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceList.getList().isEmpty());

        preferenceList.putLast(EnigmaMediaFormat.HLS().unenc());
        preferenceList.putLast(EnigmaMediaFormat.SMOOTHSTREAMING().unenc());

        assertOrder(preferenceList,
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc()
        );

        preferenceList.putLast(EnigmaMediaFormat.DASH().widevine());
        preferenceList.putLast(EnigmaMediaFormat.SMOOTHSTREAMING().unenc()); // Should move unenc smoothstreaming to last

        assertOrder(preferenceList,
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc()
        );
    }

    @Test
    public void testPutIndex() {
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceList.getList().isEmpty());

        try {
            preferenceList.put(1, EnigmaMediaFormat.HLS().unenc());
            Assert.fail("Expected exception");
        } catch (Exception e) {
            //Expected
        }
        Assert.assertTrue(preferenceList.getList().isEmpty());
        preferenceList.put(0, EnigmaMediaFormat.HLS().unenc());
        preferenceList.put(1, EnigmaMediaFormat.DASH().widevine());

        assertOrder(preferenceList,
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.DASH().widevine()
        );

        preferenceList.put(1, EnigmaMediaFormat.DASH().unenc());
        preferenceList.put(-1, EnigmaMediaFormat.SMOOTHSTREAMING().unenc()); // Should put unenc smoothstreaming to last

        assertOrder(preferenceList,
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc()
        );

        preferenceList.put(-1, EnigmaMediaFormat.HLS().unenc());
        preferenceList.put(-1, EnigmaMediaFormat.DASH().widevine());

        assertOrder(preferenceList,
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.DASH().widevine()
        );

        preferenceList.put(2, EnigmaMediaFormat.SMOOTHSTREAMING().unenc());

        assertOrder(preferenceList,
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.DASH().widevine()
        );

        preferenceList.put(3, EnigmaMediaFormat.SMOOTHSTREAMING().unenc());

        assertOrder(preferenceList,
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine()
        );

        preferenceList.put(1, EnigmaMediaFormat.SMOOTHSTREAMING().unenc());

        assertOrder(preferenceList,
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.DASH().widevine()
        );
    }

    @Test
    public void testEqualsSimple() {
        MediaFormatPreferenceList preferenceListA = new MediaFormatPreferenceList();
        MediaFormatPreferenceList preferenceListB = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceListA.equals(preferenceListB));

        preferenceListA.putLast(EnigmaMediaFormat.DASH().widevine());
        preferenceListA.putLast(EnigmaMediaFormat.DASH().unenc());
        preferenceListA.putLast(EnigmaMediaFormat.HLS().unenc());

        preferenceListB.putLast(EnigmaMediaFormat.DASH().widevine());
        preferenceListB.putLast(EnigmaMediaFormat.DASH().unenc());
        preferenceListB.putLast(EnigmaMediaFormat.HLS().unenc());

        Assert.assertTrue(preferenceListA.equals(preferenceListB));
    }

    @Test
    public void testEqualsComplexInsertion() {
        MediaFormatPreferenceList preferenceListA = new MediaFormatPreferenceList();
        MediaFormatPreferenceList preferenceListB = new MediaFormatPreferenceList();

        Assert.assertTrue(preferenceListA.equals(preferenceListB));

        preferenceListA.put(0, EnigmaMediaFormat.DASH().playready());
        preferenceListA.putFirst(EnigmaMediaFormat.HLS().playready());
        preferenceListA.put(-1, EnigmaMediaFormat.HLS().unenc());
        preferenceListA.put(1, EnigmaMediaFormat.SMOOTHSTREAMING().playready());
        preferenceListA.putLast(EnigmaMediaFormat.SMOOTHSTREAMING().widevine());
        preferenceListA.putLast(EnigmaMediaFormat.DASH().unenc());

        assertOrder(preferenceListA,
                EnigmaMediaFormat.HLS().playready(),
                EnigmaMediaFormat.SMOOTHSTREAMING().playready(),
                EnigmaMediaFormat.DASH().playready(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().widevine(),
                EnigmaMediaFormat.DASH().unenc());

        Assert.assertFalse(preferenceListA.equals(preferenceListB));

        preferenceListB.putLast(EnigmaMediaFormat.SMOOTHSTREAMING().widevine());
        preferenceListB.putLast(EnigmaMediaFormat.SMOOTHSTREAMING().playready());
        preferenceListB.putLast(EnigmaMediaFormat.DASH().unenc());
        preferenceListB.putFirst(EnigmaMediaFormat.SMOOTHSTREAMING().playready());
        preferenceListB.putFirst(EnigmaMediaFormat.DASH().playready());
        preferenceListB.put(1, EnigmaMediaFormat.HLS().unenc());
        preferenceListB.put(0, EnigmaMediaFormat.HLS().playready());
        preferenceListB.put(1, EnigmaMediaFormat.SMOOTHSTREAMING().playready());

        assertOrder(preferenceListB,
                EnigmaMediaFormat.HLS().playready(),
                EnigmaMediaFormat.SMOOTHSTREAMING().playready(),
                EnigmaMediaFormat.DASH().playready(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().widevine(),
                EnigmaMediaFormat.DASH().unenc());

        Assert.assertTrue(preferenceListA.equals(preferenceListB));
    }

    @Test
    public void testInsertIntoExisting() {
        MediaFormatPreferenceList preferenceList = new MediaFormatPreferenceList();

        preferenceList.put(0, EnigmaMediaFormat.DASH().playready());
        preferenceList.putFirst(EnigmaMediaFormat.HLS().playready());
        preferenceList.put(-1, EnigmaMediaFormat.HLS().unenc());
        preferenceList.put(1, EnigmaMediaFormat.SMOOTHSTREAMING().playready());
        preferenceList.putLast(EnigmaMediaFormat.SMOOTHSTREAMING().widevine());
        preferenceList.putLast(EnigmaMediaFormat.DASH().unenc());

        assertOrder(preferenceList,
                EnigmaMediaFormat.HLS().playready(),
                EnigmaMediaFormat.SMOOTHSTREAMING().playready(),
                EnigmaMediaFormat.DASH().playready(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().widevine(),
                EnigmaMediaFormat.DASH().unenc());

        preferenceList.put(0, EnigmaMediaFormat.SMOOTHSTREAMING().widevine());
        preferenceList.put(1, EnigmaMediaFormat.DASH().playready());
        preferenceList.put(2, EnigmaMediaFormat.DASH().unenc());
        preferenceList.put(3, EnigmaMediaFormat.HLS().playready());

        assertOrder(preferenceList,
                EnigmaMediaFormat.SMOOTHSTREAMING().widevine(),
                EnigmaMediaFormat.DASH().playready(),
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.HLS().playready(),
                EnigmaMediaFormat.SMOOTHSTREAMING().playready(),
                EnigmaMediaFormat.HLS().unenc()
        );
    }

    private static void assertOrder(MediaFormatPreferenceList preferenceList, EnigmaMediaFormat ... order) {
        List<EnigmaMediaFormat> list = preferenceList.getList();
        Assert.assertEquals(order.length, list.size());
        for(int i = 0; i < order.length; ++i ) {
            Assert.assertEquals("Wrong format at position "+i,order[i], list.get(i));
        }
    }
}
