<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

# Live drift correction (ExoPlayer)
This tutorial describes how to use drift correction for live streams when using the
ExoPlayerIntegration module.

## Prerequisites

This tutorial assumes that you have:
* Have completed the [Basics series](../basics/introduction.md) or have a basic app using the SDK working.
* Are using the ExoPlayerIntegration module and `ExoPlayerTech` as player implementation.

## Typical usage

Add a `DriftCorrector` to `ExoPlayerTech`.
```
exoPlayerTech.addDriftListener(new DriftCorrector());
```

Live streams will now be automatically keep their initial live latency. If the stream starts to lag behind
more than 0.1 seconds the playback speed will be increased (by a factor of 1.3 by default) to catch up.

## Advanced usage

This example show how to implement drift correction that adjusts the playback speed only when the drift is
smaller than 5 seconds, and seeks to catch up otherwise.

```
exoPlayerTech.addDriftListener(new IDriftListener() {
    private final float speedAdjustment = 1.3f;
    @Override
    public void onDriftUpdated(ISpeedHandler speedHandler, Duration drift) {
        float driftInSeconds = drift.inUnits(Duration.Unit.SECONDS);
        if(Math.abs(driftInSeconds) < 5f) {
            if(driftInSeconds > 0.1f) {
                speedHandler.setPlaybackSpeed(speedAdjustment);
            } else if(driftInSeconds < -0.1f) {
                speedHandler.setPlaybackSpeed(1f/speedAdjustment);
            } else {
                speedHandler.setPlaybackSpeed(1f);
            }
        } else {
            ITimelinePosition currentPosition = enigmaPlayer.getTimeline().getCurrentPosition();
            if(currentPosition != null) {
                enigmaPlayer.getControls().seekTo(currentPosition.add(drift));
            }
        }
    }
});
```


___
[Table of Contents](../index.md)<br/>
