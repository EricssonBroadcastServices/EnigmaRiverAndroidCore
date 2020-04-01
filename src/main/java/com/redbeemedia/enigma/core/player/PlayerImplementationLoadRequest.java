package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.time.Duration;

/*package-protected*/ class PlayerImplementationLoadRequest implements IPlayerImplementationControls.ILoadRequest {
    private final String url;
    private final Integer maxBitrate;
    private final Integer maxResolutionHeight;
    private Duration liveDelay = null;

    public PlayerImplementationLoadRequest(String url, IContractRestrictions contractRestrictions) {
        this.url = url;
        this.maxBitrate = contractRestrictions.getValue(ContractRestriction.MAX_BITRATE, null);
        this.maxResolutionHeight = contractRestrictions.getValue(ContractRestriction.MAX_RES_HEIGHT, null);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Integer getMaxBitrate() {
        return maxBitrate;
    }

    @Override
    public Integer getMaxResoultionHeight() {
        return maxResolutionHeight;
    }

    public PlayerImplementationLoadRequest setLiveDelay(Duration liveDelay) {
        this.liveDelay = liveDelay;
        return this;
    }

    @Override
    public Duration getLiveDelay() {
        return liveDelay;
    }
}
