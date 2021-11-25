package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.entitlement.EntitlementData;
import com.redbeemedia.enigma.core.entitlement.EntitlementRequest;
import com.redbeemedia.enigma.core.entitlement.EntitlementStatus;
import com.redbeemedia.enigma.core.entitlement.IEntitlementProvider;
import com.redbeemedia.enigma.core.entitlement.IEntitlementResponseHandler;
import com.redbeemedia.enigma.core.entitlement.listener.BaseEntitlementListener;
import com.redbeemedia.enigma.core.entitlement.listener.EntitlementCollector;
import com.redbeemedia.enigma.core.entitlement.listener.IEntitlementListener;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.AssetBlockedError;
import com.redbeemedia.enigma.core.error.ConcurrentStreamsLimitReachedError;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.GeoBlockedError;
import com.redbeemedia.enigma.core.error.NotAvailableError;
import com.redbeemedia.enigma.core.error.NotEntitledError;
import com.redbeemedia.enigma.core.error.NotPublishedError;
import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.SystemBootTimeProvider;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/*package-protected*/ class ProgramService implements IInternalPlaybackSessionListener {
    private final ISession session;
    private final IStreamInfo streamInfo;
    private final IPlaybackSessionInfo playbackSessionInfo;
    private final IStreamPrograms streamPrograms;
    private final IEntitlementProvider entitlementProvider;
    private final ITimeProvider timeProviderForCache;
    private final IEnigmaPlayerListener playerListener;
    private boolean playingFromLive;
    private final EntitlementCollector entitlementCollector = new EntitlementCollector();
    private final List<ICachedEntitlementResponse> cachedEntitlementResponses = new ArrayList<>();
    private Duration lastCheckedOffset;

    private final OpenContainer<EntitlementData> currentEntitlementData = new OpenContainer<>(null);

    public ProgramService(ISession session, IStreamInfo streamInfo, IStreamPrograms streamPrograms, IPlaybackSessionInfo playbackSessionInfo, IEntitlementProvider entitlementProvider, IPlaybackSession playbackSession, ITaskFactoryProvider taskFactoryProvider) {
        this.session = session;
        this.streamInfo = streamInfo;
        this.streamPrograms = streamPrograms;
        this.playbackSessionInfo = playbackSessionInfo;
        this.entitlementProvider = entitlementProvider;
        this.playingFromLive = playbackSession.isPlayingFromLive();
        this.timeProviderForCache = createTimeProviderForCache();
        playbackSession.addListener(new BasePlaybackSessionListener() {
            @Override
            public void onPlayingFromLiveChanged(boolean live) {
                playingFromLive = live;
            }
        });
        this.playerListener = new BaseEnigmaPlayerListener() {
            @Override
            public void onProgramChanged(IProgram from, IProgram to) {
                checkEntitlementForProgram(to);
            }
        };
    }

    @Override
    public void onStart(OnStartArgs args) {
        entitlementCollector.addListener(new BaseEntitlementListener() {
            @Override
            public void onEntitlementChanged(EntitlementData oldData, EntitlementData newData) {
                if (newData != null && !newData.isSuccess()) {
                    handleEntitlementStatus(args.communicationsChannel, newData.getStatus());
                }
            }
        });

        checkEntitlementForProgram(null);
        args.enigmaPlayer.addListener(playerListener);
    }

    @Override
    public void onStop(OnStopArgs args) {
        args.enigmaPlayer.removeListener(playerListener);
    }

    protected static void handleEntitlementStatus(IEnigmaPlayerConnection.ICommunicationsChannel communicationsChannel, EntitlementStatus status) {
        if(status == EntitlementStatus.SUCCESS) {
            return;
        }
        if(status == null) {
            communicationsChannel.onPlaybackError(new NotEntitledError(status), true);
            return;
        }
        switch (status) {
            case NOT_AVAILABLE: {
                communicationsChannel.onPlaybackError(new NotAvailableError(), true);
            } break;
            case BLOCKED: {
                communicationsChannel.onPlaybackError(new AssetBlockedError(), true);
            } break;
            case GEO_BLOCKED: {
                communicationsChannel.onPlaybackError(new GeoBlockedError(), true);
            } break;
            case CONCURRENT_STREAMS_LIMIT_REACHED: {
                communicationsChannel.onPlaybackError(new ConcurrentStreamsLimitReachedError(), true);
            } break;
            case NOT_PUBLISHED: {
                communicationsChannel.onPlaybackError(new NotPublishedError(), true);
            } break;
            case NOT_ENTITLED: {
                communicationsChannel.onPlaybackError(new NotEntitledError(EntitlementStatus.NOT_ENTITLED), true);
            } break;
            default: {
                communicationsChannel.onPlaybackError(new NotEntitledError(status), true);
            } break;
        }
    }

    protected ITimeProvider createTimeProviderForCache() {
        return new SystemBootTimeProvider();
    }

    protected void checkEntitlementForProgram(IProgram toProgram) {
        if (streamPrograms != null) {
            if (toProgram == null) {
                toProgram = streamPrograms.getProgram();
            }
            AssetIdFallbackChain assetId = getAssetIdsToCheckForAt(toProgram);
            if (assetId != null) {
                checkEntitlement(assetId);
            }
        } else {
            String channelId = streamInfo.getChannelId();
            if (channelId != null) {
                checkEntitlement(new AssetIdFallbackChain(channelId));
            }
        }
    }

    private AssetIdFallbackChain getAssetIdsToCheckForAt(IProgram toProgram) {
        String channelId = streamInfo.getChannelId();
        List<String> fallbackChain = new ArrayList<>();
        if(toProgram != null) {
            fallbackChain.add(toProgram.getAssetId());
        }
        if(channelId != null) {
            fallbackChain.add(channelId);
        }
        if(fallbackChain.isEmpty()) {
            return null;
        } else {
            return new AssetIdFallbackChain(fallbackChain);
        }
    }

    private void checkEntitlement(final AssetIdFallbackChain assetIds) {
        IEntitlementResponseHandler responseHandler = new IEntitlementResponseHandler() {
            @Override
            public void onResponse(EntitlementData entitlementData) {
                OpenContainerUtil.setValueSynchronized(currentEntitlementData, entitlementData, (oldValue, newValue) -> entitlementCollector.onEntitlementChanged(oldValue, newValue));
            }

            @Override
            public void onError(EnigmaError error) {
                error.printStackTrace();
                //Might have been a temporary network disconnection. Ignore and try again later.
            }
        };

        ICachedEntitlementResponse cachedResponse = null;
        synchronized (cachedEntitlementResponses) {
            Iterator<ICachedEntitlementResponse> iterator = cachedEntitlementResponses.iterator();
            while (iterator.hasNext()) {
                ICachedEntitlementResponse cachedEntitlementResponse = iterator.next();
                if(cachedEntitlementResponse.hasExpired()) {
                    iterator.remove();
                } else {
                    if(assetIds.getAssetId().equals(cachedEntitlementResponse.getAssetId())) {
                        cachedResponse = cachedEntitlementResponse;
                    }
                }
            }
        }
        if(cachedResponse != null) {
            cachedResponse.use(responseHandler);
        } else {
            entitlementProvider.checkEntitlement(new EntitlementRequest(session, assetIds.getAssetId()), responseHandler);
        }
    }

    protected void addEntitlementListener(IEntitlementListener listener) {
        entitlementCollector.addListener(listener);
    }

    private interface ICachedEntitlementResponse {
        boolean hasExpired();
        String getAssetId();
        void use(IEntitlementResponseHandler responseHandler);
    }

    private static class CachedEntitlementResponse implements ICachedEntitlementResponse {
        private final String assetId;
        private final EntitlementData entitlementData;
        private final ITimeProvider timeProvider;
        private final long expirationTime;

        public CachedEntitlementResponse(String assetId, EntitlementData entitlementData, Duration cacheTime, ITimeProvider timeProvider) {
            this.assetId = assetId;
            this.entitlementData = entitlementData;
            if(!timeProvider.isReady(cacheTime)) {
                this.expirationTime = System.currentTimeMillis()+cacheTime.inWholeUnits(Duration.Unit.MILLISECONDS);
            } else {
                this.expirationTime = Duration.millis(timeProvider.getTime()).add(cacheTime).inWholeUnits(Duration.Unit.MILLISECONDS);
            }
            this.timeProvider = timeProvider;
        }

        @Override
        public boolean hasExpired() {
            if(timeProvider.isReady(Duration.seconds(0))) {
                return timeProvider.getTime() > expirationTime;
            }
            return false;
        }

        @Override
        public String getAssetId() {
            return assetId;
        }

        @Override
        public void use(IEntitlementResponseHandler responseHandler) {
            responseHandler.onResponse(entitlementData);
        }
    }

    private static class AssetIdFallbackChain {
        private final List<String> assetIds;

        public AssetIdFallbackChain(String ... assetIds) {
            this(Arrays.asList(assetIds));
        }

        public AssetIdFallbackChain(List<String> assetIds) {
            this.assetIds = assetIds;
        }

        public boolean hasMoreFallbacks() {
            return assetIds.size() > 1;
        }

        public AssetIdFallbackChain fallback() {
            if(!hasMoreFallbacks()) {
                throw new IllegalStateException();
            }
            return new AssetIdFallbackChain(new ArrayList<>(assetIds.subList(1, assetIds.size())));
        }

        public String getAssetId() {
            return assetIds.get(0);
        }
    }
}
