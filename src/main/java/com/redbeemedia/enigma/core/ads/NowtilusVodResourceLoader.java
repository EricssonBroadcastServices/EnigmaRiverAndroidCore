// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import org.json.JSONException;
import org.json.JSONObject;

/** Responsible of returning ads metadata for a vod json object. */
class NowtilusVodResourceLoader implements IAdResourceLoader {

    private JSONObject adsInfo;
    private final INowtilusParser parser;

    NowtilusVodResourceLoader(INowtilusParser parser) {
        this.parser = parser;
    }

    void setJson(JSONObject adsInfo) {
        this.adsInfo = adsInfo;
    }

    @Override
    public void load(IAdsResourceLoaderDelegate delegate) {
        try {
            delegate.onEntriesLoaded(parser.parseEntries(adsInfo));
        } catch(JSONException e) { e.printStackTrace(); }
    }
}
