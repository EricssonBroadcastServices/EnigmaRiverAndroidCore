package com.redbeemedia.enigma.core.exposure.models.tag;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.util.List;
import java.io.IOException;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiTag implements Parcelable {
    private String created;
    private List<ApiTagValues> tagValues;
    private String type;
    private String changed;


    private ApiTag() {}//Private constructor for Parcelable.Creator

    public ApiTag(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "created":
                    this.created = jsonReader.nextString();
                    break;
                case "tagValues":
                    this.tagValues = JsonReaderUtil.readArray(jsonReader, ApiTagValues.class);
                    break;
                case "type":
                    this.type = jsonReader.nextString();
                    break;
                case "changed":
                    this.changed = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getCreated() {
        return this.created;
    }

    public List<ApiTagValues> getTagValues() {
        return this.tagValues;
    }

    public String getType() {
        return this.type;
    }

    public String getChanged() {
        return this.changed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiTag> CREATOR = new Parcelable.Creator<ApiTag>() {
        public ApiTag createFromParcel(Parcel in) {
            ApiTag object = new ApiTag();
            object.created = in.readString();
            object.tagValues = in.createTypedArrayList(ApiTagValues.CREATOR);
            object.type = in.readString();
            object.changed = in.readString();
            return object;
        }

        public ApiTag[] newArray(int size) {
            return new ApiTag[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(created);
        dest.writeTypedList(tagValues);
        dest.writeString(type);
        dest.writeString(changed);
    }
}
