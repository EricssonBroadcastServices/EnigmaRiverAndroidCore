### Downloads and offline playback series
# Start asset download
## Check download count
Before attempting to start a download, you should verify that the user has not reached their
limit om downloads for that asset. The maximum number of downloads as well as the current downloads
a user has made on an asset is available on the `IDownloadableInfo` object mentioned in the previous
chapter.
```java
public interface IDownloadableInfo {
    ...
    int getDownloadCount();
    int getMaxDownloadCount();
    ...
}
```
These values can be used to give feedback to the user about how many more times an asset can be
downloaded by them.

There is also a convenience method for easily checking if the download limit for the asset has been
reached:
```java
public interface IDownloadableInfo {
    ...
    boolean isMaxDownloadCountReached();
    ...
}
```

If a `DownloadStartRequest` is made when the max number of downloads for the asset has been reached,
a `MaxDownloadCountLimitReachedError` will be raised.

## DownloadStartRequest

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

### Selecting audio tracks

To select which audio tracks to download we can use `DownloadStartRequest#addAudio(AudioDownloadable)`
or `DownloadStartRequest#setAudios(List<AudioDownloadable>)` using `AudioDownloadable`s retrieved
from the `IDownloadableInfo` object for the asset.
See [Get available tracks for download](get_download_info.md) for more details.

If no audio tracks are selected the SDK will automatically select which audio tracks to download.

<br />

For an example of this in action, see [Downloads app](example_app.md).

### Selecting subtitle tracks

To select which subtitle tracks to download we can use `DownloadStartRequest#addSubtitle(SubtitleDownloadable)`
or `DownloadStartRequest#setSubtitles(List<SubtitleDownloadable>)` using `SubtitleDownloadable`s
retrieved from the `IDownloadableInfo` object for the asset.
See [Get available tracks for download](get_download_info.md) for more details.

<br />

For an example of this in action, see [Downloads app](example_app.md).

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
[Managing ongoing downloads](ongoing_downloads.md)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
