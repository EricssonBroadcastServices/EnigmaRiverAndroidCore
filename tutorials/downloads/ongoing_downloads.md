### Downloads and offline playback series
# Managing ongoing downloads
The `IAssetDownload` type represents an ongoing download of an asset. When complete, a new
`DownloadPlayable` will be available.

```java
public interface IAssetDownload {
...
    String getAssetId();
    float getProgress();
    ...
    void pauseDownload(IControlResultHandler controlResultHandler);
    void resumeDownload(IControlResultHandler controlResultHandler);
    void cancelDownload(IControlResultHandler controlResultHandler);
    ...
    boolean addListener(IAssetDownloadListener listener);
    boolean addListener(IAssetDownloadListener listener, Handler handler);
    boolean removeListener(IAssetDownloadListener listener);
...
}
```

To get a list of downloads in progress, use `IEnigmaDownload#getDownloadsInProgress`.
```java
IEnigmaDownload enigmaDownload = new EnigmaDownload(businessUnit);
enigmaDownload.getDownloadsInProgress(new BaseResultHandler<List<IAssetDownload>>() {
    @Override
    public void onResult(List<IAssetDownload> result) {
        // Success!
    }

    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... //
    }
}, handler);
```

### Download progress
The progress of an asset download can be retrieved from `IAssetDownload#getProgress()`. This
value is a fraction in the range `0.0` to `1.0`. We can also listen to changes to this value:
```java
assetDownload.addListener(new BaseAssetDownloadListener() {
    @Override
    public void onProgressChanged(IAssetDownload assetDownload, float progress) {
        // Update UI
    }
}, handler);
```

#### A small note for ExoPlayer users
If you are using the ExoPlayer download implementation module `exoPlayerDownload` the progress
will be queried every 500 milliseconds and `onProgressChanged(...)` will be triggered if any changes
are detected. You can change how often the automatic querying is made where you initialize
`EnigmaRiverContext`:
```java
EnigmaRiverContext.EnigmaRiverContextInitialization initialization
    = new EnigmaRiverContext.EnigmaRiverContextInitialization(exposureBaseUrl);
...
//Example: Set the query interval to 100 milliseconds
initialization.forModule(ExoPlayerDownloadContext.MODULE_INFO).setDownloadManagerRefreshRateMillis(100);
...
EnigmaRiverContext.initialize(this, initialization);
```

### Download state
To get the current state of an `IAssetDownload` you can use:
```java
AssetDownloadState currentState = assetDownload.getState();
```

To track changes to the state use:
```java
assetDownload.addListener(new BaseAssetDownloadListener() {
    @Override
    public void onStateChanged(AssetDownloadState oldState, AssetDownloadState newState) {
        // The state is not newState
    }
}, handler);
```

For more information on the states of an `IAssetDownload`, see [AssetDownloadState](../downloads_advanced/asset_download_state.md)

## Pause, resume and cancel

##### Example - Pause an ongoing download
```java
assetDownload.pauseDownload(new IControlResultHandler() {
    @Override
    public void onDone() {
        // Download was paused
    }

    @Override
    public void onError(EnigmaError error) {
        // An error occurred while trying to pause this download
    }

    @Override
    public void onRejected(IRejectReason reason) {
        // Request to pause was rejected
    }

    @Override
    public void onCancelled() {
        // This request was cancelled
    }
})
```

##### Example - Resume all downloads
```java
IEnigmaDownload enigmaDownload = new EnigmaDownload(businessUnit);
enigmaDownload.getDownloadsInProgress(new BaseResultHandler<List<IAssetDownload>>() {
    @Override
    public void onResult(List<IAssetDownload> result) {
        for(IAssetDownload assetDownload : result) {
            result.resumeDownload();
        }
    }

    @Override
    public void onError(EnigmaError error) {
        // ... handle error ... //
    }
}, handler);
```
##### Example - Cancel download
```java
assetDownload.cancelDownload(new IControlResultHandler() {
    @Override
    public void onDone() {
        // Download was cancelled
    }

    @Override
    public void onError(EnigmaError error) {
        // An error occurred while trying to cancel this download
    }

    @Override
    public void onRejected(IRejectReason reason) {
        // Request to cancel was rejected
    }

    @Override
    public void onCancelled() {
        // This request to cancel was cancelled
    }
})
```


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
[Using the download API](enigma_download.md)<br/>
[Get available tracks for download](get_download_info.md)<br/>
[Start asset download](start_download.md)<br/>
&bull; Managing ongoing downloads (current)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
