package com.redbeemedia.enigma.core.asset;

import android.util.JsonReader;

import java.io.IOException;

@Deprecated
public class ParentalRatingMapper implements JsonMapper<ParentalRating> {

    @Override
    public ParentalRating create() {
        return new ParentalRating();
    }

    @Override
    public void map(final JsonReader jsonReader, final ParentalRating parentalRating, final int position) throws IOException {
        switch (jsonReader.nextName()) {
            case "scheme":
                parentalRating.setScheme(jsonReader.nextString());
                break;

            case "country":
                parentalRating.setCountry(jsonReader.nextString());
                break;

            case "rating":
                parentalRating.setRating(jsonReader.nextString());
                break;

            default:
                jsonReader.skipValue();
        }
    }

    @Override
    public ParentalRating onMapComplete(final ParentalRating parentalRating,
                              final int position)
    {
        return parentalRating;
    }
}
