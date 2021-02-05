package com.redbeemedia.enigma.core.video;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.json.StringResponseHandler;
import com.redbeemedia.enigma.core.player.ITimelinePositionFactory;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.session.ISession;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/** Default implementation for retrieving sprites metadata from VTT url lists. */
public class SpriteRepository implements ISpriteRepository {
    private static final String TAG = SpriteRepository.class.getName();

    private Collection<SpriteData> sprites = new ArrayList<>();
    private Map<Integer, String> baseUrls;
    private ISession session;
    private final SpriteDataVttParser parser;
    private final IHttpHandler httpHandler;
    private final Handler handler = new Handler();
    private volatile boolean isLoading;
    private ISpriteImageRepository<?> imageRepository;
    private WeakReference<SpriteListener> imageListener;

    public SpriteRepository(ITimelinePositionFactory timelinePositionFactory, IHttpHandler httpHandler) {
        this.httpHandler = httpHandler;
        this.parser = new SpriteDataVttParser(timelinePositionFactory);
        imageRepository = new BitmapImageRepository(httpHandler);
    }

    public void setVTTUrls(Map<Integer, String> vttUrls, ISession session) {
        this.baseUrls = vttUrls;
        this.session = session;
    }

    public <T extends Object> void setImageRepository(@Nullable ISpriteImageRepository<T> imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public void activate(int preferredWidth, @Nullable final MetadataListener metadataListener) {
        isLoading = true;
        sprites = new ArrayList<>();

        if (baseUrls != null && baseUrls.isEmpty()) {
            spriteMetadataLoaded(metadataListener, sprites);
            return;
        }

        if (baseUrls == null || !baseUrls.containsKey(preferredWidth)) {
            Log.e(TAG, "No sprites with width '" + preferredWidth + "' available. Has the SpriteRepository been configured?");
            spriteMetadataLoaded(metadataListener, sprites);
            return;
        }

        final URL url = getBaseUrl(preferredWidth);
        if (url == null) {
            spriteMetadataLoaded(metadataListener, sprites);
            return;
        }

        AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("GET", session);

        httpHandler.doHttp(url, apiCall, new StringResponseHandler() {
            @Override
            public void onError(EnigmaError error) {
                error.printStackTrace();
                spriteMetadataLoaded(metadataListener, sprites);
            }

            @Override
            public void onSuccess(String response) {
                sprites = parser.parse(url, response);
                if (imageRepository != null) { imageRepository.cacheImages(sprites, null); };
                spriteMetadataLoaded(metadataListener, sprites);
            }
        });
    }

    @Override
    public void activate(@Nullable ISpriteRepository.MetadataListener metadataListener) {
        sprites = new ArrayList<>();
        if (baseUrls == null || baseUrls.size() == 0) {
            spriteMetadataLoaded(metadataListener, sprites);
        } else {
            activate(new ArrayList<>(getWidths()).get(0), metadataListener);
        }
    }

    @Override
    @Nullable public SpriteData getSpriteData(ITimelinePosition position) {
        if(sprites == null) { return null; }
        for(SpriteData sprite : sprites) {
            if (sprite.position.beforeOrEqual(position) && position.before(sprite.position.add(sprite.duration))) {
                return sprite;
            }
        }
        return null;
    }

    @Override
    public <T extends Object> void getSprite(ITimelinePosition position, SpriteListener<T> delegate) {
        SpriteData spriteData = getSpriteData(position);
        if (spriteData != null && imageRepository != null) {
            // Hack to make sure that only the latest listener is being called.
            final WeakReference<SpriteListener> listenerReference = new WeakReference<>(delegate);
            imageListener = listenerReference;
            handler.post(() -> {
                Object spriteImage = imageRepository.getImage(spriteData);
                if (imageListener.get() != null && listenerReference == imageListener) {
                    imageListener.get().onDone(spriteImage);
                }
            });
        } else {
            delegate.onDone(null);
        }
    }

    @Override
    public <T extends Object> void getSprite(long milliseconds, SpriteListener<T> delegate) {
        getSprite(parser.createTimelinePosition(milliseconds), delegate);
    }

    @Nullable
    public Collection<Integer> getWidths() {
        return baseUrls == null ? null : baseUrls.keySet();
    }

    /** Returns all sprites fetched in the previous request. */
    public @Nullable Collection<SpriteData> getSprites() { return sprites; }

    /** Return true if metadata fetch is in progress. */
    public boolean isLoading() { return isLoading; }

    public void clear() {
        sprites = new ArrayList<>();
        imageRepository.clear();
    }

    private void spriteMetadataLoaded(MetadataListener listener, Collection<SpriteData> sprites) {
        isLoading = false;
        if (listener != null) { listener.onDone(sprites); }
    }

    private URL getBaseUrl(int width) {
        try { return new URL(baseUrls.get(width)); }
        catch (MalformedURLException e) {
            Log.d(TAG, "Unable to parse VTT URL: " + baseUrls.get(width));
            e.printStackTrace();
        }
        return null;
    }
}
