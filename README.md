# Core

The core library, version 3.4.8-BETA-2.

See the [tutorials](tutorials/index.md) for more information.

## Other modules

### [ExoPlayerIntegration](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidExoPlayerIntegration/tree/3.4.8-BETA-2)

<p>Player implementation module that uses ExoPlayer.</p>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exoplayerintegration:3.4.8-BETA-2"
```

### [Cast](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidCast/tree/3.4.8-BETA-2)

<p>Optional module for easy integration with chromecast</p>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:cast:3.4.8-BETA-2"
```

### [Download](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidDownload/tree/3.4.8-BETA-2)

<p>Download and offline playback extension module that exposes Enigma River download API.</p>
<h4 style="margin-top: -1em">Note: Needs a download implementation module to be used! Such as ExoPlayerIntegration</h4>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:download:3.4.8-BETA-2"
```

### [ExoPlayerDownload](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidExoPlayerDownload/tree/3.4.8-BETA-2)

<p>Download implementation module for the `download` module that uses ExoPlayer to power downloads and offline playback. Requires ExoPlayerIntegration as the player implementation.</p>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exoPlayerDownload:3.4.8-BETA-2"
```

### [ExposureUtils](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidExposureUtils/tree/3.4.8-BETA-2)

<p>Optional utility module for converting json-responses to native java/Android objects</p>

```gradle
implementation "com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exposureUtils:3.4.8-BETA-2"
```