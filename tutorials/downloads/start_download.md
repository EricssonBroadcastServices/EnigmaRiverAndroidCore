### Downloads and offline playback series
# Start asset download
To start an asset download, simply create a `DownloadStartRequest` and call `IEnigmaDownload#startAssetDownload`.
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

## Download options

Before sending the `DownloadStartRequest` to `IEnigmaDownload#startAssetDownload` it can be modified
with a few options.

### Selecting video track

To select which video track (bitrate) to download we can use `DownloadStartRequest#setVideo(VideoDownloadable)`
using a `VideoDownloadable` retrieved from the `IDownloadableInfo` object for the asset.
See [Get available tracks for download](get_download_info.md) for more details.

If `setVideo` is never called or called with `null` the SDK will select a suitable video track
automatically.

##### Example - Selecting a random video track
In the following example a video track (bitrate) is selected at random.
```java
final IEnigmaDownload enigmaDownload = new EnigmaDownload(businessUnit);
enigmaDownload.getDownloadableInfo(assetID, session, new BaseResultHandler<IDownloadableInfo>() {
    @Override
    public void onResult(IDownloadableInfo result) {
        Random random = new Random();

        List<VideoDownloadable> videoTrackChoices = result.getVideoTracks();
        VideoDownloadable videoDownloadable;
        if(videoTrackChoices.isEmpty()) {
            videoDownloadable = null;
        } else {
            int videoTrackIndex = random.nextInt(videoTrackChoices.size());
            videoDownloadable = videoTrackChoices.get(videoTrackIndex);
        }

        DownloadStartRequest downloadStartRequest = new DownloadStartRequest(assetID, session);
        downloadStartRequest.setVideo(videoDownloadable);
        enigmaDownload.startAssetDownload(downloadStartRequest, new BaseDownloadStartResultHandler() {
            @Override
            public void onStarted() {
                // ... download started! ... //
            }

            @Override
            public void onError(EnigmaError error) {
                // ... handle error ... //
            }
        }, handler);
    }

    @Override
    public void onError(EnigmaError error) {
        // ... handle error ...
    }
}, handler);
```

## Advanced options

In [Overriding media format selection logic](../advanced_topics/media_format_preference.md) you can read about how to provide
custom media format selection logic (for example preference for DASH/HLS and with or without DRM) for playback and downloads.


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
[Using the download API](enigma_download.md)<br/>
[Get available tracks for download](get_download_info.md)<br/>
&bull; Start asset download (current)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
