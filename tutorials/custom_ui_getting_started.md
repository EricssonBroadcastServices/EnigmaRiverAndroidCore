# Getting started with custom controls

This tutorial will teach you how to integrate your custom UI buttons to EnigmaPlayer.

This tutorial assumes that you have completed [the introduction](your_first_app.md) and/or have a working video-app using the Enigma River Android SDK and the Red Bee OTT backend.
It also assumes that you have some prior experience with creating an Android UI.

Using the EnigmaPlayer with a custom UI is simple!
1. Disable the default UI
1. Hook up your own UI to the virtual controls

## Disable the default UI
If you are using the ExoPlayerIntegration-module you first need to hide the default UI. This is done by calling `hideController()` on the `ExoPlayerTech` object before you instantiate an EnigmaPlayer with it.

```java
...
ExoPlayerTech exoPlayerTech = new ExoPlayerTech(this, "myApp");
exoPlayerTech.attachView(findViewById(R.id.player_view));

exoPlayerTech.hideController();
...
IEnigmaPlayer player = new EnigmaPlayer(session, exoPlayerTech);
...
```

If you start your app now you will still see the video, but no buttons (for example play, pause, etc.) and no timeline. Now we just need to add our custom controls on top and hook them up to EnigmaPlayer!

## Hooking up custom UI to EnigmaPlayer

The SDK provides an API called [virtual controls](virtual_controls.md). The virtual controls provide an interface for a custom UI to integrate with.

### IVirtualButton

The main component of the virtual controls is the `IVirtualButton`:
```java
public interface IVirtualButton {
    boolean isEnabled();
    boolean isRelevant();
    void click();
...
}
```
A `IVirtualButton` represents an actual button in the UI.

The value returned by `isEnabled()` indicates whether the button is currently enabled. If *not enabled* the UI button is typically 'grayed out'. However, how this is shown (or not) is completely up to the UI designer.

The value returned by `isRelevant()` indicates whether the button is currently relevant for the asset being played. For example, if an asset has been configured in the backend to not be pausable, the 'pause'-button will be considered not relevant while playing that asset. Typically a button that is *not relevant* should be completely hidden from the UI.

The method `click()` executes a click of the button. For the `IVirtualButton` representing 'pause' this method results in a pause-request to EnigmaPlayer, and for the `IVirtualButton` representing 'fastForward' the method results in a seek-request, etc.

<br/>

To keep the current state of a `IVirtualButton` and the UI button in sync a listener is needed.
```java
public interface IVirtualButton {
...
    boolean addListener(IVirtualButtonListener listener);
    boolean addListener(IVirtualButtonListener listener, Handler handler);
    boolean removeListener(IVirtualButtonListener listener);
}

public interface IVirtualButtonListener { //Don't implement this interface directly!
    void onStateChanged();
}
```

To listen for changes to a `IVirtualButton` we add a listener by extending a `BaseVirtualButtonListener`.
```java
...
virtualButton.addListener(new BaseVirtualButtonListener() {
    @Override
    public void onStateChanged() {
        boolean currentEnabledState = virtualButton.isEnabled();
        boolean currentRelevantState = virtualButton.isRelevant();
        //Update UI button
        ...
    }
}, handler); //Provide a handler for the UI thread to receive events on that thread.
...
```

The `onStateChanged` event is triggered whenever the value of `isEnabled()` or `isRelevant()` changes.

### Getting a specific IVirtualButton

Now we know how a `IVirtualButton` works. Let's take a look at how to get a reference to the ones representing 'play', 'pause', etc.

The `IVirtualButton`s are aggregated in the `IVirtualControls` interface:
```java
public interface IVirtualControls {
    IVirtualButton getRewind();
    IVirtualButton getFastForward();
    IVirtualButton getPlay();
    IVirtualButton getPause();
    etc.
}
```

We can get an instance of an object implementing this interface by using the static factory method `VirtualControls.create(IEnigmaPlayer enigmaPlayer, IVirtualControlsSettings settings)`. For this we also need to pass in a `IVirtualControlsSettings` object. We will use `new VirtualControlsSettings()` for now.
```java
...
IVirtualControls virtualControls = VirtualControls.create(enigmaPlayer, new VirtualControlsSettings());
...
```
From this object we can get our `IVirtualButton`s.
```java
...
IVirtualButton virtualRewind = virtualControls.getRewind();
IVirtualButton virtualFastForward = virtualControls.getFastForward();
IVirtualButton virtualPlay = virtualControls.getPlay();
IVirtualButton virtualPause = virtualControls.getPause();
...
```

#### Example

Let's assume we have UI buttons in our layout that all extend `android.widget.Button`. This would then be a common way to hook up those buttons to the `IVirtualButton`s:
```java
private void bindButton(final Button button, final IVirtualButton virtualButton) {
    syncButtonState(button, virtualButton); //Initialize the button state

    //Forward click to virtual button
    button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            virtualButton.click();
        }
    });

    //Listen to state changes on the virtual button
    virtualButton.addListener(new BaseVirtualButtonListener() {
        @Override
        public void onStateChanged() {
            //Sync the button state
            syncButtonState(button, virtualButton);
        }
    }, handler); //We provide handler for UI thread so we can update the Button-view
}

private void syncButtonState(Button button, IVirtualButton virtualButton) {
    button.setVisibility(virtualButton.isRelevant() ? View.VISIBLE : View.GONE);
    button.setEnabled(virtualButton.isEnabled());
}
```

And in `onCreate()`:
```java
//Create virtual controls
IVirtualControls virtualControls = VirtualControls.create(enigmaPlayer, new VirtualControlsSettings());

IVirtualButton virtualRewind = virtualControls.getRewind();
IVirtualButton virtualFastForward = virtualControls.getFastForward();
IVirtualButton virtualPlay = virtualControls.getPlay();
IVirtualButton virtualPause = virtualControls.getPause();
IVirtualButton virtualGoToLive = virtualControls.getGoToLive();

Button rewindButton = findViewById(R.id.rewind);
Button fastForwardButton = findViewById(R.id.fastforward);
Button playButton = findViewById(R.id.play);
Button pauseButton = findViewById(R.id.pause);
Button goToLiveButton = findViewById(R.id.go_to_live);

bindButton(virtualRewind, rewindButton);
bindButton(virtualFastForward, fastForwardButton);
bindButton(virtualPlay, playButton);
bindButton(virtualPause, pauseButton);
bindButton(virtualGoToLive, goToLiveButton);
```

</br>
</br>

And that's all there is to it! For information on how to hook up your custom timeline see [the timeline API](timeline.md).