<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

# Overriding media format selection logic
## How EnigmaPlayer selects a media format

An asset present in the Red Bee Media OTT backend may have several available media formats. In this context
a media format is the combination of a *stream format* (DASH/HLS/etc) and a *drm technology* (widevine/fairplay/playready/none).

When calling `enigmaPlayer.play(playRequest)` with an `AssetPlayable` the following steps are taken
to decide which media format will be played.
1. EnigmaPlayer makes a "play-call" to the exposure backend. The JSON-response contains a set of
available media formats for the asset.
1. EnigmaPlayer queries it's current player implementation about which of these formats are supported.
1. EnigmaPlayer uses a default `IMediaFormatSelector` to make an initial decision on which format to use.
This tentative selection is called the **"prospect"** and may be changed by other `IMediaFormatSelector`s
further down the chain.
1. If a custom media format selector has been provided for the EnigmaPlayer
(either by `EnigmaPlayer#setMediaFormatSelector` or `EnigmaPlayer#setMediaFormatPreference`) it is also
queried and allowed to change the prospect.
1. If the PlaybackProperties of the PlayRequest has a `IMediaFormatSelector` set this is also queried.
1. After this the prospect is marked as the selection and the corresponding media source is sent to
the player implementation to be loaded.

## Supplying a custom media format selector

We can supply a custom `IMediaFormatSelector` to EnigmaPlayer by
calling `EnigmaPlayer#setMediaFormatSelector(IMediaFormatSelector)`. This is typically done during setup of the player.

We can also use the convenience method `EnigmaPlayer#setMediaFormatPreference(EnigmaMediaFormat...)`
and send in a preference order of media formats. Calling this method is equivalent to calling
```
enigmaPlayer.setMediaFormatSelector(new SimpleMediaFormatSelector(EnigmaMediaFormat...));
```

#### Simple example using `EnigmaPlayer#setMediaFormatPreference(EnigmaMediaFormat...)`

This will cause `EnigmaPlayer` to select HLS (preferring drm-protected) if it exists for an asset
and the current player implementation supports it.

```java
...
    private IEnigmaPlayer createEnigmaPlayer(ISession session, IPlayerImplementation playerImplementation) {
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(session, playerImplementation);
        ...
        //Prefer HLS
        enigmaPlayer.setMediaFormatPreference(
            EnigmaMediaFormat.HLS().fairplay(),
            EnigmaMediaFormat.HLS().unenc()
        );
        return enigmaPlayer;
    }
...
```

#### Advanced example using `EnigmaPlayer#setMediaFormatSelector(IMediaFormatSelector)`

Using `EnigmaPlayer#setMediaFormatSelector(IMediaFormatSelector)` we can supply a custom
`IMediaFormatSelector`-implementation and have more advanced logic. In this example we will
implement a `IMediaFormatSelector` that preferes unencrypted (not drm-protected) streams over
encrypted streams.

```java
...
    private IEnigmaPlayer createEnigmaPlayer(ISession session, IPlayerImplementation playerImplementation) {
        EnigmaPlayer enigmaPlayer = new EnigmaPlayer(session, playerImplementation);
        ...
        //Prefer unencrypted
        enigmaPlayer.setMediaFormatSelector(new UnencryptedMediaFormatSelector());
        return enigmaPlayer;
    }
...
private static class UnencryptedMediaFormatSelector implements IMediaFormatSelector {
    @Override
    public EnigmaMediaFormat select(EnigmaMediaFormat prospect, Collection<EnigmaMediaFormat> available) {
        if(prospect != null) {
            //If try to select corresponding unencrypted format for prospect
            if(prospect.getDrmTechnology() != EnigmaMediaFormat.DrmTechnology.NONE) {
                EnigmaMediaFormat unencrypted = new EnigmaMediaFormat(prospect.getStreamFormat(), EnigmaMediaFormat.DrmTechnology.NONE);
                if(available.contains(unencrypted)) {
                    return unencrypted;
                }
            }
            return prospect;
        } else {
            for(EnigmaMediaFormat enigmaMediaFormat : available) {
                if(enigmaMediaFormat.getDrmTechnology() == EnigmaMediaFormat.DrmTechnology.NONE) {
                    return enigmaMediaFormat;
                }
            }
            return null;
        }
    }
}
...
```

## Overriding the media format selection logic for a single play request

We can also provide a `IMediaFormatSelector` to be applied for a specific PlayRequest. This is
done by calling

`PlaybackProperties#setMediaFormatSelector(IMediaFormatSelector)`

or the convenience method

`PlaybackProperties#setMediaFormatPreference(EnigmaMediaFormat...)`

on the PlaybackProperties object passed to the PlayRequest.

```java
...
private IPlaybackProperties getPlaybackProperties(EnigmaMediaFormat... preferredFormats) {
    PlaybackProperties playbackProperties = new PlaybackProperties();
    playbackProperties.setMediaFormatPreference(preferredFormats);
    return playbackProperties;
}
...
private void playInEnigmaPlayer(IPlayable playable) {
    IPlaybackProperties playbackProperties = getPlaybackProperties(
        EnigmaMediaFormat.DASH().widevine(),
        EnigmaMediaFormat.HLS().unenc(),
    );
    enigmaPlayer.play(new PlayRequest(playable, playbackProperties, ,,,));
}
...
```

## Overriding the media format selection logic for downloads

In the [Downloads and offline playback series](../index.md#downloads-and-offline-playback-series) we learn how to use the download API.

Similarly to playback we can provide a custom `IMediaFormatSelector` to select media format for
download. This can either be set on the `EnigmaDownloadContext` during initialization
(global for downloads), or it can be provided for a single `DownloadStartRequest` (similar to
"Overriding the media format selection logic for a single play request").

### Setting the default media format selection logic for downloads

Setting the default media format selector for downloads should be done when initializing the SDK.

```java
public class MyApplication extends Application {
    ...
    @Override
    public void onCreate() {
        super.onCreate();
        ...
        EnigmaRiverContext.EnigmaRiverContextInitialization initialization = new EnigmaRiverContext.EnigmaRiverContextInitialization(exposureBaseUrl);
        ...
        initialization.forModule(EnigmaDownloadContext.MODULE_INFO).setDefaultDownloadFormatSelector(new IMediaFormatSelector() {
            @Override
            public EnigmaMediaFormat select(EnigmaMediaFormat prospect, Collection<EnigmaMediaFormat> available) {
                // ... select media format ... //
            }
        });
        ...
        EnigmaRiverContext.initialize(this, initialization);
    }
    ...
}
```

### Overriding the default media format selection logic for a single DownloadStartRequest

We can provide a `IMediaFormatSelector` to be applied for a specific DownloadStartRequest. This is
done by calling

`DownloadStartRequest#setMediaFormatSelector(IMediaFormatSelector)`

```java
IEnigmaDownload enigmaDownload = new EnigmaDownload(businessUnit);
...
DownloadStartRequest downloadStartRequest = new DownloadStartRequest(assetId, session);
downloadStartRequest.setMediaFormatSelector(new IMediaFormatSelector() {
    @Override
    public EnigmaMediaFormat select(EnigmaMediaFormat prospect, Collection<EnigmaMediaFormat> available) {
        // ... select media format ... //
    }
});
enigmaDownload.startAssetDownload(context downloadStartRequest, resultHandler);
```

## Forcing DRM L3 securityLevel

ExoPlayerTech initialization provides an option that would allow you to force L3 DRM when creating the player (under the hood we would use ExoPlayer FrameworkMediaDrm setPropertyString("securityLevel", "L3") ). 
But that should only be done on devices where this is really necessary, so app-developer would have to maintain such a list and you need to implement some logic around that in the frontend.


```java
boolean useDrmSecurityLevelL3 = true;
new ExoPlayerTech(this, getString(R.string.app_name), useDrmSecurityLevelL3)

```


___
[Table of Contents](../index.md)<br/>
