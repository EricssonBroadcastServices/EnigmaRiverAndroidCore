package com.redbeemedia.enigma.core.asset;

import android.util.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class LocalizedMapper implements JsonMapper<Localized> {

    private ImageMapper mImageMapper;

    public LocalizedMapper() {
        this(new ImageMapper());
    }

    public LocalizedMapper(final ImageMapper imageMapper) {
        this.mImageMapper = imageMapper;
    }

    @Override
    public Localized create() {
        return new Localized();
    }

    @Override
    public void map(final JsonReader jsonReader,
                    final Localized localized,
                    final int position)
            throws IOException
    {
        switch (jsonReader.nextName()) {

            case "locale":
                localized.setLocale(jsonReader.nextString());
                break;

            case "title":
                localized.setTitle(jsonReader.nextString());
                break;

            case "sortingTitle":
                localized.setSortingTitle(jsonReader.nextString());
                break;

            case "description":
                localized.setDescription(jsonReader.nextString());
                break;

            case "shortDescription":
                localized.setShortDescription(jsonReader.nextString());
                break;

            case "tinyDescription":
                localized.setTinyDescription(jsonReader.nextString());
                break;

            case "longDescription":
                localized.setLongDescription(jsonReader.nextString());
                break;

            case "images":
                final List<Image> images = new Util().mapList(jsonReader, mImageMapper);
                localized.setImages(images);
                break;

            default:
                jsonReader.skipValue();
        }
    }

    @Override
    public Localized onMapComplete(final Localized localized, final int position) {
        return localized;
    }

    public static class EpisodesLocalizedMapper implements JsonMapper<Localized> {

        private final Asset mTvShow;
        private final LocalizedMapper mMapper;

        public EpisodesLocalizedMapper(final Asset tvShow) {
            this.mTvShow = tvShow;
            this.mMapper = new LocalizedMapper();
        }

        @Override
        public Localized create() {
            return mMapper.create();
        }

        @Override
        public void map(final JsonReader jsonReader,
                        final Localized localized,
                        final int position)
                throws IOException
        {
            mMapper.map(jsonReader, localized, position);
        }

        @Override
        public Localized onMapComplete(final Localized localized,
                                       final int position)
        {
            final List<Image> images = localized.getImages();

            final List<Image> newList;
            if (images != null && !images.isEmpty()) {
                final Image portrait = setupImage(localized, Image.PORTRAIT_ORIENTATION, mTvShow);
                final Image landscape = setupImage(localized, Image.LANDSCAPE_ORIENTATION, mTvShow);

                final ArrayList<Image> list = new ArrayList<>(2);
                list.add(portrait);
                list.add(landscape);

                newList = list;
            } else {
                final Localized tvShowLocalized = Localized.findLocale(localized.getLocale(), mTvShow);
                if (tvShowLocalized != null) {
                    newList = tvShowLocalized.getImages();
                } else {
                    newList = null;
                }
            }

            localized.setImages(newList);

            return localized;
        }

        private Image setupImage(final Localized localized,
                                 final String orientation,
                                 final Asset tvShow)
        {
            final Image testImage = Image.from(localized, orientation);

            final Image image;
            if (testImage == null) {
                final Localized tvShowLocalized = Localized.findLocale(localized.getLocale(), tvShow);
                if (tvShowLocalized != null) {
                    image = Image.from(tvShowLocalized, orientation);
                } else {
                    image = null;
                }
            } else {
                image = testImage;
            }

            return image;
        }
    }

    public static class ImageMapper implements JsonMapper<Image> {

        @Override
        public Image create() {
            return new Image();
        }

        @Override
        public void map(final JsonReader jsonReader, final Image image, final int position) throws IOException {
            switch (jsonReader.nextName()) {
                case "url":
                    final String url = jsonReader.nextString();
                    image.setUrl(url);
                    break;

                case "height":
                    image.setHeight(jsonReader.nextInt());
                    break;

                case "width":
                    image.setWidth(jsonReader.nextInt());
                    break;

                case "orientation":
                    image.setOrientation(jsonReader.nextString());
                    break;

                case "type":
                    image.setType(jsonReader.nextString());
                    break;

                default:
                    jsonReader.skipValue();
            }
        }

        @Override
        public Image onMapComplete(final Image image, final int position)
        {
            return image;
        }
    }
}
