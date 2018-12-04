package com.redbeemedia.enigma.core.asset;

import android.util.JsonReader;

import java.io.IOException;

public class PublicationMapper implements JsonMapper<Publication> {

    private final Util mUtil;

    public PublicationMapper(final Util util) {
        this.mUtil = util;
    }

    @Override
    public Publication create() {
        return new Publication();
    }

    @Override
    public void map(final JsonReader jsonReader, final Publication publication, final int position) throws IOException {
        switch (jsonReader.nextName()) {
            case "publicationId":
                publication.setPublicationId(jsonReader.nextString());
                break;

            case "publicationDate":
                publication.setPublicationDate(jsonReader.nextString());
                break;

            case "fromDate":
                publication.setFromDate(jsonReader.nextString());
                break;

            case "toDate":
                publication.setToDate(jsonReader.nextString());
                break;

            case "countries":
                publication.setCountries(mUtil.mapStrings(jsonReader));
                break;

            case "products":
                publication.setProducts(mUtil.mapStrings(jsonReader));
                break;

            case "services":
                publication.setServices(mUtil.mapStrings(jsonReader));
                break;

            case "customData":
            default:
                jsonReader.skipValue();
        }
    }

    @Override
    public Publication onMapComplete(final Publication publication,
                                     final int position)
    {
        return publication;
    }
}
