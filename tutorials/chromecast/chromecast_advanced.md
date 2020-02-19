### Chromecast integration series (work in progress)
# Advanced usage of the cast-module
#### Listening to events from the chromecast receiver

Custom events sent from the chromecast receiver app can be listened to by extending `BaseEnigmaCastListener` and adding the listener to the `EnigmaCastManager`.
```java
...
IEnigmaCastManager enigmaCastManager = EnigmaCastManager.getSharedInstance(getApplicationContext());
enigmaCastManager.addCastListener(new BaseEnigmaCastListener() {
    @Override
    public void onVolumeChange(float volume, boolean muted) {
        //Volume changed
    }
    ...
    //Any other custom events of interest
    ...
});
...
```

By attaching the listener to the `EnigmaCastManager` the listener will receive all events for any currently active `IEnigmaCastSession`.
The listener can also be attached to a specific `IEnigmaCastSession` and will in that case only receive events related to that cast session.

```java
IEnigmaCastListener enigmaCastListener = new BaseEnigmaCastListener() {
    ...
    //events of interest
    ...
};

//Attach listener to current session if one exists
IEnigmaCastSession currentCastSession = enigmaCastManager.getCurrentEnigmaCastSession();
if(currentCastSession != null) {
    currentCastSession.addCastListener(enigmaCastListener);
}

//Listen for castSession changes
enigmaCastManager.addListener(new BaseEnigmaCastManagerListener() {
    @Override
    public void onCastSessionChanged(IEnigmaCastSession oldSession, IEnigmaCastSession newSession) {
        if(oldSession != null) {
            //Detach from old session
            oldSession.removeCastListener(enigmaCastListener);
        }
        if(newSession != null) {
            //Attach to new session
            newSession.addCastListener(enigmaCastListener);
        }
    }
});
```


#### Sending requests to the receiver app

To send a request to an active `IEnigmaCastSession` use the factory methods in `EnigmaCastMessage` to create a `ICastControlRequest` and send it using `IEnigmaCastSession#sendMessage(...)`.

```java
IEnigmaCastSession currentCastSession = enigmaCastManager.getCurrentEnigmaCastSession();
if(currentCastSession != null) {
    currentCastSession.sendMessage(EnigmaCastMessage.goToLive()); //For example
    //or currentCastSession.sendMessage(EnigmaCastMessage.playNextProgram());
    //or currentCastSession.sendMessage(EnigmaCastMessage.selectAudioTrack("de", null));
    //etc.
} else {
    //Currently not connected...
}
```


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Integrating Red Bee OTT cast support](chromecast.md)<br/>
Advanced usage of the cast-module (current)<br/>
