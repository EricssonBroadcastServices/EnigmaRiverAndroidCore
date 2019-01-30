package com.redbeemedia.enigma.core.exposure.models.linked;

import android.os.Parcel;
import android.util.JsonReader;
import android.os.Parcelable;
import java.io.IOException;


public class ApiLinkedEntity implements Parcelable {
    private String entityType;
    private String entityId;
    private String linkType;


    private ApiLinkedEntity() {}//Private constructor for Parcelable.Creator

    public ApiLinkedEntity(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "entityType":
                    this.entityType = jsonReader.nextString();
                    break;
                case "entityId":
                    this.entityId = jsonReader.nextString();
                    break;
                case "linkType":
                    this.linkType = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getEntityType() {
        return this.entityType;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public String getLinkType() {
        return this.linkType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiLinkedEntity> CREATOR = new Parcelable.Creator<ApiLinkedEntity>() {
        public ApiLinkedEntity createFromParcel(Parcel in) {
            ApiLinkedEntity object = new ApiLinkedEntity();
            object.entityType = in.readString();
            object.entityId = in.readString();
            object.linkType = in.readString();
            return object;
        }

        public ApiLinkedEntity[] newArray(int size) {
            return new ApiLinkedEntity[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entityType);
        dest.writeString(entityId);
        dest.writeString(linkType);
    }
}
