package com.redbeemedia.enigma.core.format;

import static com.redbeemedia.enigma.core.format.EnigmaMediaFormat.StreamFormat.SMOOTHSTREAMING;

import com.redbeemedia.enigma.core.testutil.json.JsonArrayBuilder;
import com.redbeemedia.enigma.core.testutil.json.JsonObjectBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EnigmaMediaFormatUtilTest {
    @Test
    public void testSelectUsableMediaFormat() throws JSONException {
        IMediaFormatSelector formatSelector = new IMediaFormatSelector() {
            @Override
            public EnigmaMediaFormat select(EnigmaMediaFormat prospect, Collection<EnigmaMediaFormat> available) {
                //Prefer smooth streaming
                for(EnigmaMediaFormat mediaFormat : available) {
                    if(mediaFormat.getStreamFormat() == SMOOTHSTREAMING) {
                        return mediaFormat;
                    }
                }
                //Then HLS
                for(EnigmaMediaFormat mediaFormat : available) {
                    if(mediaFormat.getStreamFormat() == EnigmaMediaFormat.StreamFormat.HLS) {
                        return mediaFormat;
                    }
                }
                //Then encrypted dash
                for(EnigmaMediaFormat mediaFormat : available) {
                    if(mediaFormat.equals(EnigmaMediaFormat.StreamFormat.DASH, EnigmaMediaFormat.DrmTechnology.WIDEVINE)) {
                        return mediaFormat;
                    }
                }
                return prospect;
            }
        };
        IMediaFormatSupportSpec formatSupportSpec = new IMediaFormatSupportSpec() {
            @Override
            public boolean supports(EnigmaMediaFormat enigmaMediaFormat) {
                //Support everything except smooth streaming
                return enigmaMediaFormat.getStreamFormat() != SMOOTHSTREAMING;
            }

            @Override
            public Set<EnigmaMediaFormat> getSupportedFormats() {
                Set<EnigmaMediaFormat> formatSet =  new HashSet<>();
                EnigmaMediaFormat format = new EnigmaMediaFormat(SMOOTHSTREAMING, EnigmaMediaFormat.DrmTechnology.NONE);
                formatSet.add(format);
                return formatSet;
            }

        };

        JsonArrayBuilder arrayBuilder = new JsonArrayBuilder();
        arrayBuilder.addObject().put("format", "SMOOTHSTREAMING");
        arrayBuilder.addObject().put("format", "DASH").putObject("drm");
        arrayBuilder.addObject().put("format", "DASH");
        arrayBuilder.addObject().put("format", "HLS");

        JSONObject selectedFormat
                = EnigmaMediaFormatUtil.selectUsableMediaFormat(arrayBuilder.getJsonArray(), formatSupportSpec, formatSelector);

        Assert.assertNotNull(selectedFormat);

        JsonObjectBuilder expectedFormat = new JsonObjectBuilder().put("format", "HLS");

        Assert.assertEquals(expectedFormat.toString(), selectedFormat.toString());

        //Adjust available formats

        arrayBuilder = new JsonArrayBuilder();
        arrayBuilder.addObject().put("format", "SMOOTHSTREAMING");
        arrayBuilder.addObject().put("format", "DASH")
                .putObject("drm").put(EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey(), "mock");
        arrayBuilder.addObject().put("format", "DASH");

        selectedFormat
                = EnigmaMediaFormatUtil.selectUsableMediaFormat(arrayBuilder.getJsonArray(), formatSupportSpec, formatSelector);

        Assert.assertNotNull(selectedFormat);

        expectedFormat = new JsonObjectBuilder().put("format", "DASH");
        expectedFormat.putObject("drm").put(EnigmaMediaFormat.DrmTechnology.WIDEVINE.getKey(), "mock");

        Assert.assertEquals(expectedFormat.toString(), selectedFormat.toString());

        //Adjust available formats

        arrayBuilder = new JsonArrayBuilder();
        arrayBuilder.addObject().put("format", "SMOOTHSTREAMING");
        arrayBuilder.addObject().put("format", "DASH");

        selectedFormat
                = EnigmaMediaFormatUtil.selectUsableMediaFormat(arrayBuilder.getJsonArray(), formatSupportSpec, formatSelector);

        Assert.assertNull(selectedFormat);
    }
}
