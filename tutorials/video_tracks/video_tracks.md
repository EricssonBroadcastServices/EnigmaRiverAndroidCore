<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

### Video tracks
# Video tracks selection
## Keeping track of tracks

The current `PlaybackSession` manages available and selected audio/subtitle tracks.

```java
public interface IPlaybackSession {
    ...
    List<IVideoTrack> getVideoTracks();
    IVideoTrack getSelectedVideoTrack();
    ...
}
```

The `IPlaybackSessionListener` interface also contains events for when these values change.

```java
public interface IPlaybackSessionListener {
    ...
    void onVideoTracks(List<IVideoTrack> tracks);
    void onSelectedVideoTrackChanged(IVideoTrack oldSelectedTrack, IVideoTrack newSelectedTrack);
    ...
}
```

These events can be used to initiate and update UI elements.

#### Note
> `onVideoTracks(...)` can get called at *any* time. This is dependent on the current stream and the playerImplementation.


### Example - Video track

## Changing selected video track

Video track selection can be Adaptive or Fixed:

Adaptive video track selection: use following API and SDK to optimize the video track selection based on network conditions

Read more about Adaptive strategy here : https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/trackselection/AdaptiveTrackSelection.html

```java
public void selectVideoTrack(IVideoTrack audioTrack) {
        enigmaPlayer.getControls().setMaxVideoTrackDimensions(object);
}
```

The following API can be used to force the selected video track:
```java
public void selectVideoTrack(IVideoTrack audioTrack) {
        enigmaPlayer.getControls().setVideoTrack(object);
}
```

It will toggle between adaptive strategy and fixed video track and vice-versa. 


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
&bull; Video tracks selection (current)<br/>
