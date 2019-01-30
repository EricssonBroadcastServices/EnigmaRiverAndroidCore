package com.redbeemedia.enigma.core.asset;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

@Deprecated
public class Image implements Parcelable {

    public static final String PORTRAIT_ORIENTATION = "PORTRAIT";
    public static final String LANDSCAPE_ORIENTATION = "LANDSCAPE";

    private String url;
    private int height;
    private int width;
    private String orientation;
    private String type;

    public static Image from(final Localized localized,
                             final String orientation)
    {
        final List<Image> images = localized.getImages();
        final Image image;
        if (images != null && !images.isEmpty() && orientation != null) {

            Image currentImage = null;
            for (final Image aImage : images) {
                if (orientation.equals(aImage.getOrientation())) {
                    currentImage = aImage;
                    break;
                }
            }

            image = currentImage;
        } else {
            image = null;
        }

        return image;
    }

    public static Image localizedWithFallbackFrom(final String locale,
                                                  final Asset asset,
                                                  final String orientation)
    {
        final List<Localized> localizedList = asset.getLocalized();

        final Image returnImage;
        if (localizedList != null && !localizedList.isEmpty()) {
            final Localized firstLocalized = localizedList.get(0);

            final Localized localized = Localized.findCurrentFromItem(locale, asset, firstLocalized);

            returnImage = from(localized, orientation);
        } else {
            returnImage = null;
        }

        return returnImage;
    }

    public static Image portraitFrom(final Localized localized) {
        return from(localized, PORTRAIT_ORIENTATION);
    }

    public static Image landscapeFrom(final Localized localized) {
        return from(localized, LANDSCAPE_ORIENTATION);
    }

    public Image() {}

    public String getUrl() {
        return url;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getType() {
        return type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public void setType(String type) {
        this.type = type;
    }

    // <editor-fold description="PARCELABLE IMPLEMENTATION">
    protected Image(final Parcel in) {
        url = in.readString();
        height = in.readInt();
        width = in.readInt();
        orientation = in.readString();
        type = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(url);
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeString(orientation);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(final Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(final int size) {
            return new Image[size];
        }
    };
    // </editor-fold>
}
