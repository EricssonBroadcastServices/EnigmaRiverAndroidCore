package com.redbeemedia.enigma.core.video;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/** Implementation capable of caching sprite images. Implementations download the compressed
    `image containers` (a "map" containing several sprites). */
public interface ISpriteImageRepository<T> {

    /** Asynchronously cache sprite data from all `image containers`.  */
    void cacheImages(Collection<SpriteData> sprites, @Nullable ImageCacheListener listener);

    /**
     * Fetch a sprite represented by `SpriteData` metadata from a `image container`.
     * Images must have been cached using {@link #cacheImages(Collection, ImageCacheListener)}.
     * @param spriteData Metadata for the requested image.
     * @return a sprite <code>T</code> (i.e. a Bitmap) or null if no image found in cache.
     */
    @Nullable T getImage(SpriteData spriteData);

    /** Clears image cache. */
    void clear();

    interface ImageCacheListener {

        /** Returns the total number of `image containers` that have been cached. */
        void onDone(int count);

    }
}
