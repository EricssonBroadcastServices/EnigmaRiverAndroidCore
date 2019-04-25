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
import com.redbeemedia.enigma.core.error.IllegalSeekPositionError;
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
import com.redbeemedia.enigma.core.player.controls.AbstractEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.playrequest.IPlayResultHandler;
import com.redbeemedia.enigma.core.playrequest.IPlaybackProperties;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.IInternalCallbackObject;
import com.redbeemedia.enigma.core.util.OpenContainer;
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
    private static final String TAG = "EnigmaPlayer";
    private static final EnigmaMediaFormat[] FORMAT_PREFERENCE_ORDER = new EnigmaMediaFormat[]{new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.WIDEVINE),
                                                                                               new EnigmaMediaFormat(StreamFormat.DASH, DrmTechnology.NONE)};

    private ISession session;
    private IPlayerImplementation playerImplementation;
    private final EnigmaPlayerControls controls = new EnigmaPlayerControls();
    private EnigmaPlayerEnvironment environment = new EnigmaPlayerEnvironment();
    private EnigmaPlayerState state = EnigmaPlayerState.IDLE;
    private WeakReference<Activity> weakActivity = new WeakReference<>(null);
    private IActivityLifecycleListener activityLifecycleListener;
    private ITimeProvider timeProvider;
    private IHandler callbackHandler = null;

    private EnigmaPlayerCollector enigmaPlayerListeners = new EnigmaPlayerCollector();

    private IPlaybackSessionFactory playbackSessionFactory = new DefaultPlaybackSessionFactory();
    private IPlaybackSession currentPlaybackSession = null;
    private final OpenContainer<IPlaybackStartAction> currentPlaybackStartAction = new OpenContainer<>(null);

    public EnigmaPlayer(ISession session, IPlayerImplementation playerImplementation) {
        this.session = session;
        this.playerImplementation = playerImplementation;
        this.playerImplementation.install(environment);
        environment.validateInstallation();
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
        IPlaybackProperties playbackProperties = playRequest.getPlaybackProperties();
        IPlayResultHandler resultHandler = playRequest.getResultHandler();
        playable.useWith(new PlayableHandler(playbackProperties, resultHandler));
    }

    @Override
    public boolean addListener(IEnigmaPlayerListener playerListener) {
        return enigmaPlayerListeners.addListener(playerListener);
    }

    @Override
    public boolean addListener(IEnigmaPlayerListener playerListener, Handler handler) {
        return addListener(playerListener, new HandlerWrapper(handler));
    }

    protected boolean addListener(IEnigmaPlayerListener playerListener, IHandler handler) {
        return enigmaPlayerListeners.addListener(playerListener, handler);
    }

    @Override
    public boolean removeListener(IEnigmaPlayerListener playerListener) {
        return enigmaPlayerListeners.removeListener(playerListener);
    }

    @Override
    public EnigmaPlayer setCallbackHandler(Handler handler) {
        return (EnigmaPlayer) setCallbackHandler(new HandlerWrapper(handler));
    }

    @Override
    public EnigmaPlayer setCallbackHandler(IHandler handler) {
        this.callbackHandler = handler;
        return this;
    }

    private <T extends IInternalCallbackObject> T useCallbackHandlerIfPresent(Class<T> callbackInterface, T callback) {
        if(callbackHandler != null) {
            return ProxyCallback.createCallbackOnThread(callbackHandler, callbackInterface, callback);
        } else {
            return callback;
        }
    }

    @Override
    public IEnigmaPlayerControls getControls() {
        return controls;
    }

    @Override
    public EnigmaPlayerState getState() {
        return state;
    }

    private class PlayableHandler implements IPlayableHandler {
        private IPlaybackProperties playbackProperties;
        private IPlayResultHandler playResultHandler;

        public PlayableHandler(IPlaybackProperties playbackProperties, IPlayResultHandler playResultHandler) {
            this.playbackProperties = playbackProperties;
            this.playResultHandler = useCallbackHandlerIfPresent(IPlayResultHandler.class, playResultHandler);
        }

        @Override
        public void startUsingAssetId(String assetId) {
            IPlaybackStartAction playbackStartAction;
            synchronized (currentPlaybackStartAction) {
                if(currentPlaybackStartAction.value != null) {
                    currentPlaybackStartAction.value.cancel();
                }
                currentPlaybackStartAction.value = new PlaybackStartAction(playbackProperties.getPlayFrom(), playResultHandler);
                playbackStartAction = currentPlaybackStartAction.value;
            }
            playbackStartAction.startUsingAssetId(playbackProperties, playResultHandler, assetId);
        }


        @Override
        public void startUsingUrl(URL url) {
            //TODO handle callbacks to IPlayRequest
            environment.playerImplementationControls.load(url.toString());
            environment.playerImplementationControls.start();
        }
    }

    private interface IPlaybackStartAction {
        void startUsingAssetId(IPlaybackProperties playbackProperties, IPlayResultHandler playResultHandler, String assetId);
        IPlaybackProperties.PlayFrom getPlayFrom();
        void onStarted(Object object);
        void cancel();
    }

    private class PlaybackStartAction implements IPlaybackStartAction {
        private final IPlaybackProperties.PlayFrom playFrom;
        private final IPlayResultHandler playResultHandler;

        public PlaybackStartAction(IPlaybackProperties.PlayFrom playFrom, IPlayResultHandler playResultHandler) {
            this.playFrom = playFrom;
            this.playResultHandler = playResultHandler;
        }

        @Override
        public void startUsingAssetId(IPlaybackProperties playbackProperties, IPlayResultHandler playResultHandler, String assetId) {
            URL url;
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
                    changeState(EnigmaPlayerState.IDLE);
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

                        replacePlaybackSession(playbackSessionFactory.createPlaybackSession(session, jsonObject, timeProvider));
                        changeState(EnigmaPlayerState.LOADING);
                        environment.playerImplementationControls.load(manifestUrl, new BasePlayerImplementationControlResultHandler() {
                            @Override
                            public void onError(Error error) {
                                playResultHandler.onError(error);
                            }

                            @Override
                            public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
                                String message = "Manifest load was rejected ("+rejectReason.getType()+"): "+rejectReason.getDetails();
                                playResultHandler.onError(new UnexpectedError(message));
                            }

                            @Override
                            public void onDone() {
                                IPlaybackProperties.PlayFrom playFrom = playbackProperties.getPlayFrom();
                                if(playFrom == IPlaybackProperties.PlayFrom.BEGINNING) {
                                    environment.playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.TIMELINE_START, new BasePlayerImplementationControlResultHandler() {
                                        @Override
                                        public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
                                            String message = "Could not start from requested position ("+rejectReason.getType()+"): "+rejectReason.getDetails();
                                            Log.d(TAG, message);
                                            playResultHandler.onError(new IllegalSeekPositionError(message));
                                        }

                                        @Override
                                        public void onError(Error error) {
                                            playResultHandler.onError(error);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        onError(new NoSupportedMediaFormatsError("Could not find a media format supported by the current player implementation."));
                    }
                }
            });
        }

        @Override
        public IPlaybackProperties.PlayFrom getPlayFrom() {
            return playFrom;
        }

        @Override
        public void onStarted(Object object) {
            playResultHandler.onStarted(object);
        }

        @Override
        public void cancel() {
        }
    }


    private synchronized void replacePlaybackSession(IPlaybackSession playbackSession) {
        if(this.currentPlaybackSession != null) {
            this.currentPlaybackSession.onStop(this);
        }
        this.currentPlaybackSession = playbackSession;
        this.currentPlaybackSession.onStart(this);
    }



    private synchronized void changeState(EnigmaPlayerState newState) {
        EnigmaPlayerState oldState = this.state;
        this.state = newState;
        if(oldState != newState) {
            enigmaPlayerListeners.onStateChanged(oldState, newState);
        }
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
        private IPlayerImplementationControls playerImplementationControls;
        private IPlayerImplementationInternals playerImplementationInternals;
        private IPlayerImplementationListener playerImplementationListener;

        @Override
        public IDrmProvider getDrmProvider() {
            return this;
        }

        @Override
        public void setMediaFormatSupportSpec(IMediaFormatSupportSpec formatSupportSpec) {
            this.formatSupportSpec = formatSupportSpec;
        }

        @Override
        public void setControls(IPlayerImplementationControls controls) {
            this.playerImplementationControls = controls;
        }

        @Override
        public void setInternals(IPlayerImplementationInternals internals) {
            this.playerImplementationInternals = internals;
        }

        @Override
        public IPlayerImplementationListener getPlayerImplementationListener() {
            synchronized (this) {
                if(playerImplementationListener == null) {
                    playerImplementationListener = new IPlayerImplementationListener() {
                        @Override
                        public void onError(Error error) {
                            //TODO feed to current playbackSession and have that object propagate to listeneres.
                            enigmaPlayerListeners.onPlaybackError(error);
                            changeState(EnigmaPlayerState.IDLE);
                        }

                        @Override
                        public void onLoadCompleted() {
                            synchronized (currentPlaybackStartAction) {
                                if(currentPlaybackStartAction.value != null) {
                                    environment.playerImplementationControls.start(new BasePlayerImplementationControlResultHandler());
                                    changeState(EnigmaPlayerState.LOADED);
                                }
                            }
                        }

                        @Override
                        public void onPlaybackStarted() {
                            synchronized (currentPlaybackStartAction) {
                                if(currentPlaybackStartAction.value != null) {
                                    currentPlaybackStartAction.value.onStarted(currentPlaybackSession.value);
                                    currentPlaybackStartAction.value = null;
                                    changeState(EnigmaPlayerState.PLAYING);
                                }
                            }
                        }
                    };
                }
                return playerImplementationListener;
            }
        }

        public void setDrmInfo(IDrmInfo drmInfo) {
            this.drmInfo = drmInfo;
        }

        @Override
        public IDrmInfo getDrmInfo() {
            return drmInfo;
        }

        public void validateInstallation() {
            if(playerImplementationControls == null) {
                throw new IllegalStateException("PlayerImplementation did not provide controls!");
            }
            if(playerImplementationInternals == null) {
                throw new IllegalStateException("PlayerImplementation did not provide internals!");
            }
        }
    }

    private static class DefaultFormatSupportSpec implements IMediaFormatSupportSpec {
        @Override
        public boolean supports(EnigmaMediaFormat enigmaMediaFormat) {
            return enigmaMediaFormat != null && enigmaMediaFormat.equals(StreamFormat.DASH, DrmTechnology.NONE);
        }
    }

    private class EnigmaPlayerControls extends AbstractEnigmaPlayerControls {
        @Override
        public EnigmaPlayerState getPlayerState() {
            return EnigmaPlayer.this.getState();
        }

        private ControlResultHandlerAdapter wrapResultHandler(IControlResultHandler resultHandler) {
            return new ControlResultHandlerAdapter(useCallbackHandlerIfPresent(IControlResultHandler.class, resultHandler));
        }

        @Override
        protected IControlResultHandler getDefaultResultHandler() {
            return new DefaultControlResultHandler(TAG);
        }

        private boolean seekAllowed() {
            synchronized (currentPlaybackSession) {
                return currentPlaybackSession.value != null && currentPlaybackSession.value.isSeekAllowed();
            }
        }

        private boolean seekToLiveAllowed() {
            synchronized (currentPlaybackSession) {
                return currentPlaybackSession.value != null && currentPlaybackSession.value.isSeekToLiveAllowed();
            }
        }

        @Override
        public void start(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            environment.playerImplementationControls.start(controlResultHandler);
            controlResultHandler.runWhenDone(() -> {
                changeState(EnigmaPlayerState.PLAYING);
                updatePlayingFromLive();
            });
        }

        @Override
        public void pause(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            environment.playerImplementationControls.pause(controlResultHandler);
            controlResultHandler.runWhenDone(() -> {
                changeState(EnigmaPlayerState.PAUSED);
                setPlayingFromLive(false);
            });
        }

        @Override
        public void stop(IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            environment.playerImplementationControls.stop(controlResultHandler);
            controlResultHandler.runWhenDone(() -> {
                changeState(EnigmaPlayerState.IDLE);
                setPlayingFromLive(false);
            });
        }

        @Override
        public void seekTo(long millis, IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            if(seekAllowed()) {
                environment.playerImplementationControls.seekTo(new IPlayerImplementationControls.TimelineRelativePosition(millis), controlResultHandler);
                controlResultHandler.runWhenDone(() -> updatePlayingFromLive());
            } else {
                controlResultHandler.onRejected(RejectReason.contractRestriction("Seek not allowed"));
            }
        }

        @Override
        public void seekTo(StreamPosition streamPosition, IControlResultHandler resultHandler) {
            ControlResultHandlerAdapter controlResultHandler = wrapResultHandler(resultHandler);
            if(streamPosition == StreamPosition.START) {
                if(seekAllowed()) {
                    environment.playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.TIMELINE_START, controlResultHandler);
                    controlResultHandler.runWhenDone(() -> updatePlayingFromLive());
                } else {
                    controlResultHandler.onRejected(RejectReason.contractRestriction("Seek not allowed"));
                }
            } else if(streamPosition == StreamPosition.LIVE_EDGE) {
                if(seekToLiveAllowed()) {
                    environment.playerImplementationControls.seekTo(IPlayerImplementationControls.ISeekPosition.LIVE_EDGE, controlResultHandler);
                    controlResultHandler.runWhenDone(() -> setPlayingFromLive(true));
                } else {
                    if(seekAllowed()) {
                        controlResultHandler.onRejected(RejectReason.inapplicable("Seek to live not allowed"));
                    } else {
                        controlResultHandler.onRejected(RejectReason.contractRestriction("Seek not allowed"));
                    }
                }
            } else {
                controlResultHandler.onRejected(RejectReason.illegal("Unknown "+StreamPosition.class.getSimpleName()+" \""+streamPosition+"\""));
            }
        }

        @Override
        public void setVolume(float volume, IControlResultHandler resultHandler) {
            environment.playerImplementationControls.setVolume(volume, wrapResultHandler(resultHandler));
        }
    }

}