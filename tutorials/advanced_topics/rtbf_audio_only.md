<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

# RTBF - AUDIO ONLY 3.0
This tutorial describes how to facilitate and configure sticky player and background player for
audio only streams.

## Set up Sticky player

Sticky player is a sticky screen that can be added at the screen top or bottom section over the
screen. Users can navigate in the app while listening to the audio stream/podcasts in the background
and controlling the player via a sticky player at the bottom/top of the screen.

Users can forward, rewind, and pause/play from the sticky player.

### Configuration

- App can read media type for currently played stream to check if it is audio-only via following
  code

```java
    enigmaPlayer.isCurrentStreamTypeAudioOnly()
```
It will return true if currently playing asset is audio-only stream.
It will return false, if currently no asset is being played or if it is not audio-only media type.

- EnigmaPlayer binds itself to the activity like PlayerActivity. So when activity is destroyed the
  EnigmaPlayer releases and cleans up Exoplayer instances too. For sticky player functionality, we want
  to keep Exoplayer instances remain active for continuous playback of the stream even if there is no
  video area. Set EnigmaPlayer.setStickyPlayer to true when the app wants sticky functionality and does not
  want that enigmaPlayer to clean up the current Exoplayer instance to continue the playback. 
  See how it is done in this example : <a href="https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidReferenceApp/blob/master/src/main/java/com/redbeemedia/enigma/referenceapp/PlayerActivity.java" target="_blank">PlayerActivity.java</a>

```java
    enigmaPlayer.setStickyPlayer(true);
```

- Ensure that you are not explicitly called enigmaPlayer.release() when you want sticky player
  functionality. The below method will clean up the currently running instance of Exoplayer.
  
```java
    // Do not call it when you want sticky player functionality and want to let stream run in background even if activity is destroyed
    enigmaPlayer.release();
```

- Create a Sticky UI either bottom or top of the screen and control the player activities. To
  control player controls use following code.

  See how buttons are controlling the sticky player in this example : <a href="https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidReferenceApp/blob/master/src/main/java/com/redbeemedia/enigma/referenceapp/ListAssetsActivity.java" target="_blank">Example for controlling player controls</a>


```java

getEnigmaPlayer().getControls().start();
        getEnigmaPlayer().getControls().pause();


//  rewind player

        ITimelinePosition currentPosition=PlayerService.getEnigmaPlayer().getTimeline().getCurrentPosition();
        if(currentPosition!=null){
        ITimelinePosition newPosition=currentPosition.subtract(CONTROL_INCREMENT);
        ITimelinePosition startPosition=PlayerService.getEnigmaPlayer().getTimeline().getCurrentStartBound();
        if(newPosition.before(startPosition)){
        newPosition=startPosition;
        }
        PlayerService.getEnigmaPlayer().getControls().seekTo(newPosition);
        PlayerService.getEnigmaPlayer().getControls().start();
        }

// forward player   
        ITimelinePosition currentPosition=PlayerService.getEnigmaPlayer().getTimeline().getCurrentPosition();
        if(currentPosition!=null){
        ITimelinePosition newPosition=currentPosition.add(CONTROL_INCREMENT);
        ITimelinePosition endPosition=PlayerService.getEnigmaPlayer().getTimeline().getCurrentEndBound();
        if(newPosition.after(endPosition)){
        newPosition=endPosition;
        }
        PlayerService.getEnigmaPlayer().getControls().seekTo(newPosition);
        PlayerService.getEnigmaPlayer().getControls().start();
        }
```

### Example Configuration

Follow the sticky player implementation in the reference app here :
<a href="https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidReferenceApp/blob/master/src/main/java/com/redbeemedia/enigma/referenceapp/PlayerActivity.java" target="_blank">PlayerActivity.java</a>





## Set up Background player and display in status bar notification

This functionality allows the app to play the stream in the background even if the application is not active in
Android.

Use Android service to create a foreground
service: https://developer.android.com/guide/components/foreground-services

Also, see PlayerService.java in the reference app on how to create a foreground service.

### Configuration

- Follow all the same steps as 'Sticky player'
- To show notification in the status bar and control player controls from notification
  use https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/ui/PlayerNotificationManager.html
- See <a href="https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidReferenceApp/blob/master/src/main/java/com/redbeemedia/enigma/referenceapp/PlayerService.java" target="_blank">PlayerService.java</a>
  in reference app how to create and use PlayerNotificationManager
- To set Exoplayer in PlayerNotificationManager use the following way:

```java
// this will set exoplayer player instance in playernotificationmanager
  ExoPlayerTech.setupPlayerNotificationManager(manager);
```

### Example Configuration

Follow the sticky player implementation in the reference app here :
<a href="https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidReferenceApp/blob/master/src/main/java/com/redbeemedia/enigma/referenceapp/PlayerActivity.java" target="_blank">PlayerActivity.java</a>


Follow the example of PlayerService here, where service creates notification in the status bar: 

<a href="https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidReferenceApp/blob/master/src/main/java/com/redbeemedia/enigma/referenceapp/PlayerService.java" target="_blank">PlayerService.java</a>


### Link to Sample App

Follow the reference app implementation for sticky player and backgrround player support
Link : 
<a href="https://github.com/EricssonBroadcastServices/DevEnigmaRiverAndroidReferenceApp" target="_blank">Link to Reference app</a>


# Sample App Usage
1. Fill in BASE url, CU/BU , username and password in the login screen
   <a href="https://github.com/EricssonBroadcastServices/DevAndroidClient3/tree/master/buildSrc/src/main/resources/login.png" target="_blank">Login page screenshot</a>

2. Choose asset which has audio-only flag set from BE

3. Play the asset

4. Asset will be played in the video-area screen

5. Now press back button to close the video-area screen

6. If it is audio-only asset (audioOnly flag is true from BE) then sticky player will come automatically and continue playing the audio

Look at the screen shot below

<a href="https://github.com/EricssonBroadcastServices/DevAndroidClient3/tree/master/buildSrc/src/main/resources/stickyplayer.png" target="_blank">Sticky player screenshot</a>


7. If it audio only asset then you can control the player from status bar also . Look at the screenshot how notification will look like
   <a href="https://github.com/EricssonBroadcastServices/DevAndroidClient3/tree/master/buildSrc/src/main/resources/statusbar.png" target="_blank">Status bar player screenshot</a>


## Disclaimer

Please note that this tutorial will be a subject for change and will be extended once SSAI is fully
implemented.


___
[Table of Contents](../index.md)<br/>
