package com.redbeemedia.enigma.core.format;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class SimpleMediaFormatSelectorTest {
    @Test
    public void testSelectsIfPresent() {
        SimpleMediaFormatSelector selector = new SimpleMediaFormatSelector(
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine(),
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.DASH().fairplay()
        );

        EnigmaMediaFormat selected = selector.select(null, Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine()
        ));
        Assert.assertEquals(EnigmaMediaFormat.HLS().unenc(), selected);


        selected = selector.select(EnigmaMediaFormat.DASH().widevine(), Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine()
        ));
        Assert.assertEquals(EnigmaMediaFormat.HLS().unenc(), selected);


        selected = selector.select(null, Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay(),
                EnigmaMediaFormat.HLS().widevine()
        ));
        Assert.assertEquals(EnigmaMediaFormat.HLS().widevine(), selected);


        selected = selector.select(EnigmaMediaFormat.HLS().widevine(), Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay()
        ));
        Assert.assertEquals(EnigmaMediaFormat.DASH().unenc(), selected);


        selected = selector.select(null, Arrays.asList(
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay()
        ));
        Assert.assertEquals(EnigmaMediaFormat.DASH().fairplay(), selected);


        selected = selector.select(null, Arrays.asList(
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.HLS().fairplay()
        ));
        Assert.assertEquals(null, selected);


        selected = selector.select(EnigmaMediaFormat.HLS().fairplay(), Arrays.asList(
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.HLS().fairplay()
        ));
        Assert.assertEquals(EnigmaMediaFormat.HLS().fairplay(), selected);


        selected = selector.select(EnigmaMediaFormat.SMOOTHSTREAMING().unenc(), Arrays.asList(
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.HLS().fairplay()
        ));
        Assert.assertEquals(EnigmaMediaFormat.SMOOTHSTREAMING().unenc(), selected);
    }

    @Test
    public void testPreferenceListChange() {
        SimpleMediaFormatSelector selector = new SimpleMediaFormatSelector(
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine(),
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.DASH().fairplay()
        );

        EnigmaMediaFormat selected = null;

        selected = selector.select(selected, Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine()
        ));
        Assert.assertEquals(EnigmaMediaFormat.HLS().unenc(), selected);


        selector.getPreferenceList().putLast(EnigmaMediaFormat.HLS().unenc());
        selected = selector.select(selected, Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine()
        ));
        Assert.assertEquals(EnigmaMediaFormat.HLS().widevine(), selected);
        selected = selector.select(selected, Arrays.asList(
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.HLS().unenc()
        ));
        Assert.assertEquals(EnigmaMediaFormat.HLS().unenc(), selected);


        selector.getPreferenceList().putFirst(EnigmaMediaFormat.DASH().unenc());
        selected = selector.select(selected, Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine()
        ));
        Assert.assertEquals(EnigmaMediaFormat.DASH().unenc(), selected);
    }

    @Test
    public void testEmpty() {
        SimpleMediaFormatSelector selector = new SimpleMediaFormatSelector();
        Assert.assertEquals(0, selector.getPreferenceList().getList().size());

        EnigmaMediaFormat selected = selector.select(null, Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine()
        ));
        Assert.assertEquals(null, selected);


        selected = selector.select(EnigmaMediaFormat.DASH().fairplay(), Arrays.asList(
                EnigmaMediaFormat.DASH().unenc(),
                EnigmaMediaFormat.SMOOTHSTREAMING().unenc(),
                EnigmaMediaFormat.DASH().widevine(),
                EnigmaMediaFormat.DASH().fairplay(),
                EnigmaMediaFormat.HLS().unenc(),
                EnigmaMediaFormat.HLS().widevine()
        ));
        Assert.assertEquals(EnigmaMediaFormat.DASH().fairplay(), selected);
    }
}
