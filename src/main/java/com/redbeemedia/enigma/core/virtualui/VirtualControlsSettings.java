package com.redbeemedia.enigma.core.virtualui;

import com.redbeemedia.enigma.core.time.Duration;

public class VirtualControlsSettings implements IVirtualControlsSettings {
    private Duration seekForwardStep = Duration.seconds(15);
    private Duration seekBackwardStep = Duration.seconds(5);

    @Override
    public Duration getSeekForwardStep() {
        return seekForwardStep;
    }

    @Override
    public Duration getSeekBackwardStep() {
        return seekBackwardStep;
    }

    public VirtualControlsSettings setSeekBackwardStep(Duration seekBackwardStep) {
        this.seekBackwardStep = seekBackwardStep;
        return this;
    }

    public VirtualControlsSettings setSeekForwardStep(Duration seekForwardStep) {
        this.seekForwardStep = seekForwardStep;
        return this;
    }
}
