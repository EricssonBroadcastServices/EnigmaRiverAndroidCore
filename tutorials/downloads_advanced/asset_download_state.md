### Additional topics for download and offline playback series
# AssetDownloadState
![stateDiagram](../images/AssetDownloadState_diagram.png "IAssetDownload
state diagram")

An `IAssetDownload` represents an ongoing download of an asset. It can have 5 different states.
Some of these are considered 'resolved' such as 'DONE' or 'CANCELLED'. These are the terminal
states for which the download is no longer considered 'ongoing'.

### IN_PROGRESS
An `IAssetDownload` in this state will try to continue downloading whenever it can. Internet
connectivity and network settings are examples of what could stop a download from proceeding.

When and if the download completes successfully the state will transition into `DONE`.

### PAUSED
An `IAssetDownload` in this state has been instructed to not try to continue download until
further notice.

### FAILED
An `IAssetDownload` in this state has encountered an error. To remove any partial data for this
download, `cancelDownload` must first be called.

### DONE
An `IAssetDownload` in this state has completed successfully and a `DownloadedPlayable` has been
made available in its place.

### CANCELLED
An `IAssetDownload` in this state has been cancelled. All partial data associated with the download
has been removed.


___
[Table of Contents](../index.md)<br/>
[Working with DRM protected offline content](download_drm_management.md)<br/>
&bull; AssetDownloadState (current)<br/>
[Offline Analytics events](offline_analytics.md)<br/>
[Only download on wifi networks](set_download_requirements.md)<br/>
[AssetQueueDownload](asset_queue_download.md)<br/>
