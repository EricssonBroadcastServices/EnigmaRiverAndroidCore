<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

### Additional topics for download and offline playback series
# Working with DRM protected offline content
## Limitations
Currently only Widevine DRM + DASH is supported.

## Basics
Downloading and playing assets with DRM protection works exactly the same as for any other asset.
Key requests, storage of keys and selection of keys for playback are all handled by the
Enigma River Android SDK.

## DRM licence validity
You can access the DRM licence for a downloaded playable using `DownloadedPlayable#getDrmLicence()`.
Note that this method will return `null` if the downloaded playable is not DRM protected. Using this
object we can get the expiry date for the currently downloaded licence.
```java
IDrmLicence drmLicence = downloadedPlayable.getDrmLicence();
if(drmLicence != null) {
    Log.d(TAG,  "DRM licence expires "+new Date(drmLicence.getExpiryTime()));
} else {
    Log.d(TAG,  "Not DRM protected");
}
```

## Play token expiry date
You can access play token expiry timestamp from
```java
long playTokenExpiration = downloadedPlayable.getPlayTokenExpiration();
}
```

### Handling DRM licence expiration
If a DRM licence expiration is detected the Enigma River Android SDK will send a `DrmKeysExpiredError`.
There are two places where you as an app developer should be ready to handle this.

When trying to start an asset:
```java
enigmaPlayer.play(new PlayRequest(playable, new BasePlayResultHandler() {
    @Override
    public void onError(EnigmaError error) {
        if(error instanceof DrmKeysExpiredError) {
            // Could not start playback of asset, DRM licence has expired
        } else {
            // ... handle other errors ... //
        }
    }
}));
```

And when the licence expires during playback:
```java
enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
    @Override
    public void onPlaybackError(EnigmaError error) {
        if(error instanceof DrmKeysExpiredError) {
            // DRM licence has expired
        } else {
            // ... handle other errors ... //
        }
    }
}, handler);
```


## Renewing the DRM licence
We can renew the DRM licence without needing to re-download any other part of the material. This
action might be counted as a download by the Red Bee Media OTT backend. This is something to keep in
mind if the asset has a set maximum number of downloads configured.

Simply renew the DRM licence by calling `IDrmLicence#renew(...)`:
```java
drmLicence.renew(session, new BaseDrmLicenceRenewResultHandler() {
    @Override
    public void onSuccess() {
        // ... DRM licence successfully renewed ... //
    }

    @Override
    public void onError(EnigmaError error) {
        if(error instanceof MaxDownloadCountLimitReachedError) {
            // The maximum number of downloads has already been reached
        } else {
            // ... Something went wrong ... //
        }
    }
}, handler);
```

## Further reading
To see this in action please refer to the tutorial app
[downloads](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidTutorialApps/tree/r3.7.19/downloads).


___
[Table of Contents](../index.md)<br/>
&bull; Working with DRM protected offline content (current)<br/>
[AssetDownloadState](asset_download_state.md)<br/>
[Offline Analytics events](offline_analytics.md)<br/>
[Only download on wifi networks](set_download_requirements.md)<br/>
[AssetQueueDownload](asset_queue_download.md)<br/>
