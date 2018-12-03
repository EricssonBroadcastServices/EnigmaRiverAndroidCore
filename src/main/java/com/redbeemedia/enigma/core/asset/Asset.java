package com.redbeemedia.enigma.core.asset;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;

import java.util.List;

public class Asset implements Parcelable, IPlayable {

    public enum Type {
        MOVIE,
        TV_SHOW,
        TV_SEASON,
        EPISODE
    }

    private String created;
    private String changed;
    private String assetId;
    private String type;
    private List<Localized> localized;
    private List<Tag> tags;
    private List<Publication> publications;
    private List<Participant> participants;
    private int productionYear;
    private String originalTitle;
    private boolean live;
    private List<String> productionCountries;
    private List<String> subtitles;
    private List<String> audioTracks;
    private List<String> spokenLanguages;
    private List<Media> medias;
    private List<ParentalRating> parentalRatings;
    private List<String> linkedEntities;
    private int runtime;
    private Object customData;
    private List<String> externalReferences;
    private double rating;
    private List<String> markers;

    private String tvShowId;
    private String season;
    private String episode;
    private String seasonId;

    public Asset() {
    }

    @Override
    public void useWith(final IPlayableHandler playableHandler) {
        if (!TextUtils.isEmpty(assetId)) {
            playableHandler.startUsingAssetId(assetId);
        }
    }

    public String getTvShowId() {
        return tvShowId;
    }

    public void setTvShowId(final String tvShowId) {
        this.tvShowId = tvShowId;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(final String season) {
        this.season = season;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(final String episode) {
        this.episode = episode;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(final String seasonId) {
        this.seasonId = seasonId;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setChanged(String changed) {
        this.changed = changed;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public void setLocalized(List<Localized> localized) {
        this.localized = localized;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public void setProductionYear(int productionYear) {
        this.productionYear = productionYear;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public void setProductionCountries(List<String> productionCountries) {
        this.productionCountries = productionCountries;
    }

    public void setSubtitles(List<String> subtitles) {
        this.subtitles = subtitles;
    }

    public void setAudioTracks(List<String> audioTracks) {
        this.audioTracks = audioTracks;
    }

    public void setSpokenLanguages(List<String> spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }

    public void setMedias(List<Media> medias) {
        this.medias = medias;
    }

    public void setParentalRatings(List<ParentalRating> parentalRatings) {
        this.parentalRatings = parentalRatings;
    }

    public void setLinkedEntities(List<String> linkedEntities) {
        this.linkedEntities = linkedEntities;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public void setCustomData(Object customData) {
        this.customData = customData;
    }

    public void setExternalReferences(List<String> externalReferences) {
        this.externalReferences = externalReferences;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setMarkers(List<String> markers) {
        this.markers = markers;
    }

    public String getCreated() {
        return created;
    }

    public String getChanged() {
        return changed;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getTypes() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public List<Localized> getLocalized() {
        return localized;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public int getProductionYear() {
        return productionYear;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public boolean isLive() {
        return live;
    }

    public List<String> getProductionCountries() {
        return productionCountries;
    }

    public List<String> getSubtitles() {
        return subtitles;
    }

    public List<String> getAudioTracks() {
        return audioTracks;
    }

    public List<String> getSpokenLanguages() {
        return spokenLanguages;
    }

    public List<Media> getMedias() {
        return medias;
    }

    public List<ParentalRating> getParentalRatings() {
        return parentalRatings;
    }

    public List<String> getLinkedEntities() {
        return linkedEntities;
    }

    public int getRuntime() {
        return runtime;
    }

    public Object getCustomData() {
        return customData;
    }

    public List<String> getExternalReferences() {
        return externalReferences;
    }

    public double getRating() {
        return rating;
    }

    public List<String> getMarkers() {
        return markers;
    }

    public boolean isEpisode() {
        return Type.EPISODE.name().equals(type);
    }

    public boolean isTvShow() {
        return Type.TV_SHOW.name().equals(type);
    }

    @Override
    public int hashCode() {
        return assetId.hashCode();
    }

    @Override
    public String toString() {
        return "Asset ID: " + assetId + " [" + originalTitle + " | " + hashCode() + "]";
    }

    // <editor-fold description="PARCELABLE IMPLEMENTATION">
    protected Asset(final Parcel in) {
        created = in.readString();
        changed = in.readString();
        assetId = in.readString();
        type = in.readString();
        tags = in.createTypedArrayList(Tag.CREATOR);
        localized = in.createTypedArrayList(Localized.CREATOR);
        publications = in.createTypedArrayList(Publication.CREATOR);
        participants = in.createTypedArrayList(Participant.CREATOR);
        productionYear = in.readInt();
        originalTitle = in.readString();
        live = in.readByte() != 0;
        productionCountries = in.createStringArrayList();
        subtitles = in.createStringArrayList();
        audioTracks = in.createStringArrayList();
        spokenLanguages = in.createStringArrayList();
        medias = in.createTypedArrayList(Media.CREATOR);
        parentalRatings = in.createTypedArrayList(ParentalRating.CREATOR);
        linkedEntities = in.createStringArrayList();
        runtime = in.readInt();
        externalReferences = in.createStringArrayList();
        rating = in.readDouble();
        markers = in.createStringArrayList();
        tvShowId = in.readString();
        season = in.readString();
        episode = in.readString();
        seasonId = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(created);
        dest.writeString(changed);
        dest.writeString(assetId);
        dest.writeString(type);
        dest.writeTypedList(tags);
        dest.writeTypedList(localized);
        dest.writeTypedList(publications);
        dest.writeTypedList(participants);
        dest.writeInt(productionYear);
        dest.writeString(originalTitle);
        dest.writeByte((byte) (live ? 1 : 0));
        dest.writeStringList(productionCountries);
        dest.writeStringList(subtitles);
        dest.writeStringList(audioTracks);
        dest.writeStringList(spokenLanguages);
        dest.writeTypedList(medias);
        dest.writeTypedList(parentalRatings);
        dest.writeStringList(linkedEntities);
        dest.writeInt(runtime);
        dest.writeStringList(externalReferences);
        dest.writeDouble(rating);
        dest.writeStringList(markers);
        dest.writeString(tvShowId);
        dest.writeString(season);
        dest.writeString(episode);
        dest.writeString(seasonId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Asset> CREATOR = new Creator<Asset>() {
        @Override
        public Asset createFromParcel(final Parcel in) {
            return new Asset(in);
        }

        @Override
        public Asset[] newArray(final int size) {
            return new Asset[size];
        }
    };
    // </editor-fold>
}
