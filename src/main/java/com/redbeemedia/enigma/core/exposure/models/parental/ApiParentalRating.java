package com.redbeemedia.enigma.core.exposure.models.parental;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiParentalRating implements Parcelable {
    private String country;
    private String scheme;
    private String rating;


    protected ApiParentalRating() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiParentalRating(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "country":
                    this.country = jsonReader.nextString();
                    break;
                case "scheme":
                    this.scheme = jsonReader.nextString();
                    break;
                case "rating":
                    this.rating = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getCountry() {
        return this.country;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String getRating() {
        return this.rating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiParentalRating> CREATOR = new Parcelable.Creator<ApiParentalRating>() {
        public ApiParentalRating createFromParcel(Parcel in) {
            ApiParentalRating object = new ApiParentalRating();
            object.country = in.readString();
            object.scheme = in.readString();
            object.rating = in.readString();
            return object;
        }

        public ApiParentalRating[] newArray(int size) {
            return new ApiParentalRating[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(country);
        dest.writeString(scheme);
        dest.writeString(rating);
    }
}
