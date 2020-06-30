package com.redbeemedia.enigma.core.format;

import org.junit.Assert;

import java.util.List;

/*package-protected*/ abstract class MediaFormatPreferenceTestUtil {

    /*package-protected*/ static void assertOrder(MediaFormatPreferenceList preferenceList, EnigmaMediaFormat ... order) {
        List<EnigmaMediaFormat> list = preferenceList.getList();
        Assert.assertEquals(order.length, list.size());
        for(int i = 0; i < order.length; ++i ) {
            Assert.assertEquals("Wrong format at position "+i,order[i], list.get(i));
        }
    }

    /*package-protected*/ static FormatBuilder_SelectFormat format() {
        return new FormatBuilder_SelectFormat();
    }

    /*package-protected*/ static class FormatBuilder_SelectFormat {
        /*package-protected*/ static class FormatBuilder_SelectDrmTech {
            private final EnigmaMediaFormat.StreamFormat streamFormat;

            public FormatBuilder_SelectDrmTech(EnigmaMediaFormat.StreamFormat streamFormat) {
                this.streamFormat = streamFormat;
            }

            private EnigmaMediaFormat build(EnigmaMediaFormat.DrmTechnology drmTechnology) {
                return new EnigmaMediaFormat(streamFormat, drmTechnology);
            }

            public EnigmaMediaFormat none() {
                return build(EnigmaMediaFormat.DrmTechnology.NONE);
            }

            public EnigmaMediaFormat widevine() {
                return build(EnigmaMediaFormat.DrmTechnology.WIDEVINE);
            }

            public EnigmaMediaFormat fairplay() {
                return build(EnigmaMediaFormat.DrmTechnology.FAIRPLAY);
            }

            public EnigmaMediaFormat playready() {
                return build(EnigmaMediaFormat.DrmTechnology.PLAYREADY);
            }
        }
        public FormatBuilder_SelectDrmTech dash() {
            return new FormatBuilder_SelectDrmTech(EnigmaMediaFormat.StreamFormat.DASH);
        }

        public FormatBuilder_SelectDrmTech hls() {
            return new FormatBuilder_SelectDrmTech(EnigmaMediaFormat.StreamFormat.HLS);
        }

        public FormatBuilder_SelectDrmTech ss() {
            return new FormatBuilder_SelectDrmTech(EnigmaMediaFormat.StreamFormat.SMOOTHSTREAMING);
        }
    }
}
