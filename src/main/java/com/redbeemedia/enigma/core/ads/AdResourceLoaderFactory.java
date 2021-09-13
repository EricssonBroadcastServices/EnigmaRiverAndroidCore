package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.http.IHttpHandler;

import org.json.JSONObject;

public class AdResourceLoaderFactory implements IAdResourceLoaderFactory {

    private final String TAG = AdResourceLoaderFactory.class.getName();
    private final IHttpHandler httpHandler;
    private NowtilusHlsLiveResourceLoader nowtilusHlsLiveResourceLoader;
    private NowtilusVodResourceLoader nowtilusVodResourceLoader;

    public AdResourceLoaderFactory(IHttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public <T> IAdResourceLoader createResourceLoader(@Nullable IAdMetadata adsInfo, @Nullable T metadata) {
        if(adsInfo == null || metadata == null) { return null; }
        if(adsInfo.getStitcher() == IAdMetadata.AdStitcherType.None) {return null;}

        if(adsInfo.getStitcher() == IAdMetadata.AdStitcherType.Nowtilus &&
            EnigmaMediaFormat.StreamFormat.HLS.equals(adsInfo.getStreamFormat())) {
                return adsInfo.isLive() ?
                    getNowtilusHlsLiveResourceLoader((String)metadata) :
                    getNowtilusVodResourceLoader((JSONObject)metadata);
        }
        android.util.Log.w(TAG, "IAdResourceLoader not found for type " + adsInfo.getStitcher() + " stream format: " + adsInfo.getStreamFormat());
        return  null;
    }

    private IAdResourceLoader getNowtilusHlsLiveResourceLoader(String manifest) {
        if(nowtilusHlsLiveResourceLoader == null) {
            nowtilusHlsLiveResourceLoader = new NowtilusHlsLiveResourceLoader(httpHandler, new NowtilusLiveParser());
        }
        nowtilusHlsLiveResourceLoader.setManifestUrl(manifest);
        return nowtilusHlsLiveResourceLoader;
    }

    private IAdResourceLoader getNowtilusVodResourceLoader(JSONObject adsJson) {
        if(nowtilusVodResourceLoader == null) {
            nowtilusVodResourceLoader = new NowtilusVodResourceLoader(new NowtilusVodParser());
        }
        nowtilusVodResourceLoader.setJson(adsJson);
        return nowtilusVodResourceLoader;
    }
}
