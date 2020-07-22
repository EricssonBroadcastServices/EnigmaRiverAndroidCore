### Downloads and offline playback series
# Start asset download
To start an asset download, simplycreate a `DownloadStartRequest` and call `IEnigmaDownload#startAssetDownload`.
```java
...
IEnigmaDownload enigmaDownload = new EnigmaDownload(businessUnit);
DownloadStartRequest downloadStartRequest = new DownloadStartRequest(assetId, session);
enigmaDownload.startAssetDownload(downloadStartRequest, new BaseDownloadStartResultHandler() {
    @Override
    public void onStarted() {
        // Download was successfully started
    }

    @Override
    public void onError(EnigmaError error) {
        // Failed to start download
    }
}, handler);
...
```


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
[Using the download API](enigma_download.md)<br/>
Start asset download (current)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
