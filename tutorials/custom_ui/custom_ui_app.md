### Custom UI controls series
# Custom UI app
## Putting it all together
In this tutorial we will create an app that will use the previously
mentioned custom UI elements: play/pause, timeline, spinner and live
indicator.

As a starting point, we will use the
[yourFirstApp](../basics/your_first_app.md) application, then we will
add our custom UI elements one by one.

## Add the basics
Don't forget to disable the default UI first:

```java
...
ExoPlayerTech exoPlayerTech = new ExoPlayerTech(this, "customcontrols");
exoPlayerTech.attachView(findViewById(R.id.player_view));

exoPlayerTech.hideController();
...
IEnigmaPlayer player = new EnigmaPlayer(session, exoPlayerTech);
...
```

Next, let's get the needed basic virtual controls:

```java
...
IVirtualControlsSettings virtualControlsSettings = new VirtualControlsSettings()
    .setSeekBackwardStep(SEEK_JUMP)
    .setSeekForwardStep(SEEK_JUMP);
IVirtualControls virtualControls = VirtualControls.create(enigmaPlayer, virtualControlsSettings);

bindButton(ibtnSeekBack, virtualControls.getRewind());
bindButton(ibtnSeekForward, virtualControls.getFastForward());
...
```

## Add the pause/play button
Let's add and connect our custom `PausePlayImageButton`:

```java
...
private PausePlayImageButton ibtnPlayPause;
...
ibtnPlayPause.connectTo(player);
...
```

## Add the timeline
Now let's add and hook the `TimelineView` control:

```java
...
private TimelineView timelineView;
...
timelineView.connectTo(player);
...
```

## Add the spinner
Next is our loading spinner, so let's add it as a default
`android.widget.ProgressBar`:

```java
...
private ProgressBar pbLoader;
...
enigmaPlayer.addListener(new BaseEnigmaPlayerListener(){
    ...
    @Override
    public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
        if (to == EnigmaPlayerState.LOADING){
            pbLoader.setVisibility(View.VISIBLE);
        }else if (to == EnigmaPlayerState.LOADED){
            pbLoader.setVisibility(View.GONE);
        }
    }
    ...
}
...
```

## Add the live indicator
We have our live indicator to add, as a default
`android.widget.TextView`:

```java
...
playbackSessionListener = new BasePlaybackSessionListener(){
    @Override
    public void onPlayingFromLiveChanged(boolean live) {
        updateIsLive(live);
    }
};
...
player.addListener(new BaseEnigmaPlayerListener(){
    ...
    @Override
    public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
        if(from != null) {
            from.removeListener(playbackSessionListener);
        }
        if(to != null) {
            updateIsLive(to.isPlayingFromLive());
            to.addListener(playbackSessionListener, handler);
        }
    }
}, handler);
...
private void updateIsLive(boolean isLive){
    if (isLive){
        txtIsLive.setText("Live");
        txtIsLive.setTextColor(Color.RED);
    }else{
        txtIsLive.setText("Not live");
        txtIsLive.setTextColor(Color.DKGRAY);
    }
}
```

## Customize subtitle view
One can customize the exo subtitle view. Fetch subtitle view instance from engimaPlayer

`com.google.android.exoplayer2.ui.SubtitleView`:

```java
...
        
enigmaPlayer.getPlayerSubtitleView()
...
        
# Change color by following ways :
        
CaptionStyleCompat captionStyleCompat = new CaptionStyleCompat(DEFAULT.foregroundColor,
                 Color.BLUE,
                 DEFAULT.windowColor,
                 DEFAULT.edgeType,
                 DEFAULT.edgeColor,
                 DEFAULT.typeface);
 enigmaPlayer.getPlayerSubtitleView().setStyle(captionStyleCompat);
 
 // set size
enigmaPlayer.getPlayerSubtitleView().setFixedTextSize(Dimension.PX,30);
```

**And that's all there is to it**!

This is what our finished app looks like:
[customUiApp](https://github.com/EricssonBroadcastServices/EnigmaRiverAndroidTutorialApps/tree/r3.6.4-BETA-4/customcontrols)<br
/>



___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Basics: play, pause and seeking](basics.md)<br/>
[Play/Pause Button](play_pause_button.md)<br/>
[Custom timeline](timeline.md)<br/>
[Spinner and Live Indicator](spinner_and_live.md)<br/>
&bull; Custom UI app (current)<br/>
