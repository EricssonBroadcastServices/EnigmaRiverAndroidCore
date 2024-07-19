// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.time.Duration;

/*package-protected*/ abstract class PlayerImplementationLoadRequest implements IPlayerImplementationControls.ILoadRequest {
    private final Integer maxBitrate;
    private final Integer maxResolutionHeight;
    private Duration liveDelay = null;

    public PlayerImplementationLoadRequest(IContractRestrictions contractRestrictions) {
        this.maxBitrate = contractRestrictions.getValue(ContractRestriction.MAX_BITRATE, null);
        this.maxResolutionHeight = contractRestrictions.getValue(ContractRestriction.MAX_RES_HEIGHT, null);
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

    /*package-protected*/ static class Stream
            extends PlayerImplementationLoadRequest
            implements IPlayerImplementationControls.IStreamLoadRequest {

        private final String url;


        public Stream(String url, IContractRestrictions contractRestrictions) {
            super(contractRestrictions);
            this.url = url;
        }

        @Override
        public String getUrl() {
            return url;
        }
    }

    /*package-protected*/ static class Download
            extends PlayerImplementationLoadRequest
            implements IPlayerImplementationControls.IDownloadedLoadRequest {

        private final Object downloadData;


        public Download(Object downloadData, IContractRestrictions contractRestrictions) {
            super(contractRestrictions);
            this.downloadData = downloadData;
        }

        @Override
        public Object getDownloadData() {
            return downloadData;
        }
    }
}
