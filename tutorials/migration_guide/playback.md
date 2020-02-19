### Migration guide (Android SDK 2.0 -> Enigma River Android SDK)
# Changes to playback
## Initiating playback

### Before
Playback was started by passing `EmpAsset`, `EmpProgram` or similar as a 'playable' in a start
activity intent that was handled in `SimplePlaybackActivity`.
```
EmpAsset asset = new EmpAsset();
asset.assetId = "MY_ASSET_ID";

Intent intent = new Intent(context, MyVideoPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.putExtra("playable", asset);
context.startActivity(intent);
```

### Now

Since the Enigma River Android SDK does not provide UI components the app developer needs to
create their own playback activity and set up the EnigmaPlayer. Different types of playables are
now aligned into `AssetPlayable` where assets, programs and channels are all referred to by their
assetId.

See the [Basics series](../basics/prerequisites.md) for an introduction to playback.

## PlaybackProperties

### Before
`net.ericsson.emovs.playback.PlaybackProperties` was used.

### Now
`com.redbeemedia.enigma.core.playrequest.PlaybackProperties` replaces the old PlaybackProperties. It
works essentially the same, although some capabilities are not yet supported.

It is passed as an optional parameter to the `PlayRequest` constructor.


## Contract restrictions

### Before
Contract restrictions were contained in the `Entitlement` object that belonged to the `EMPPlayer`.
```
Entitlement entitlement = empPlayer.getEntitlement();

if(entitlement.ffEnabled) {
    // Fast-forwarding is enabled
}

if(entitlement.rwEnabled) {
    // Rewinding is enabled
}

if(entitlement.timeshiftEnabled) {
    // timeshifting is enabled. In the case where timeshift is disabled you cannot pause the playback.
}
```


### Now

Contract restrictions are represented by the `IContractRestrictions` object and owned by the
`PlaybackSession`. To get a reference to a `PlaybackSession` the app developer must listen to
changes of the current `PlaybackSession` on the `EnigmaPlayer`.

Ex:
```
...
IEnigmaPlayer enigmaPlayer = ...;
...
enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
    @Override
    public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
        // Playback session changed
        if(to != null) {
            IContractRestrictions contractRestrictions = to.getContractRestrictions();

            if(contractRestrictions.getValue(ContractRestriction.FASTFORWARD_ENABLED, true)) {
                // Fast-forwarding is enabled
            }

            if(contractRestrictions.getValue(ContractRestriction.REWIND_ENABLED, true)) {
                // Rewinding is enabled
            }

            if(contractRestrictions.getValue(ContractRestriction.TIMESHIFT_ENABLED, true)) {
                // timeshifting is enabled. In the case where timeshift is disabled you cannot pause the playback.
            }
        }
    }
});

```
To listen for changes to the contract restrictions during a playback session the app developer can
add a listener to the playback session
```
...
playbackSession..addListener(new BasePlaybackSessionListener() {
    @Override
    public void onContractRestrictionsChanged(IContractRestrictions oldContractRestrictions, IContractRestrictions newContractRestrictions) {
        this.contractRestrictions = newContractRestrictions; //Contract restrictions has been updated
    }
});
...
```

Methods such as `empPlayer.canSeekForward()` and `empPlayer.canSeekBackwards()` now belong to the
`PlaybackSession` instead. For example, `playbackSession.isSeekAllowed()` and `playbackSession.isSeekToLiveAllowed()`.

### Note
Please note that manually checking contract restrictions and querying `empPlayer.canSeekForward()`
etc., is no longer needed since the introduction of virtual controls.
See [virtual controls](../advanced_topics/virtual_controls.md).

## Subtitle tracks and audio tracks

### Before
For controlling subtitles
* `getSelectedTextLanguage()`
* `getTextLanguages()`
* `setTextLanguage(...)`

was available on the player object.
<br />
And for controlling audio tracks
* `getSelectedAudioLanguage()`
* `getAudioLanguages()`
* `setAudioLanguage(...)`

was available on the player object.

### Now

Current selected and available subtitle- and audio-tracks now belong to the `PlaybackSession` object and can be accessed through:
```
public interface IPlaybackSession {
...
    List<ISubtitleTrack> getSubtitleTracks();
    ISubtitleTrack getSelectedSubtitleTrack();

    List<IAudioTrack> getAudioTracks();
    IAudioTrack getSelectedAudioTrack();
...
```

Changing the selected subtitle- and audio-track is done by calling
```
enigmaPlayer.getControls().setSubtitleTrack(subtitleTrack);
```
and
```
enigmaPlayer.getControls().setAudioTrack(audioTrack);
```
respectively.

Also note that the `subtitleTrack` and `audioTrack` used in the set-methods must originate from the
list of available tracks returned by `getSubtitleTracks()` and `getAudioTracks()`.


___
[Table of Contents](../index.md)<br/>
[Introduction](introduction.md)<br/>
[Structural changes](structural_changes.md)<br/>
[Changes to SDK initialization](sdk_initialization.md)<br/>
[Changes to authentication/login](login.md)<br/>
[Changes to asset metadata retrieval](asset_metadata.md)<br/>
Changes to playback (current)<br/>
[Further reading](further_reading.md)<br/>
