// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.format;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EnigmaMediaFormatUtil {
    public static JSONObject selectUsableMediaFormat(JSONArray formats, IMediaFormatSupportSpec formatSupportSpec, IMediaFormatSelector mediaFormatSelector) throws JSONException {
        Map<EnigmaMediaFormat, JSONObject> foundFormats = new HashMap<>();
        for(int i = 0; i < formats.length(); ++i) {
            JSONObject mediaFormat = formats.getJSONObject(i);
            EnigmaMediaFormat enigmaMediaFormat = EnigmaMediaFormat.parseMediaFormat(mediaFormat);
            if(enigmaMediaFormat != null) {
                if(formatSupportSpec.supports(enigmaMediaFormat)) {
                    foundFormats.put(enigmaMediaFormat, mediaFormat);
                }
            }
        }

        EnigmaMediaFormat selection = mediaFormatSelector.select(null, foundFormats.keySet());
        if(selection != null) {
            JSONObject object = foundFormats.get(selection);
            if(object != null) {
                return object;
            }
        }

        return null;//If no format was picked we can't decide.
    }
}
