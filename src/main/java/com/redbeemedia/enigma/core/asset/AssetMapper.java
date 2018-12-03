package com.redbeemedia.enigma.core.asset;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;

public class AssetMapper implements JsonMapper<Asset> {

    private final JsonMapper.Util util = new Util();

    private final JsonMapper<Localized> mLocalizedMapper;

    public AssetMapper() {
        this(new LocalizedMapper());
    }

    public AssetMapper(final JsonMapper<Localized> localizedMapper)
    {
        mLocalizedMapper = localizedMapper;
    }

    @Override
    public Asset create() {
        return new Asset();
    }

    @Override
    public void map(final JsonReader jsonReader, final Asset asset, final int position) throws IOException
    {
        switch (jsonReader.nextName()) {
            case "created":
                asset.setCreated(jsonReader.nextString());
                break;

            case "changed":
                asset.setChanged(jsonReader.nextString());
                break;

            case "assetId":
                asset.setAssetId(jsonReader.nextString());
                break;

            case "type":
                asset.setType(jsonReader.nextString());
                break;

            case "localized":
                asset.setLocalized(util.mapList(jsonReader, mLocalizedMapper));
                break;

            case "tags":
                asset.setTags(util.mapList(jsonReader, new TagMapper()));
                break;

            case "publications":
                asset.setPublications(util.mapList(jsonReader, new PublicationMapper(util)));
                break;

            case "participants":
                asset.setParticipants(util.mapList(jsonReader, new ParticipantMapper()));
                break;

            case "productionYear":
                asset.setProductionYear(jsonReader.nextInt());
                break;

            case "originalTitle":
                asset.setOriginalTitle(jsonReader.nextString());
                break;

            case "live":
                asset.setLive(jsonReader.nextBoolean());
                break;

            case "productionCountries":
                asset.setProductionCountries(util.mapStrings(jsonReader));
                break;

            case "subtitles":
                asset.setSubtitles(util.mapStrings(jsonReader));
                break;

            case "audioTracks":
                asset.setAudioTracks(util.mapStrings(jsonReader));
                break;

            case "spokenLanguages":
                asset.setSpokenLanguages(util.mapStrings(jsonReader));
                break;

            case "medias":
                asset.setMedias(util.mapList(jsonReader, new MediaMapper()));
                break;

            case "parentalRatings":
                asset.setParentalRatings(util.mapList(jsonReader, new ParentalRatingMapper()));
                break;

            case "linkedEntities":
                asset.setLinkedEntities(util.mapStrings(jsonReader));
                break;

            case "runtime":
                asset.setRuntime(jsonReader.nextInt());
                break;

            case "externalReferences":
                asset.setExternalReferences(util.mapStrings(jsonReader));
                break;

            case "rating":
                asset.setRating(jsonReader.nextDouble());
                break;

            case "markers":
                asset.setMarkers(util.mapStrings(jsonReader));
                break;

            case "tvShowId":
                asset.setTvShowId(jsonReader.nextString());
                break;

            case "season":
                asset.setSeason(jsonReader.nextString());
                break;

            case "episode":
                asset.setEpisode(jsonReader.nextString());
                break;

            case "seasonId":
                asset.setSeasonId(jsonReader.nextString());
                break;

            case "customData":
            default:
                jsonReader.skipValue();
        }
    }

    @Override
    public Asset onMapComplete(final Asset asset, final int position) {
        return asset;
    }

    // Can be used with search API
    public static class Wrapped implements JsonMapper<Asset> {

        private final AssetMapper mAssetMapper;

        public Wrapped() {
            this.mAssetMapper = new AssetMapper();
        }

        @Override
        public Asset create() {
            return mAssetMapper.create();
        }

        @Override
        public void map(final JsonReader jsonReader, final Asset asset, final int position) throws IOException {
            if (jsonReader.nextName().equals("asset")) {
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    mAssetMapper.map(jsonReader, asset, position);
                }
                jsonReader.endObject();

                mAssetMapper.onMapComplete(asset, position);
            } else {
                jsonReader.skipValue();
            }
        }

        @Override
        public Asset onMapComplete(final Asset asset,
                                   final int position)
        {
            return asset;
        }
    }
}
