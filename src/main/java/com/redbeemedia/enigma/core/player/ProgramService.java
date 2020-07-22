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
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.SystemBootTimeProvider;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/*package-protected*/ class ProgramService implements IInternalPlaybackSessionListener {
    private static final long PROGRAM_SERVICE_ENTITLEMENT_CHECK_INTERVAL_MILLIS = 4000L;

    private final ISession session;
    private final IStreamInfo streamInfo;
    private final IPlaybackSessionInfo playbackSessionInfo;
    private final IStreamPrograms streamPrograms;
    private final IEntitlementProvider entitlementProvider;
    private final ITimeProvider timeProviderForCache;
    private final Repeater checkEntitlementRepeater;
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
        this.checkEntitlementRepeater = new Repeater(taskFactoryProvider.getMainThreadTaskFactory(), PROGRAM_SERVICE_ENTITLEMENT_CHECK_INTERVAL_MILLIS, this::checkEntitlement);
        this.playerListener = new BaseEnigmaPlayerListener() {
            @Override
            public void onProgramChanged(IProgram from, IProgram to) {
                checkEntitlementRepeater.executeNow();
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
        checkEntitlementRepeater.setEnabled(true);
        args.enigmaPlayer.addListener(playerListener);
    }

    @Override
    public void onStop(OnStopArgs args) {
        checkEntitlementRepeater.setEnabled(false);
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

    protected void checkEntitlement() {
        if(streamPrograms != null) {
            Duration offset = playbackSessionInfo.getCurrentPlaybackOffset();
            if(lastCheckedOffset != null && lastCheckedOffset.equals(offset)) {
                return;
            } else {
                lastCheckedOffset = offset;
            }
            AssetIdFallbackChain assetId = getAssetIdsToCheckForAt(offset);
            if(assetId != null) {
                checkEntitlement(assetId);
            }
            // Here 'fuzzy' refers to the fact that the time the entitlement is checked for is not exact (it is fuzzy).
            // The terminology is borrowed from the term 'fuzzy logic'. The reason behind this fuzzy check
            // is to spread out the calls to the backend over a time period (see parameters passed to generateFuzzyOffset())
            // so that the backend is not overloaded when a live stream changes programs and all clients, at the same time,
            // wants to do an entitlement check for the new program.
            if(usesFuzzyCheck()) {
                Duration fuzz = generateFuzzyOffset(Duration.seconds(30), Duration.minutes(2));
                Duration futurePointOffset = offset.add(fuzz);
                AssetIdFallbackChain futureAssetId = getAssetIdsToCheckForAt(futurePointOffset);
                if(futureAssetId != null) {
                    if(!Objects.equals(assetId.getAssetId(), futureAssetId.getAssetId())) {
                        checkFutureEntitlementAndCache(futureAssetId, futurePointOffset, Duration.minutes(5));
                    }
                }
            }
        } else {
            String channelId = streamInfo.getChannelId();
            if(channelId != null) {
                checkEntitlement(new AssetIdFallbackChain(channelId));
            }
        }
    }

    protected Duration generateFuzzyOffset(Duration min, Duration max) {
        return min.add(max.subtract(min).multiply((float) Math.random()));
    }

    private boolean usesFuzzyCheck() {
        return playingFromLive && streamPrograms != null;
    }

    private AssetIdFallbackChain getAssetIdsToCheckForAt(Duration offset) {
        IProgram program = streamPrograms.getProgramAtOffset(offset.inWholeUnits(Duration.Unit.MILLISECONDS));
        String channelId = streamInfo.getChannelId();
        List<String> fallbackChain = new ArrayList<>();
        if(program != null) {
            fallbackChain.add(program.getAssetId());
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

    private void checkFutureEntitlementAndCache(final AssetIdFallbackChain assetId, Duration offset, Duration cacheTime) {
        long time = streamInfo.getStart(Duration.Unit.MILLISECONDS) + offset.inWholeUnits(Duration.Unit.MILLISECONDS);
        EntitlementRequest entitlementRequest = new EntitlementRequest(session, assetId.getAssetId()).setTime(time);
        entitlementProvider.checkEntitlement(entitlementRequest, new IEntitlementResponseHandler() {
            @Override
            public void onResponse(EntitlementData entitlementData) {
                synchronized (cachedEntitlementResponses) {
                    cachedEntitlementResponses.add(new CachedEntitlementResponse(assetId.getAssetId(), entitlementData, cacheTime, timeProviderForCache));
                }
            }

            @Override
            public void onError(EnigmaError error) {
                error.printStackTrace();
                //Too bad
            }
        });
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
