### Downloads and offline playback series
# Listing downloaded assets
To list the downloaded asset available for playback, use `IEnigmaDownload#getDownloadedAssets`.
```java
IEnigmaDownload enigmaDownload = new EnigmaDownload(businessUnit);
enigmaDownload.getDownloadedAssets(new BaseResultHandler<List<DownloadedPlayable>>() {
    @Override
    public void onResult(List<DownloadedPlayable> result) {
        for(DownloadedPlayable playable : result) {
            // ... create UI for playable ... //
        }
    }

    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... //
    }
}, handler);
```


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
[Using the download API](enigma_download.md)<br/>
[Start asset download](start_download.md)<br/>
Listing downloaded assets (current)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
