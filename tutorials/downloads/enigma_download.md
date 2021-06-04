### Downloads and offline playback series
# Using the download API
To interact with the download API we need to create an `EnigmaDownload` instance.

```java
IEnigmaDownload enigmaDownload = new EnigmaDownload(businessUnit);
```

This instance can be reused or recreated as a matter of preference.

```java
public interface IEnigmaDownload {
    void getDownloadableInfo(String assetId, ISession session, IResultHandler<IDownloadableInfo> resultHandler);
    void getDownloadableInfo(String assetId, ISession session, IResultHandler<IDownloadableInfo> resultHandler, Handler handler);

    void startAssetDownload(DownloadStartRequest request, IDownloadStartResultHandler resultHandler);
    void startAssetDownload(DownloadStartRequest request, IDownloadStartResultHandler resultHandler, Handler handler);

    void getDownloadedAssets(IResultHandler<List<DownloadedPlayable>> resultHandler);
    void getDownloadedAssets(IResultHandler<List<DownloadedPlayable>> resultHandler, Handler handler);

    void removeDownloadedAsset(DownloadedPlayable downloadedPlayable, IResultHandler<Void> resultHandler);
    void removeDownloadedAsset(DownloadedPlayable downloadedPlayable, IResultHandler<Void> resultHandler, Handler handler);

    void getDownloadsInProgress(IResultHandler<List<IAssetDownload>> resultHandler);
    void getDownloadsInProgress(IResultHandler<List<IAssetDownload>> resultHandler, Handler handler);
}
```

Every method is potentially asynchronous and finishes by calling one method on
the {callback object}/{result handler}. If the version of a method with a `android.os.Handler` is used
the SDK will invoke the result handler object using the handler. This can be useful to ensure
callbacks are executed on the main/UI thread. If the version of a method without a `Handler` is used
the SDK makes no guarantees about which thread the callback will be made on.


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Gradle dependencies](dependencies.md)<br/>
[Check if an asset is downloadable](check_downloadability.md)<br/>
&bull; Using the download API (current)<br/>
[Get available tracks for download](get_download_info.md)<br/>
[Start asset download](start_download.md)<br/>
[Managing ongoing downloads](ongoing_downloads.md)<br/>
[Listing downloaded assets](list_downloads.md)<br/>
[Start playback of a downloaded asset](play_download.md)<br/>
[Remove downloaded assets](remove_download.md)<br/>
[Downloads app](example_app.md)<br/>
