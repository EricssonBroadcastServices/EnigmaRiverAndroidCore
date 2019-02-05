package com.redbeemedia.enigma.core.asset;

import android.util.JsonReader;

import java.io.IOException;

@Deprecated
public class MediaMapper implements JsonMapper<Media> {

    @Override
    public Media create() {
        return new Media();
    }

    @Override
    public void map(final JsonReader jsonReader, final Media media, final int position) throws IOException {
        switch (jsonReader.nextName()) {
            case "mediaId":
                media.setMediaId(jsonReader.nextString());
                break;

            case "drm":
                media.setDrm(jsonReader.nextString());
                break;

            case "format":
                media.setFormat(jsonReader.nextString());
                break;

            case "durationMillis":
                media.setDurationMillis(jsonReader.nextLong());
                break;

            case "status":
                media.setStatus(jsonReader.nextString());
                break;

            default:
                jsonReader.skipValue();
        }
    }

    @Override
    public Media onMapComplete(final Media media,
                               final int position)
    {
        return media;
    }
}
