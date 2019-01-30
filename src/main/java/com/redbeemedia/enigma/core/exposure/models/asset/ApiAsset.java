package com.redbeemedia.enigma.core.exposure.models.asset;

import com.redbeemedia.enigma.core.exposure.models.linked.ApiLinkedEntity;
import com.redbeemedia.enigma.core.exposure.models.tag.ApiTag;
import android.os.Parcel;
import android.os.Parcelable;
import com.redbeemedia.enigma.core.exposure.models.external.ApiExternalReference;
import com.redbeemedia.enigma.core.exposure.models.Marker;
import java.io.IOException;
import com.redbeemedia.enigma.core.exposure.models.season.ApiSeason;
import com.redbeemedia.enigma.core.exposure.models.track.ApiTrackSizes;
import com.redbeemedia.enigma.core.exposure.models.localized.ApiLocalizedData;
import com.redbeemedia.enigma.core.exposure.models.person.ApiPerson;
import com.redbeemedia.enigma.core.exposure.models.media.ApiMedia;
import com.redbeemedia.enigma.core.exposure.models.parental.ApiParentalRating;
import com.redbeemedia.enigma.core.exposure.models.publication.ApiPublication;
import com.redbeemedia.enigma.core.exposure.models.user.ApiUserAssetData;
import android.util.JsonReader;
import java.util.List;
import com.redbeemedia.enigma.core.util.JsonReaderUtil;


public class ApiAsset implements Parcelable {
    private String expires;
    private ApiUserAssetData userData;
    private List<ApiLinkedEntity> linkedEntities;
    private List<ApiLocalizedData> localized;
    private double rating;
    private String episode;
    private int productionYear;
    private String type;
    private List<String> productionCountries;
    private List<String> audioTracks;
    private String seasonId;
    private String assetId;
    private String defaultAudioTrack;
    private String season;
    private boolean live;
    private List<ApiPerson> participants;
    private List<String> subtitles;
    private List<ApiExternalReference> externalReferences;
    private List<ApiSeason> seasons;
    private String releaseDate;
    private String created;
    private List<ApiParentalRating> parentalRatings;
    private int runtime;
    private String tvShowId;
    private List<ApiTag> tags;
    private List<ApiMedia> medias;
    private ApiTrackSizes trackSizes;
    private String originalTitle;
    private List<String> spokenLanguages;
    private List<Marker> markers;
    private String changed;
    private List<ApiPublication> publications;


    private ApiAsset() {}//Private constructor for Parcelable.Creator

    public ApiAsset(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "expires":
                    this.expires = jsonReader.nextString();
                    break;
                case "userData":
                    this.userData = new ApiUserAssetData(jsonReader);
                    break;
                case "linkedEntities":
                    this.linkedEntities = JsonReaderUtil.readArray(jsonReader, ApiLinkedEntity.class);
                    break;
                case "localized":
                    this.localized = JsonReaderUtil.readArray(jsonReader, ApiLocalizedData.class);
                    break;
                case "rating":
                    this.rating = jsonReader.nextDouble();
                    break;
                case "episode":
                    this.episode = jsonReader.nextString();
                    break;
                case "productionYear":
                    this.productionYear = jsonReader.nextInt();
                    break;
                case "type":
                    this.type = jsonReader.nextString();
                    break;
                case "productionCountries":
                    this.productionCountries = JsonReaderUtil.readArray(jsonReader, String.class);
                    break;
                case "audioTracks":
                    this.audioTracks = JsonReaderUtil.readArray(jsonReader, String.class);
                    break;
                case "seasonId":
                    this.seasonId = jsonReader.nextString();
                    break;
                case "assetId":
                    this.assetId = jsonReader.nextString();
                    break;
                case "defaultAudioTrack":
                    this.defaultAudioTrack = jsonReader.nextString();
                    break;
                case "season":
                    this.season = jsonReader.nextString();
                    break;
                case "live":
                    this.live = jsonReader.nextBoolean();
                    break;
                case "participants":
                    this.participants = JsonReaderUtil.readArray(jsonReader, ApiPerson.class);
                    break;
                case "subtitles":
                    this.subtitles = JsonReaderUtil.readArray(jsonReader, String.class);
                    break;
                case "externalReferences":
                    this.externalReferences = JsonReaderUtil.readArray(jsonReader, ApiExternalReference.class);
                    break;
                case "seasons":
                    this.seasons = JsonReaderUtil.readArray(jsonReader, ApiSeason.class);
                    break;
                case "releaseDate":
                    this.releaseDate = jsonReader.nextString();
                    break;
                case "created":
                    this.created = jsonReader.nextString();
                    break;
                case "parentalRatings":
                    this.parentalRatings = JsonReaderUtil.readArray(jsonReader, ApiParentalRating.class);
                    break;
                case "runtime":
                    this.runtime = jsonReader.nextInt();
                    break;
                case "tvShowId":
                    this.tvShowId = jsonReader.nextString();
                    break;
                case "tags":
                    this.tags = JsonReaderUtil.readArray(jsonReader, ApiTag.class);
                    break;
                case "medias":
                    this.medias = JsonReaderUtil.readArray(jsonReader, ApiMedia.class);
                    break;
                case "trackSizes":
                    this.trackSizes = new ApiTrackSizes(jsonReader);
                    break;
                case "originalTitle":
                    this.originalTitle = jsonReader.nextString();
                    break;
                case "spokenLanguages":
                    this.spokenLanguages = JsonReaderUtil.readArray(jsonReader, String.class);
                    break;
                case "markers":
                    this.markers = JsonReaderUtil.readArray(jsonReader, Marker.class);
                    break;
                case "changed":
                    this.changed = jsonReader.nextString();
                    break;
                case "publications":
                    this.publications = JsonReaderUtil.readArray(jsonReader, ApiPublication.class);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }


    public String getExpires() {
        return this.expires;
    }

    public ApiUserAssetData getUserData() {
        return this.userData;
    }

    public List<ApiLinkedEntity> getLinkedEntities() {
        return this.linkedEntities;
    }

    public List<ApiLocalizedData> getLocalized() {
        return this.localized;
    }

    public double getRating() {
        return this.rating;
    }

    public String getEpisode() {
        return this.episode;
    }

    public int getProductionYear() {
        return this.productionYear;
    }

    public String getType() {
        return this.type;
    }

    public List<String> getProductionCountries() {
        return this.productionCountries;
    }

    public List<String> getAudioTracks() {
        return this.audioTracks;
    }

    public String getSeasonId() {
        return this.seasonId;
    }

    public String getAssetId() {
        return this.assetId;
    }

    public String getDefaultAudioTrack() {
        return this.defaultAudioTrack;
    }

    public String getSeason() {
        return this.season;
    }

    public boolean getLive() {
        return this.live;
    }

    public List<ApiPerson> getParticipants() {
        return this.participants;
    }

    public List<String> getSubtitles() {
        return this.subtitles;
    }

    public List<ApiExternalReference> getExternalReferences() {
        return this.externalReferences;
    }

    public List<ApiSeason> getSeasons() {
        return this.seasons;
    }

    public String getReleaseDate() {
        return this.releaseDate;
    }

    public String getCreated() {
        return this.created;
    }

    public List<ApiParentalRating> getParentalRatings() {
        return this.parentalRatings;
    }

    public int getRuntime() {
        return this.runtime;
    }

    public String getTvShowId() {
        return this.tvShowId;
    }

    public List<ApiTag> getTags() {
        return this.tags;
    }

    public List<ApiMedia> getMedias() {
        return this.medias;
    }

    public ApiTrackSizes getTrackSizes() {
        return this.trackSizes;
    }

    public String getOriginalTitle() {
        return this.originalTitle;
    }

    public List<String> getSpokenLanguages() {
        return this.spokenLanguages;
    }

    public List<Marker> getMarkers() {
        return this.markers;
    }

    public String getChanged() {
        return this.changed;
    }

    public List<ApiPublication> getPublications() {
        return this.publications;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ApiAsset> CREATOR = new Parcelable.Creator<ApiAsset>() {
        public ApiAsset createFromParcel(Parcel in) {
            ApiAsset object = new ApiAsset();
            object.expires = in.readString();
            object.userData = in.readParcelable(ApiUserAssetData.class.getClassLoader());
            object.linkedEntities = in.createTypedArrayList(ApiLinkedEntity.CREATOR);
            object.localized = in.createTypedArrayList(ApiLocalizedData.CREATOR);
            object.rating = in.readDouble();
            object.episode = in.readString();
            object.productionYear = in.readInt();
            object.type = in.readString();
            object.productionCountries = in.createStringArrayList();
            object.audioTracks = in.createStringArrayList();
            object.seasonId = in.readString();
            object.assetId = in.readString();
            object.defaultAudioTrack = in.readString();
            object.season = in.readString();
            object.live = (in.readInt() != 0);
            object.participants = in.createTypedArrayList(ApiPerson.CREATOR);
            object.subtitles = in.createStringArrayList();
            object.externalReferences = in.createTypedArrayList(ApiExternalReference.CREATOR);
            object.seasons = in.createTypedArrayList(ApiSeason.CREATOR);
            object.releaseDate = in.readString();
            object.created = in.readString();
            object.parentalRatings = in.createTypedArrayList(ApiParentalRating.CREATOR);
            object.runtime = in.readInt();
            object.tvShowId = in.readString();
            object.tags = in.createTypedArrayList(ApiTag.CREATOR);
            object.medias = in.createTypedArrayList(ApiMedia.CREATOR);
            object.trackSizes = in.readParcelable(ApiTrackSizes.class.getClassLoader());
            object.originalTitle = in.readString();
            object.spokenLanguages = in.createStringArrayList();
            object.markers = in.createTypedArrayList(Marker.CREATOR);
            object.changed = in.readString();
            object.publications = in.createTypedArrayList(ApiPublication.CREATOR);
            return object;
        }

        public ApiAsset[] newArray(int size) {
            return new ApiAsset[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(expires);
        dest.writeParcelable(userData, flags);
        dest.writeTypedList(linkedEntities);
        dest.writeTypedList(localized);
        dest.writeDouble(rating);
        dest.writeString(episode);
        dest.writeInt(productionYear);
        dest.writeString(type);
        dest.writeStringList(productionCountries);
        dest.writeStringList(audioTracks);
        dest.writeString(seasonId);
        dest.writeString(assetId);
        dest.writeString(defaultAudioTrack);
        dest.writeString(season);
        dest.writeInt(live ? 1 : 0);
        dest.writeTypedList(participants);
        dest.writeStringList(subtitles);
        dest.writeTypedList(externalReferences);
        dest.writeTypedList(seasons);
        dest.writeString(releaseDate);
        dest.writeString(created);
        dest.writeTypedList(parentalRatings);
        dest.writeInt(runtime);
        dest.writeString(tvShowId);
        dest.writeTypedList(tags);
        dest.writeTypedList(medias);
        dest.writeParcelable(trackSizes, flags);
        dest.writeString(originalTitle);
        dest.writeStringList(spokenLanguages);
        dest.writeTypedList(markers);
        dest.writeString(changed);
        dest.writeTypedList(publications);
    }
}
