package com.redbeemedia.enigma.core.entitlement;

import android.util.JsonReader;

import com.redbeemedia.enigma.core.asset.JsonMapper;

import java.io.IOException;


public class FormatMapper implements JsonMapper<Format> {

    @Override
    public Format create() {
        return new Format();
    }

    @Override
    public void map(final JsonReader jsonReader, final Format format, final int position) throws IOException {
        switch (jsonReader.nextName()) {
            case "format":
                format.setFormat(jsonReader.nextString());
                break;

            case "mediaLocator":
                format.setMediaLocator(jsonReader.nextString());
                break;


            default:
                jsonReader.skipValue();
        }
    }

    @Override
    public Format onMapComplete(final Format format,
                               final int position)
    {
        return format;
    }
}
