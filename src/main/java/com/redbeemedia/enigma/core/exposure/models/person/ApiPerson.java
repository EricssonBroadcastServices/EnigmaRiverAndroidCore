package com.redbeemedia.enigma.core.exposure.models.person;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiPerson implements Parcelable {
    private String function;
    private String name;
    private String personId;


    protected ApiPerson() {}//Protected constructor for Parcelable.Creator and Mocks

    public ApiPerson(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "function":
                    this.function = jsonReader.nextString();
                    break;
                case "name":
                    this.name = jsonReader.nextString();
                    break;
                case "personId":
                    this.personId = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getFunction() {
        return this.function;
    }

    public String getName() {
        return this.name;
    }

    public String getPersonId() {
        return this.personId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiPerson> CREATOR = new Parcelable.Creator<ApiPerson>() {
        public ApiPerson createFromParcel(Parcel in) {
            ApiPerson object = new ApiPerson();
            object.function = in.readString();
            object.name = in.readString();
            object.personId = in.readString();
            return object;
        }

        public ApiPerson[] newArray(int size) {
            return new ApiPerson[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(function);
        dest.writeString(name);
        dest.writeString(personId);
    }
}
