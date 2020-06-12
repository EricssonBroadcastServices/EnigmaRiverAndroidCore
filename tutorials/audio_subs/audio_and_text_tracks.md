### Audio and subtitles series (work in progress)
# Audio and subtitle selection
## Keeping track of tracks

The current `PlaybackSession` manages available and selected audio/subtitle tracks.

```java
public interface IPlaybackSession {
    ...
    List<ISubtitleTrack> getSubtitleTracks();
    ISubtitleTrack getSelectedSubtitleTrack();

    List<IAudioTrack> getAudioTracks();
    IAudioTrack getSelectedAudioTrack();
    ...
}
```

The `IPlaybackSessionListener` interface also contains events for when these values change.

```java
public interface IPlaybackSessionListener {
    ...
    void onSubtitleTracks(List<ISubtitleTrack> tracks);
    void onSelectedSubtitleTrackChanged(ISubtitleTrack oldSelectedTrack, ISubtitleTrack newSelectedTrack);

    void onAudioTracks(List<IAudioTrack> tracks);
    void onSelectedAudioTrackChanged(IAudioTrack oldSelectedTrack, IAudioTrack newSelectedTrack);
    ...
}
```

These events can be used to initiate and update UI elements.

#### Note
> `onSubtitleTracks(...)` and `onAudioTracks(...)` can get called at *any* time. This is dependent on the current stream and the playerImplementation.


### Example - Audio

In this example the variable `audioTrackUi` represents some object or callback used for updating the UI.

```java
public void setupAudioSelector(IEnigmaPlayer enigmaPlayer) {
    enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
        //Listener for currently active PlaybackSession
        private final IPlaybackSessionListener activePlaybackSessionListener = new BasePlaybackSessionListener() {
            @Override
            public void onAudioTracks(List<IAudioTrack> tracks) {
                //Update UI
                audioTrackUi.setAvailableAudioLanguages(tracks);
            }

            @Override
            public void onSelectedAudioTrackChanged(IAudioTrack oldSelectedTrack, IAudioTrack newSelectedTrack) {
                //Update UI
                audioTrackUi.setSelectedAudioLanguage(newSelectedTrack);
            }
        };

        @Override
        public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
            if(from != null) {
                //Remove listener from old PlaybackSession
                from.removeListener(activePlaybackSessionListener);
            }
            if(to != null) {
                //Update UI
                audioTrackUi.setAvailableAudioLanguages(to.getAudioTracks());
                audioTrackUi.setSelectedAudioLanguage(to.getSelectedAudioTrack());

                //Add listener to new PlaybackSession
                to.addListener(activePlaybackSessionListener, handler);
            }
        }
    }, handler);
}
```

While this example is only for audio, the same pattern applies to subtitles as well.

## Changing selected audio/subtitle track

To change the selected audio/subtitle track, simply send one of the available audio/subtitle track-objects into `IEnigmaPlayerControls#set{Audio/Subtitle}Track(...)`.
```java
public void selectSubtitleTrack(ISubtitleTrack subtitleTrack) {
    enigmaPlayer.getControls().setSubtitleTrack(subtitleTrack);
}

public void selectAudioTrack(IAudioTrack audioTrack) {
    enigmaPlayer.getControls().setAudioTrack(audioTrack);
}
```

To turn off subtitles, simply select `null` as the subtitle track.

```java
public void turnOffSubtitles() {
    enigmaPlayer.getControls().setSubtitleTrack(null);
}
```

This is only allowed for subtitle tracks. A request to `setAudioTrack(null)` will be rejected.

#### Note
> Objects passed to `set{Audio/Subtitle}Track(...)` must originate from EnigmaPlayer. Using a custom implementation of `IAudioTrack` or `ISubtitleTrack` will result in a rejected request.

### Working example
Here is a working example project using what is described in this tutorial:
[audioAndSubtitles](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidTutorialApps/tree/r3.0.4/audioAndSubtitles)<br />


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
Audio and subtitle selection (current)<br/>
