package com.redbeemedia.enigma.core.asset;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Collection;
import java.util.List;

@Deprecated
public class Localized implements Parcelable {

    private String locale;
    private String title;
    private String sortingTitle;
    private String description;
    private String tinyDescription;
    private String shortDescription;
    private String longDescription;
    private List<Image> images;

    public Localized() {}

    public String getLocale() {
        return locale;
    }

    public String getTitle() {
        return title;
    }

    public String getSortingTitle() {
        return sortingTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getTinyDescription() {
        return tinyDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSortingTitle(String sortingTitle) {
        this.sortingTitle = sortingTitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setTinyDescription(final String tinyDescription) {
        this.tinyDescription = tinyDescription;
    }

    public void setLongDescription(final String longDescription) {
        this.longDescription = longDescription;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public static Localized findFirstForm(final Asset asset) {
        final List<Localized> localizedList = asset.getLocalized();

        final Localized localized;
        if (localizedList != null && !localizedList.isEmpty()) {
            localized = localizedList.get(0);
        } else {
            localized = null;
        }

        return localized;
    }

    public static Localized findLocale(final String locale,
                                       final Asset asset)
    {
        return find(locale, asset.getLocalized());
    }

    public static Localized findCurrentFromItem(final String locale,
                                                final Asset asset,
                                                final Localized fallback)
    {
        final Localized foundLocalized = find(locale, asset.getLocalized());

        final Localized localized;
        if (foundLocalized == null) {
            localized = fallback;
        } else {
            localized = foundLocalized;
        }

        return localized;
    }

    private static Localized find(final String locale,
                                  final Collection<Localized> localized)
    {
        final Localized returnValue;
        if (localized != null) {
            Localized currentValue = null;
            for (final Localized currentLocalized : localized) {
                if (locale.equalsIgnoreCase(currentLocalized.getLocale())) {
                    currentValue = currentLocalized;
                    break;
                }
            }

            returnValue = currentValue;
        } else {
            returnValue = null;
        }

        return returnValue;
    }

    // <editor-fold description="PARCELABLE IMPLEMENTATION">
    protected Localized(final Parcel in)
    {
        locale = in.readString();
        title = in.readString();
        sortingTitle = in.readString();
        description = in.readString();
        shortDescription = in.readString();
        tinyDescription = in.readString();
        longDescription = in.readString();
        images = in.createTypedArrayList(Image.CREATOR);
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags)
    {
        dest.writeString(locale);
        dest.writeString(title);
        dest.writeString(sortingTitle);
        dest.writeString(description);
        dest.writeString(shortDescription);
        dest.writeString(tinyDescription);
        dest.writeString(longDescription);
        dest.writeTypedList(images);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Localized> CREATOR = new Creator<Localized>() {
        @Override
        public Localized createFromParcel(final Parcel in) {
            return new Localized(in);
        }

        @Override
        public Localized[] newArray(final int size) {
            return new Localized[size];
        }
    };
    // </editor-fold>
}
