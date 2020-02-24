package com.redbeemedia.enigma.core.virtualui;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.time.Duration;

public class VirtualControlsSettings implements IVirtualControlsSettings {
    private Duration seekForwardStep = Duration.seconds(15);
    private Duration seekBackwardStep = Duration.seconds(5);

    @Override
    @NonNull
    public Duration getSeekForwardStep() {
        return seekForwardStep;
    }

    @Override
    @NonNull
    public Duration getSeekBackwardStep() {
        return seekBackwardStep;
    }

    public VirtualControlsSettings setSeekBackwardStep(@NonNull Duration seekBackwardStep) {
        this.seekBackwardStep = seekBackwardStep;
        return this;
    }

    public VirtualControlsSettings setSeekForwardStep(@NonNull Duration seekForwardStep) {
        this.seekForwardStep = seekForwardStep;
        return this;
    }
}
