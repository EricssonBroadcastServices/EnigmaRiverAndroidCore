// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.virtualui.impl;

import android.util.Log;

import com.redbeemedia.enigma.core.ads.VastAdEntry;
import com.redbeemedia.enigma.core.ads.VideoClicks;
import com.redbeemedia.enigma.core.http.SimpleHttpCall;

import java.net.URL;
import java.util.List;

/*package-protected*/ class VideoAdClickButton extends AbstractVirtualButtonImpl {

    public VideoAdClickButton(IVirtualButtonContainer container) {
        super(container, false);
    }

    @Override
    protected boolean calculateRelevant(IVirtualButtonContainer container) {
        return true;
    }

    @Override
    protected boolean calculateEnabled(IVirtualButtonContainer container) {
        VastAdEntry currentAdEntry = container.getEnigmaPlayer().getAdDetector().getCurrentAdEntry();
        if (currentAdEntry != null) {
            VideoClicks videoClicks = currentAdEntry.getVideoClicks();
            if (videoClicks != null) {
                String clickThroughUrl = videoClicks.getClickThroughUrl();
                return clickThroughUrl != null;
            }
        }
        return false;
    }

    @Override
    protected void onClick(IVirtualButtonContainer container) {
        // Sending tracking
        container.getEnigmaPlayer().getAdDetector().sendVideoAdClickImpression();
    }
}
