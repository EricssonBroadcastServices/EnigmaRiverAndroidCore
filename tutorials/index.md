# Table of Contents

The following tutorials will show you various ways Enigma River Android SDK can be utilized, and described advanced topics.

<br>

## Jump to...
<span style="margin-left: 10px"></span>-&gt; [Basics series](#basics-series) <br>
<span style="margin-left: 10px"></span>-&gt; [Custom UI controls series](#custom-ui-controls-series) <br>
<span style="margin-left: 10px"></span>-&gt; [Audio and subtitles series](#audio-and-subtitles-series) <br>
<span style="margin-left: 10px"></span>-&gt; [Chromecast integration series](#chromecast-integration-series) <br>
<span style="margin-left: 10px"></span>-&gt; [Downloads and offline playback series](#downloads-and-offline-playback-series) <br>
<span style="margin-left: 10px"></span>-&gt; [Additional topics for download and offline playback series](#additional-topics-for-download-and-offline-playback-series) <br>
<span style="margin-left: 10px"></span>-&gt; [Migration guide (Android SDK 2.0 -> Enigma River Android SDK)](#migration-guide-android-sdk-20---enigma-river-android-sdk) <br>
<br>
<span style="margin-left: 10px"></span>-&gt; [Advanced Topics](#advanced-topics) <br>


### Basics series
###### Purpose
The purpose of this series is to familiarize yourself with Enigma River Android SDK concepts and basic setup.
###### What will you learn
At the end, you should have a running Android application that will:
* Authenticate to Redbee Media OTT backend.
* Play media assets from RedBee Media OTT.
* Use a custom layout for play, pause, seeking, timeline, loading spinner and live indicator controls.


[Prerequisites](basics/prerequisites.md)<br/>
[Introduction](basics/introduction.md)<br/>
[Project setup](basics/project_setup.md)<br/>
[Your first app](basics/your_first_app.md)<br/>

### Custom UI controls series
###### Purpose
The purpose of this series is to teach you how to integrate your custom UI playback controls to Enigma Android Player.
###### What will you learn
At the end, you should have a running Android application that will:
* Authenticate to Redbee Media OTT backend.
* Play media assets from RedBee Media OTT.
* Use a custom layout for play, pause, seeking, timeline, loading spinner and live indicator controls.


[Prerequisites](custom_ui/prerequisites.md)<br/>
[Basics: play, pause and seeking](custom_ui/basics.md)<br/>
[Play/Pause Button](custom_ui/play_pause_button.md)<br/>
[Custom timeline](custom_ui/timeline.md)<br/>
[Spinner and Live Indicator](custom_ui/spinner_and_live.md)<br/>
[Custom UI app](custom_ui/custom_ui_app.md)<br/>

### Audio and subtitles series
###### Purpose
The purpose of this series is to teach you to build a custom UI that will keep track and change audio and subtitle tracks with Enigma Android Player.
###### What will you learn
At the end, you should have a running Android application that will:
* Authenticate to Redbee Media OTT backend.
* Play media assets from RedBee Media OTT.
* Change audio tracks and subtitles.


[Prerequisites](audio_subs/prerequisites.md)<br/>
[Audio and subtitle selection](audio_subs/audio_and_text_tracks.md)<br/>

### Chromecast integration series
###### Purpose
The purpose of this series is to teach you how to integrate Chromecast support to Enigma Android Player.
###### What will you learn
At the end, you should have a running Android application that will:
* Authenticate to Redbee Media OTT backend.
* Play media assets from RedBee Media OTT.
* Provide custom UI to cast the media to a Chromecast device.


[Prerequisites](chromecast/prerequisites.md)<br/>
[Integrating Red Bee OTT cast support](chromecast/chromecast.md)<br/>
[Advanced usage of the cast-module](chromecast/chromecast_advanced.md)<br/>

### Downloads and offline playback series
###### Purpose
The purpose of this series is to how to add support for offline playback in your Enigma Android Player application.
###### What will you learn
You will learn how to:
* Check if an asset is downloadable.
* Download an asset.
* Select bitrate for download.
* Show a list of downloaded asset.
* Remove a downloaded asset.


[Prerequisites](downloads/prerequisites.md)<br/>
[Gradle dependencies](downloads/dependencies.md)<br/>
[Check if an asset is downloadable](downloads/check_downloadability.md)<br/>
[Using the download API](downloads/enigma_download.md)<br/>
[Get available tracks for download](downloads/get_download_info.md)<br/>
[Start asset download](downloads/start_download.md)<br/>
[Managing ongoing downloads](downloads/ongoing_downloads.md)<br/>
[Listing downloaded assets](downloads/list_downloads.md)<br/>
[Start playback of a downloaded asset](downloads/play_download.md)<br/>
[Remove downloaded assets](downloads/remove_download.md)<br/>
[Downloads app](downloads/example_app.md)<br/>

### Additional topics for download and offline playback series
###### Purpose
The purpose of this series is to provide some additional tutorials on how to achieve
            specific tasks and features related to downloads.

[Working with DRM protected offline content](downloads_advanced/download_drm_management.md)<br/>
[AssetDownloadState](downloads_advanced/asset_download_state.md)<br/>
[Only download on wifi networks](downloads_advanced/set_download_requirements.md)<br/>

### Migration guide (Android SDK 2.0 -> Enigma River Android SDK)
###### Purpose
The purpose of this series is to help app developers currently using the old 2.0 SDK migrate to
            using the Enigma River Android SDK (3.0).

[Introduction](migration_guide/introduction.md)<br/>
[Structural changes](migration_guide/structural_changes.md)<br/>
[Changes to SDK initialization](migration_guide/sdk_initialization.md)<br/>
[Changes to authentication/login](migration_guide/login.md)<br/>
[Changes to asset metadata retrieval](migration_guide/asset_metadata.md)<br/>
[Changes to playback](migration_guide/playback.md)<br/>
[Further reading](migration_guide/further_reading.md)<br/>

### Advanced Topics
The following tutorials provide in-depth information about different areas of interest when implementing/integrating custom controls for EnigmaPlayer.

[Virtual controls](advanced_topics/virtual_controls.md)<br/>
[Controlling EnigmaPlayer programmatically](advanced_topics/controls.md)<br/>
[Timeline API](advanced_topics/timeline.md)<br/>
[EnigmaPlayerState](advanced_topics/enigma_player_state.md)<br/>
[Playback Session](advanced_topics/playback_session.md)<br/>
[Live drift correction (ExoPlayer)](advanced_topics/drift_correction.md)<br/>
[Overriding media format selection logic](advanced_topics/media_format_preference.md)<br/>
[Ad Insertion](advanced_topics/ad_insertion.md)<br/>
[Sprites API](advanced_topics/sprites.md)<br/>
