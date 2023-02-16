### Downloads and offline playback series
# Gradle dependencies
To use the download- and offline playback-functionality, your project will need a few extra
Enigma River Android SDK modules.

### Prerequisite dependencies
It is assumed that you already have the dependencies to `core` and `exoplayerintegration` in your
`build.gradle`:
```gradle
// in build.gradle of your app-module
...
dependencies {
    ...
    implementation 'com.github.EricssonBroadcastServices.EnigmaRiverAndroid:core:r3.5.0'
    implementation 'com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exoplayerintegration:r3.5.0'
    ...
}
...
```


## The Download module
This module provides a player implementation independent interface for working with downloads
together with the Red Bee Media OTT backend and the rest of the Enigma River Android SDK.

Add this to you `build.gradle`:
```gradle
// in build.gradle of your app-module
...
dependencies {
    ...
    implementation 'com.github.EricssonBroadcastServices.EnigmaRiverAndroid:download:r3.5.0'
    ...
}
...
```

## The ExoPlayerDownload module
In addition to the Download module, you will need a player implementation specific module to serve
as the bridge between the Download module and the player implementation module (in our case
the ExoPlayer integration module).

Add this to you `build.gradle`:
```gradle
// in build.gradle of your app-module
...
dependencies {
    ...
    implementation 'com.github.EricssonBroadcastServices.EnigmaRiverAndroid:exoPlayerDownload:r3.5.0'
    ...
}
...
```


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
&bull; Gradle dependencies (current)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
[Using the download API](enigma_download.md)<br/>
[Get available tracks for download](get_download_info.md)<br/>
[Start asset download](start_download.md)<br/>
[Managing ongoing downloads](ongoing_downloads.md)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
