package com.redbeemedia.enigma.core.video;

import android.util.Log;

import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.SimpleHttpCall;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/** Default abstract implementation for image handling. Concrete implementations handles the decoding of the actual image type represented by `T` (i.e. `Bitmap` objects). */
public abstract class SpriteImageRepository<T> implements ISpriteImageRepository<T> {
    private static final String TAG = SpriteImageRepository.class.getName();

    // This number is used to warn consumers that the sprite image is large and that the sprite handling will use a lot of memory.
    private static final int MAX_RECOMMENDED_CACHE_SIZE = 50;

    private volatile HashMap<URL, byte[]> compressedImages = new HashMap<>();
    private volatile HashMap<SpriteData, T> cache = new HashMap<>();
    private volatile Collection<SpriteData> activeSpriteData = new ArrayList<>();

    private final IHttpHandler httpHandler;
    private WeakReference<ImageCacheListener> imageCacheListener;

    private volatile Thread downloadThread;

    SpriteImageRepository(IHttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    @Override
    public void cacheImages(Collection<SpriteData> spriteData, @Nullable ImageCacheListener listener) {
        synchronized(this) {
            this.activeSpriteData = new ArrayList<>(spriteData);
            compressedImages = new HashMap<>();
            imageCacheListener = new WeakReference<>(listener);
            downloadThread = createDownloadThread();
            downloadThread.start();
        }
    }

    @Override
    public @Nullable T getImage(SpriteData spriteData) {
        synchronized(this) {
            if (!compressedImages.containsKey(spriteData.imageUrl)) {
                Log.d(TAG, "Compressed images not for " + spriteData.imageUrl + " not downloaded. Make sure to cache image prior to calling getImage.");
                return null;
            }
            if (!cache.containsKey(spriteData)) {
                createSprites(spriteData);
            }
            if (cache.containsKey(spriteData)) {
                return cache.get(spriteData);
            }
            Log.w(TAG, "No sprite image cached for " + spriteData.imageUrl);
            return null;
        }
    }

    @Override
    public void clear() {
        compressedImages = new HashMap<>();
        activeSpriteData = new ArrayList<>();
        clearCache();
    }

    /** Implementation is responsible for decoding a raw byte array to the concrete type `T` where `data` contains the raw compressed image file retrieved from the backend.*/
    abstract protected <T extends Object> T doDecodeImage(byte []data);

    /** Implementation is responsible for returning a part of `masterImage` which will represent the actual sprite embedded in the container image.  */
    abstract protected <T extends Object> T doGetSprite(T masterImage, SpriteData.Frame frame);

    /** Implement this method in order to release the image resource. */
    abstract protected <T> void doReleaseImage(T image);

    private boolean saveCompressedData(URL url, InputStream inputStream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            synchronized (this) {
                compressedImages.put(url, buffer.toByteArray());
            }
            return true;
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    private boolean downloadImageMap(URL url, IHttpCall httpCall) {
        final boolean[] success = {false};
        try {
            httpHandler.doHttpBlocking(url, httpCall, new IHttpHandler.IHttpResponseHandler() {
                @Override
                public void onResponse(HttpStatus httpStatus) { }

                @Override
                public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                    success[0] = saveCompressedData(url, inputStream);
                }

                @Override
                public void onException(Exception e) {
                    e.printStackTrace();
                }
            });
        } catch ( InterruptedException e) { e.printStackTrace(); }
        return success[0];
    }

    private Thread createDownloadThread() {
        return new Thread() {
            @Override
            public void run() {
                SimpleHttpCall httpCall = new SimpleHttpCall("GET");
                int downloadCount = 0;
                for(SpriteData sprite : activeSpriteData) {
                    if(!compressedImages.containsKey(sprite.imageUrl)) {
                        if(downloadImageMap(sprite.imageUrl, httpCall)) { downloadCount++; }
                    }
                }
                if (imageCacheListener.get() != null) {
                    imageCacheListener.get().onDone(downloadCount);
                }
            }
        };
    }


    private void clearCache() {
        if (cache != null) {
            for(T image : cache.values()) {
                doReleaseImage(image);
            }
        }
        cache = new HashMap<>();
    }

    private void createSprites(SpriteData spriteData) {
        T masterImage = doDecodeImage(compressedImages.get(spriteData.imageUrl));
        clearCache();
        for(SpriteData sprite : activeSpriteData) {
            if (sprite.imageUrl.equals(spriteData.imageUrl)) {
                T spriteImage = doGetSprite(masterImage, sprite.frame);
                cache.put(sprite, spriteImage);
            }
        }
        doReleaseImage(masterImage);
        if (cache.size() > MAX_RECOMMENDED_CACHE_SIZE) {
            Log.w(TAG, "Sprite map image is very large. Please request adjustment of the VTT configuration by splitting sprites in to several sprite maps. " + spriteData.imageUrl + " contains " + cache.keySet().size() + " images. Maximum recommended size is " + MAX_RECOMMENDED_CACHE_SIZE);
        }
    }
}
