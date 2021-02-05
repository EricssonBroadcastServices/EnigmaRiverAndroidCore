package com.redbeemedia.enigma.core.video;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.session.ISession;

import java.util.Collection;
import java.util.Map;

/** Represents an entity capable of retrieving VTT sprite data for a video stream. */
public interface ISpriteRepository {

    /**
     * Fetch the sprite metadata asynchronously for the specified width. The sprites fetched should be relevant for the current playback session.
     * @param width The requested width of the sprites. One can use {@link #getWidths() getWidths} in order to determine available sprite widths.
     * @param metadataListener If not null, the listener will be called upon successful or failed request for sprite metadata.
     */
    void activate(int width, @Nullable ISpriteRepository.MetadataListener metadataListener);

    /** Returns the sprite as in {@link #activate(int, MetadataListener) activate}, but with the smallest possible width. */
    void activate(@Nullable ISpriteRepository.MetadataListener metadataListener);

    /** <b>Intended for internal usage</b>. Sets the list of vtt URLs with their corresponding width. */
    void setVTTUrls(Map<Integer, String> vttUrls, ISession session);

    /** Clears downloaded data. */
    void clear();

    /**
     * Return the metadata for the specified position if possible. Please note that {@link #activate(int, MetadataListener) activate} needs to be called
     * prior to calling this method in order for the <code>ISpriteRepository</code> to have fetched it's content.
     * @param position A position on the timeline.
     * @return The sprite metadata for the specified position or <code>null</code> if no sprite exists for that position.
     */
    @Nullable SpriteData getSpriteData(ITimelinePosition position);

    /**
     * Return the sprite image for the specified position if possible. Please note that {@link #activate(int, MetadataListener) activate} needs to be called
     * prior to calling this method in order for the <code>ISpriteRepository</code> to have fetched it's content.
     * @param position A position on the timeline.
     * @param delegate Will be called if the sprite was fetched successfully.
     */
    <T extends Object> void getSprite(ITimelinePosition position, SpriteListener<T> delegate);

    /**
     *  Fetching a sprite for the position in milliseconds. See {@link #getSprite(ITimelinePosition, SpriteListener) getSprite}.
     */
    <T extends Object> void getSprite(long milliseconds, SpriteListener<T> delegate);

    /** Available widths for sprites. This list can be used to determine what sprites are eligible for fetch using {@link #activate(int, MetadataListener) activate}. */
    @Nullable Collection<Integer> getWidths();

    /**
     * Allows the custom configuration of how the images are being parsed and retrieved.
     * @param imageRepository The <code>ISpriteImageRepository<T></code> to be used for image downloading and retrieval. If set to `null`, image handling will be disabled.
     * @param <T> The type of image object representation to use.
     */
    <T extends Object> void setImageRepository(@Nullable ISpriteImageRepository<T> imageRepository);

    /** Callback for sprite fetching. */
    interface MetadataListener {

        /** Called whenever a sprite fetch has completed. `sprites` will contain the entire set of sprites for the current playback session and will be empty if an error occurred or if.
         * no sprites was available. */
        void onDone(Collection<SpriteData> sprites);
    }

    /** Delegate listener for the fetching of sprite images. */
    interface SpriteListener<T> {

        /** Will be called with an image (i.e. a <Code>Bitmap</Code>) or <code>null</code> if image not found. */
        void onDone(@Nullable T sprite);
    }
}
