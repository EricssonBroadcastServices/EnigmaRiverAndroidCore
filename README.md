# Core

The core library, version r3.7.5-BETA-7.

See the [tutorials](tutorials/index.md) for more information.

## Other modules

### [ExoPlayerIntegration](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidExoPlayerIntegration/tree/r3.7.5-BETA-7)

<p>Player implementation module that uses ExoPlayer.</p>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exoplayerintegration:r3.7.5-BETA-7"
```

### [Cast](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidCast/tree/r3.7.5-BETA-7)

<p>Optional module for easy integration with chromecast</p>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:cast:r3.7.5-BETA-7"
```

### [Download](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidDownload/tree/r3.7.5-BETA-7)

<p>Download and offline playback extension module that exposes Enigma River download API.</p>
<h4 style="margin-top: -1em">Note: Needs a download implementation module to be used! Such as ExoPlayerIntegration</h4>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:download:r3.7.5-BETA-7"
```

### [ExoPlayerDownload](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidExoPlayerDownload/tree/r3.7.5-BETA-7)

<p>Download implementation module for the `download` module that uses ExoPlayer to power downloads and offline playback. Requires ExoPlayerIntegration as the player implementation.</p>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exoPlayerDownload:r3.7.5-BETA-7"
```

### [ExposureUtils](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidExposureUtils/tree/r3.7.5-BETA-7)

<p>Optional utility module for converting json-responses to native java/Android objects</p>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exposureUtils:r3.7.5-BETA-7"
```