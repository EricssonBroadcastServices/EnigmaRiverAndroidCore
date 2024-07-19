<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

### Downloads and offline playback series
# Remove downloaded assets
To remove a downloaded asset, simply use `IEnigmaDownload#removeDownloadedAsset` with a valid
`DownloadedPlayable`.

```java
enigmaDownload.removeDownloadedAsset(downloadedPlayable, new BaseResultHandler<Void>() {
    @Override
    public void onResult(Void noResult) {
        // Download successfully removed
    }

    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... //
    }
}, handler);
```

Available `DownloadedPlayable`s can be acquired by calling `IEnigmaDownload#getDownloadedAssets`.

See [Listing downloaded assets](list_downloads.md) for more details.


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
[Using the download API](enigma_download.md)<br/>
[Get available tracks for download](get_download_info.md)<br/>
[Start asset download](start_download.md)<br/>
[Managing ongoing downloads](ongoing_downloads.md)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
&bull; Remove downloaded assets (current)<br/>
[Downloads app](example_app.md)<br/>
