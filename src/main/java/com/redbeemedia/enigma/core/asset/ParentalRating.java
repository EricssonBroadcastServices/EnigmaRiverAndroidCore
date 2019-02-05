package com.redbeemedia.enigma.core.asset;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

@Deprecated
public class ParentalRating implements Parcelable {

    private String country;
    private String scheme;
    private String rating;

    public ParentalRating() {}

    public String getCountry() {
        return country;
    }

    public String getScheme() {
        return scheme;
    }

    public String getRating() {
        return rating;
    }

    public static ParentalRating find(final Asset asset,
                                      final String countryCode)
    {
        final List<ParentalRating> ratings = asset.getParentalRatings();

        final ParentalRating rating;
        if (ratings != null) {

            ParentalRating currentRating = null;
            for (final ParentalRating nextRating : ratings) {
                if (countryCode.equalsIgnoreCase(nextRating.country)) {
                    currentRating = nextRating;
                    break;
                }
            }

            rating = currentRating;
        } else {
            rating = null;
        }

        return rating;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    // <editor-fold description="PARCELABLE IMPLEMENTATION">
    protected ParentalRating(final Parcel in) {
        country = in.readString();
        scheme = in.readString();
        rating = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(country);
        dest.writeString(scheme);
        dest.writeString(rating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ParentalRating> CREATOR = new Creator<ParentalRating>() {
        @Override
        public ParentalRating createFromParcel(final Parcel in) {
            return new ParentalRating(in);
        }

        @Override
        public ParentalRating[] newArray(final int size) {
            return new ParentalRating[size];
        }
    };
    // </editor-fold>
}
