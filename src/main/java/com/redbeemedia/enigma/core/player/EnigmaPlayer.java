package com.redbeemedia.enigma.core.player;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;

import com.redbeemedia.enigma.core.activity.AbstractActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleListener;
import com.redbeemedia.enigma.core.activity.IActivityLifecycleManager;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.drm.DrmInfoFactory;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.drm.IDrmProvider;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat.DrmTechnology;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat.StreamFormat;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playable.IPlayableHandler;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.ProxyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class EnigmaPlayer implements IEnigmaPlayer {
    private static final EnigmaMediaFormat[] FORMAT_PREFERENCE_ORDER = new EnigmaMediaFormat[]{new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.WIDEVINE),
                                                                                               new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.NONE)};

    private ISession session;
    private IPlayerImplementation playerImplementation;
    private EnigmaPlayerEnvironment environment = new EnigmaPlayerEnvironment();
    private WeakReference<Activity> weakActivity = new WeakReference<>(null);
    private IActivityLifecycleListener activityLifecycleListener;
    private ITimeProvider timeProvider;
    private IHandler callbackHandler = null;

    private Collector<IEnigmaPlayerListener> enigmaPlayerCollector;
    private IEnigmaPlayerListener enigmaPlayerListeners;

    private IPlaybackSessionFactory playbackSessionFactory = new DefaultPlaybackSessionFactory();
    private IPlaybackSession currentPlaybackSession = null;

    public EnigmaPlayer(ISession session, IPlayerImplementation playerImplementation) {
        this.session = session;
        this.playerImplementation = playerImplementation;
        EnigmaPlayerCollector playerCollector = new EnigmaPlayerCollector();
        this.enigmaPlayerCollector = playerCollector;
        this.enigmaPlayerListeners = playerCollector;
        this.playerImplementation.install(environment);
        this.activityLifecycleListener = new AbstractActivityLifecycleListener() {
            @Override
            public void onDestroy() {
                playerImplementation.release();
                ((LegacyTimeProvider) timeProvider).release();
                synchronized (EnigmaPlayer.this) {
                    if(currentPlaybackSession != null) {
                        currentPlaybackSession.onStop(EnigmaPlayer.this);
                    }
                    currentPlaybackSession = null;
                }
            }
        };
        this.timeProvider = newTimeProvider(session);
    }

    protected ITimeProvider newTimeProvider(ISession session) {
        return new LegacyTimeProvider(session);
    }

    public EnigmaPlayer setActivity(Activity activity) {
        IActivityLifecycleManager lifecycleManager = EnigmaRiverContext.getActivityLifecycleManager();
        Activity oldActivity = weakActivity.get();
        if(oldActivity != null) {
            lifecycleManager.remove(oldActivity, activityLifecycleListener);
        }
        lifecycleManager.add(activity, activityLifecycleListener);
        weakActivity = new WeakReference<>(activity);
        //TODO should we allow switching activities?
        return this;
    }

    @Override
    public void play(IPlayRequest playRequest) {
        IPlayable playable = playRequest.getPlayable();
        playable.useWith(new PlayableHandler(playRequest.getResultHandler()));
    }

    @Override
    public boolean addListener(IEnigmaPlayerListener playerListener) {
        return enigmaPlayerCollector.addListener(playerListener);
    }

    @Override
    public boolean removeListener(IEnigmaPlayerListener playerListener) {
        return enigmaPlayerCollector.removeListener(playerListener);
    }

    @Override
    public EnigmaPlayer setCallbackHandler(Handler handler) {
        return (EnigmaPlayer) setCallbackHandler(new HandlerWrapper(handler));
    }

    @Override
    public EnigmaPlayer setCallbackHandler(IHandler handler) {
        this.enigmaPlayerListeners = ProxyCallback.createCallbackOnThread(handler, IEnigmaPlayerListener.class, (IEnigmaPlayerListener) enigmaPlayerCollector);
        this.callbackHandler = handler;
        return this;
    }

    private class PlayableHandler implements IPlayableHandler {
        private IPlayResultHandler playResultHandler;

        public PlayableHandler(IPlayResultHandler playResultHandler) {
            if(callbackHandler != null) {
                this.playResultHandler = ProxyCallback.createCallbackOnThread(callbackHandler, IPlayResultHandler.class, playResultHandler);
            } else {
                this.playResultHandler = playResultHandler;
            }
        }

        @Override
        public void startUsingAssetId(String assetId) {
            URL url = null;
            try {
                url = session.getBusinessUnit().getApiBaseUrl("v2").append("entitlement").append(assetId).append("play").toURL();
            } catch (MalformedURLException e) {
                playResultHandler.onError(new InvalidAssetError(assetId, new UnexpectedError(e)));
                return;
            }
            AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("GET", session);
            EnigmaRiverContext.getHttpHandler().doHttp(url, apiCall, new PlayResponseHandler(assetId) {
                @Override
                protected void onError(Error error) {
                    playResultHandler.onError(error);
                }

                @Override
                protected void onSuccess(JSONObject jsonObject) throws JSONException {
                    String playToken = jsonObject.optString("playToken");
                    JSONArray formats = jsonObject.getJSONArray("formats");
                    JSONObject usableMediaFormat = getUsableMediaFormat(formats, environment.formatSupportSpec);
                    if (usableMediaFormat != null) {
                        JSONObject drms = usableMediaFormat.optJSONObject("drm");
                        if (drms != null) {
                            JSONObject drmTypeInfo = drms.optJSONObject(DrmTechnology.WIDEVINE.getKey());
                            String licenseUrl = drmTypeInfo.getString("licenseServerUrl");
                            String licenseWithToken = Uri.parse(licenseUrl)
                                    .buildUpon()
                                    .appendQueryParameter("token", "Bearer " + playToken)
                                    .build().toString();
                            IDrmInfo drmInfo = DrmInfoFactory.createWidevineDrmInfo(licenseWithToken, playToken);
                            environment.setDrmInfo(drmInfo);
                        }
                        String manifestUrl = usableMediaFormat.getString("mediaLocator");
                        //TODO need to call onStarted on the playRequest at some point.
                        playerImplementation.startPlayback(manifestUrl);
                        replacePlaybackSession(playbackSessionFactory.createPlaybackSession(session, jsonObject, timeProvider));
                    } else {
                        onError(new NoSupportedMediaFormatsError("Could not find a media format supported by the current player implementation."));
                    }
                }
            });
        }

        @Override
        public void startUsingUrl(URL url) {
            //TODO handle callbacks to IPlayRequest
            playerImplementation.startPlayback(url.toString());
        }
    }


    private synchronized void replacePlaybackSession(IPlaybackSession playbackSession) {
        if(this.currentPlaybackSession != null) {
            this.currentPlaybackSession.onStop(this);
        }
        this.currentPlaybackSession = playbackSession;
        this.currentPlaybackSession.onStart(this);
    }


    private static JSONObject getUsableMediaFormat(JSONArray formats, IMediaFormatSupportSpec formatSupportSpec) throws JSONException {
        Map<EnigmaMediaFormat, JSONObject> foundFormats = new HashMap<>();
        for(int i = 0; i < formats.length(); ++i) {
            JSONObject mediaFormat = formats.getJSONObject(i);
            EnigmaMediaFormat enigmaMediaFormat = parseMediaFormat(mediaFormat);
            if(enigmaMediaFormat != null) {
                if(formatSupportSpec.supports(enigmaMediaFormat)) {
                    foundFormats.put(enigmaMediaFormat, mediaFormat);
                }
            }
        }
        for(EnigmaMediaFormat format : FORMAT_PREFERENCE_ORDER) {
            JSONObject object = foundFormats.get(format);
            if(object != null) {
                return object;
            }
        }
        return null;//If the format is not in FORMAT_PREFERENCE_ORDER we don't support it.
    }

    /*package-protected*/ static EnigmaMediaFormat parseMediaFormat(JSONObject mediaFormat) throws JSONException {
        String streamFormatName = mediaFormat.getString("format");
        StreamFormat streamFormat = null;
        DrmTechnology drmTechnology = null;

        if("DASH".equals(streamFormatName)) {
            streamFormat = StreamFormat.DASH;
        } else if("HLS".equals(streamFormatName)) {
            streamFormat = StreamFormat.HLS;
        } else if("SMOOTHSTREAMING".equals(streamFormatName)) {
            streamFormat = StreamFormat.SMOOTHSTREAMING;
        }

        JSONObject drm = mediaFormat.optJSONObject("drm");
        if(drm != null) {
            for(DrmTechnology drmTech : DrmTechnology.values()) {
                if(drmTech == DrmTechnology.NONE) {
                    continue;
                }
                if(drm.has(drmTech.getKey())) {
                    drmTechnology = drmTech;
                    break;
                }
            }
        } else {
            drmTechnology = DrmTechnology.NONE;
        }

        if(streamFormat != null && drmTechnology != null) {
            return new EnigmaMediaFormat(streamFormat, drmTechnology);
        } else {
            return null;
        }
    }

    private class EnigmaPlayerEnvironment implements IEnigmaPlayerEnvironment, IDrmProvider {
        private IMediaFormatSupportSpec formatSupportSpec = new DefaultFormatSupportSpec();
        private IDrmInfo drmInfo;

        @Override
        public IDrmProvider getDrmProvider() {
            return this;
        }

        @Override
        public void setMediaFormatSupportSpec(IMediaFormatSupportSpec formatSupportSpec) {
            this.formatSupportSpec = formatSupportSpec;
        }

        @Override
        public IPlayerImplementationListener getPlayerImplementationListener() {
            return new IPlayerImplementationListener() {
                @Override
                public void onError(Error error) {
                    //TODO feed to current playbackSession and have that object propagate to listeneres.
                    enigmaPlayerListeners.onPlaybackError(error);
                }
            };
        }

        public void setDrmInfo(IDrmInfo drmInfo) {
            this.drmInfo = drmInfo;
        }

        @Override
        public IDrmInfo getDrmInfo() {
            return drmInfo;
        }
    }

    private static class DefaultFormatSupportSpec implements IMediaFormatSupportSpec {
        @Override
        public boolean supports(EnigmaMediaFormat enigmaMediaFormat) {
            return enigmaMediaFormat != null && enigmaMediaFormat.equals(StreamFormat.DASH, DrmTechnology.NONE);
        }
    }
}