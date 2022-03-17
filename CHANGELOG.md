# CHANGELOG
* `3.4.3` Release - [3.4.3](#3.4.3) &nbsp; - &nbsp; 2022-03-17
* `3.4.2` Release - [3.4.2](#3.4.2) &nbsp; - &nbsp; 2022-03-08
* `3.4.1` Release - [3.4.1](#3.4.1) &nbsp; - &nbsp; 2022-02-24
* `3.4.0` Release - [3.4.0](#3.4.0) &nbsp; - &nbsp; 2022-02-01
* `3.3.4` Release - [3.3.4](#3.3.4) &nbsp; - &nbsp; 2022-01-24
* `3.3.3` Release - [3.3.3](#3.3.3) &nbsp; - &nbsp; 2021-10-15
* `3.3.2` Release - [3.3.2](#3.3.2) &nbsp; - &nbsp; 2021-09-29
* `3.3.1` Release - [3.3.1](#3.3.1) &nbsp; - &nbsp; 2021-09-29
* `3.3.0` Release - [3.3.0](#3.3.0) &nbsp; - &nbsp; 2021-09-10
* `3.2.1` Release - [3.2.1](#3.2.1) &nbsp; - &nbsp; 2021-06-15
* `3.2.1` Release - [3.2.1](#3.2.1) &nbsp; - &nbsp; 2021-06-15
* `3.2.0` Release - [3.2.0](#3.2.0) &nbsp; - &nbsp; 2021-06-04
* `3.1.14` Release - [3.1.14](#3.1.14) &nbsp; - &nbsp; 2021-04-28
* `3.1.13` Release - [3.1.13](#3.1.13) &nbsp; - &nbsp; 2021-04-08
* `3.1.12` Release - [3.1.12](#3.1.12) &nbsp; - &nbsp; 2021-03-19
* `3.1.11` Release - [3.1.11](#3.1.11) &nbsp; - &nbsp; 2021-02-10
* `3.1.10` Release - [3.1.10](#3.1.10) &nbsp; - &nbsp; 2021-02-02
* `3.1.9` Release - [3.1.9](#3.1.9) &nbsp; - &nbsp; 2021-01-28
* `3.1.8` Release - [3.1.8](#3.1.8) &nbsp; - &nbsp; 2020-12-11
* `3.1.7` Release - [3.1.7](#3.1.7) &nbsp; - &nbsp; 2020-12-04
* `3.1.6` Release - [3.1.6](#3.1.6) &nbsp; - &nbsp; 2020-11-13

# <a name="3.4.3"></a> 3.4.3
#### Features
`
DevEnigmaRiverAndroidCore
DevEnigmaRiverAndroidExoPlayerIntegration
DevEnigmaRiverAndroidExperimentalLowLatency
`
* EMP-17859 - Fix for Subtitles tracks.
* EMP-17511 - Add an API to allow video track selection.

# <a name="3.4.2"></a> 3.4.2
#### Features
`
DevEnigmaRiverAndroidCore
DevEnigmaRiverAndroidExoPlayerIntegration
`
* EMP-17698 - Media Codec Error.
* EMP-17761- Upgrade Exoplayer version 2.17

# <a name="3.4.1"></a> 3.4.1
#### Features
`
DevEnigmaRiverAndroidCore
DevEnigmaRiverAndroidExoPlayerIntegration
DevEnigmaRiverAndroidInternalTestUtils
`
* EMP-17795 - Implement for ad tracking events Loaded and Start.
* EMP-17464 - Click through support on ads.
* EMP-17817 - Missing analytics parameters.

# <a name="3.4.0"></a> 3.4.0
#### Features
`
DevEnigmaRiverAndroidCore
DevEnigmaRiverAndroidExoPlayerIntegration
DevEnigmaRiverAndroidCast
DevEnigmaRiverAndroidExperimentalLowLatency
DevEnigmaRiverAndroidReferenceapp
`
* EMP-17618 - Expose MediaType.
* EMP-17620 - Background Audio support.
* EMP-17619 - Play Audio Only Stream without video area.
* EMP-17622 - Chromecast support for audioOnly.

# <a name="3.3.4"></a> 3.3.4
#### Features
`
DevEnigmaRiverAndroidCore
`
* EMP-17640 - Get player duration from the stream.

# <a name="3.3.3"></a> 3.3.3
#### Features
`
DevEnigmaRiverAndroidCore
DevEnigmaRiverAndroidExoPlayerIntegration
DevEnigmaRiverAndroidInternalTestUtils
DevEnigmaRiverAndroidCast
`
* EMP-17140 - ExoMedia DRM issue.
* EMP-17492 - Missing bitrates on analytics.
* EMP-17456 - Support for set delay from the live edge.
* EMP-17405 - Live stream freezes when scrubbed past live point.
* EMP-17416 - Use UTC time from the server.
* EMP-17351 - ad option uid should be user id, not email.
* EMP-17397 - In case of live stream don't use player offset , for finding current program.
* EMP-17368 - Disable cache for future entitlements.
* EMP-17281 - Android timeline issues in Catchup Play.
* EMP-17036 - Add support for default cast subtitle & audio language.
* EMP-17232 - Entitlement issue in live channel NotPublishedError.
* EMP-17282-  SDK doesn't track ad impression.
* EMP-16153 - language/label for subtitles(if label is empty then it will show the language)
* EMP-17202 - There was no ad, but it was showing admarker in starting.
* EMP-16992 - Align Start/Stop Cast events.
* EMP-17202 - Crash after ad playback
* EMP-16153 - Add support for audio description / captions for hearing impaired
* EMP-17118 - Upgraded Exoplayer version to 2.15.1
* EMP-17114 - Scrubbing issue in SSAI, when ad offset is -1
* Emp-17122 - In vast LAST_VIEWED_OFFSET can come negative
* EMP-17139 - SSAI- Freezing issue
* EMP-16992 - Align Analytics implementation with the current specification.
* EMP-16716 - Enhance reported player errors sent to analytics


# <a name="3.3.2"></a> 3.3.2
#### Features
`EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
* EMP-16814: Updated ChangeLog

# <a name="3.3.1"></a> 3.3.1
#### Features
`EricssonBroadcastServices/DevEnigmaRiverAndroidCore/DevEnigmaRiverAndroidExoPlayerIntegration/
DevEnigmaRiverAndroidInternalTestUtils/DevEnigmaRiverAndroidExperimentalLowLatency/
DevEnigmaRiverAndroidExoPlayerDownload`
* EMP-16814: Player cue points exposed from SDK
* EMP-16857: SSAI- Send in supported formats and drms to the play request
* EMP-16781: Live SSAI- Should not show Seekbar

# <a name="3.3.0"></a> 3.3.0
#### Features
`EricssonBroadcastServices/DevEnigmaRiverAndroidCore/DevEnigmaRiverAndroidExoPlayerIntegration/
DevEnigmaRiverAndroidCast`
* EMP-16975 : VOD SSAI implementation and Update tutorials

# <a name="3.2.1"></a> 3.2.1
#### Features
`EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
* EMP-16122: Configure custom Analytics url
* EMP-16335: Upgrade exoplayer to 2.14.0

# <a name="3.2.0"></a> 3.2.0
#### Features
`EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
* EMP-16119: Login V3
* EMP-16237: Set limit to 10 sec for bookmarks
* EMP-16024: Check if released in EnigmaPlayer
* EMP-15896: Update tutorials
* EMP-16244: test case fixes
* EMP-15734: Upgrade exoplayer.2.13.2

`EricssonBroadcastServices/DevEnigmaRiverAndroidInternalTestUtils`
* EMP-15896/Enable-muting-of-analytics
* EMP-16012/Fix-test-cases-for-analytics

`EricssonBroadcastServices/DevEnigmaRiverAndroidExoPlayerIntegration`
* EMP-16024: Check if released in EnigmaPlayer.
* EMP-16023: Fix minor driftmeter issue
* EMP-15734: Upgrade exoplayer.2.13.2

`EricssonBroadcastServices/DevEnigmaRiverAndroidTutorialApps`
* EMP-15734: Upgrade exoplayer.2.13.2

`EricssonBroadcastServices/DevEnigmaRiverAndroidExoplayerDownloads`
* EMP-15734: Upgrade exoplayer.2.13.2

`DevEnigmaRiverAndroidReferenceApp`
* EMP-15734: Upgrade exoplayer.2.13.2

`EricssonBroadcastServices/DevEnigmaRiverAndroidDownload`
* EMP-15734: Upgrade exoplayer.2.13.2

`EricssonBroadcastServices/DevAndroidClient3`
* EMP-15734: Upgrade exoplayer.2.13.2

`EricssonBroadcastServices/EnigmaRiverAndroidReferenceApp`
* EMP-15734: Upgrade exoplayer.2.13.2

# BREAKING CHANGES IN VERSION 3.2.0
Due to the large amount of changes in the version of ExoPlayer this SDK release are using (2.13.2) the following changes are required.

## Android mobile and Android TV
- User interaction must be implemented on the ExoPlayerViews surface view and not on the `PlayerView` itself. 
```java
    playerView.getVideoSurfaceView().setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Do something
        }
    });
```
- If the Virtual Controls are used, the default exoplayer controls layout must explicitly defined in the player view layout. Otherwise a `ClassCastException` will be thrown. 
```xml
<com.google.android.exoplayer2.ui.PlayerView
...
app:controller_layout_id="@layout/exo_playback_control_view"
...
```

- `IEnigmaDownload`s (and `EnigmaDownload`s) `startAssetDownload` requires a `Context` parameter.
```java

enigmaDownload.startAssetDownload(myContext, downloadStartRequest, new BaseDownloadStartResultHandler() {
...

```

## Android TV observations
- Exoplayer's surface view has been observed requesting focus upon playback. This can be mitigated by disabling focusability for the superview.
```xml
<LineraLayout
...
android:descendantFocusability="blocksDescendants"
...
```
- Issues with the default ExoPlayerViews `surface_type` for DRM playback for some Android TV devices. Surface type for ExoPlayerView might be required to be `"texture_view"`.
```xml
<com.google.android.exoplayer2.ui.PlayerView
...
app:surface_type="texture_view"
...
```

# <a name="3.1.14"></a> 3.1.14
EMP-15928: synchronization fix for playback session delegate calls
EMP-15896: Enable muting of analytics
EMP-15934: Update gitignore
EMP-16012: Fix test cases

`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidInternalTestUtils`

All repositories has been updated with a new `.gitignore` file.

#### Features

# <a name="3.1.13"></a> 3.1.13
#### Features
* EMP-15922: Update how Adobe Primetime token is being configured
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidCast`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidInternalTestUtils`
`https://github.com/EricssonBroadcastServices/DevAndroidClient3`
''

# <a name="3.1.12"></a> 3.1.12
#### Features
* EMP-15723: Use new cast receivers
* EMP-15793: Adobe prime time token
* EMP-15736: Synchronization fixes
* EMP-15851: revert EMP-15736
* EMP-15756: Add size of file to downloads
* EMP-15754: Test-resource loader

`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidCast`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidExoPlayerIntegration`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidExoPlayerDownload`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidDownload`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidInternalTestUtils`
`https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidTutorialApps`

# <a name="3.1.11"></a> 3.1.11
#### Features
`EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
* EMP-15665: Synchronization bug fixes
* EMP-15600: Sprite bug fixes

`EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
* EMP-15591: App state bug if start-playback on new asset fails
* EMP-15570: ANDROID_TV included as device type

# <a name="3.1.10"></a> 3.1.10
#### Features

`EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
* EMP-15591: App state bug if start-playback on new asset fails
* EMP-15570: ANDROID_TV included as device type

## <a name="3.1.9"></a> 3.1.9
#### Features

`EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
* EMP-15506: Soften entitlement checks during live playback
* EMP-15570: TV included as device type
* EMP-15539: Sprites (overrides EMP-15396)

`EricssonBroadcastServices/DevEnigmaRiverAndroidExoPlayerIntegration`
* EMP-15505: Removed repeating entitlements check
* EMP-15586: Now switching player views instead of setting them

`EricssonBroadcastServices/DevEnigmaRiverAndroidInternalTestUtils`
* EMP-15369: Sprites

`EricssonBroadcastServices/DevAndroidClient3`
* EMP-15537: Sprite tutorials

## <a name="3.1.8"></a> 3.1.8
#### Features
* No ticket number: (change how player view is set)
 - https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidExoPlayerIntegration/pull/37

## <a name="3.1.7"></a> 3.1.7
#### Features
`EricssonBroadcastServices/DevEnigmaRiverAndroidCore`
* `EMP-15366`: Enable Mp3 Playback.
* `EMP-15319`: Typo fix

`EricssonBroadcastServices/DevEnigmaRiverAndroidExoPlayerIntegration`
* `EMP-15366`: Enable Mp3 Playback.

## <a name="3.1.6"></a> 3.1.6
#### Features
* `EMP-15183`: Enhanced live experience (mainly "proposed" visibility of the timeline if consumer decides to implement it).
* `EMP-15296`: Support including ad insertion parameters to the play request.
* `EMP-14701`: Support for managing ongoing downloads, including listening on various download events. Support for pausing/resuming/deleting downloads.
* `EMP-15235`: Add change in how the pages are retrieved
