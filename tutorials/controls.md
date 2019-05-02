# Controlling EnigmaPlayer programmatically

There are two main ways one usually interacts with a video player. One is by starting up a stream
(which in our case is done using `IEnigmaPlayer#play(IPlayRequest)`). The other way is
by controlling the playback of an already started/loaded stream by sending 'control requests' such as 'start','pause', 'seekTo', etc. This tutorial covers the latter.

Methods for controlling the playback of a stream are found in the `IEnigmaPlayerControls` interface. To access these methods use `IEnigmaPlayer#getControls()`.
```java
IEnigmaPlayerControls controls = enigmaPlayer.getControls();
```

They all come in two flavours,
```java
public interface IEnigmaPlayerControls {
    ...
    void [name_of_command](...arguments...);
    void [name_of_command](...arguments..., IControlResultHandler resultHandler);
    ...
}
```
and should all be called from the main thread.

#### Example
```java
pauseButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        controls.pause();
    }
});
```

## IControlResultHandler
Since it is not certain when or if the the underlying player implementation will execute the
requested command, they are all modelled as requests that can use result handlers.

An implementation of `IControlResultHandler` can be added to control requests if extra callbacks
are necessary.
```java
public interface IControlResultHandler {
    void onRejected(IRejectReason reason);
    void onCancelled();
    void onError(Error error);
    void onDone();
}
```
Exactly one of these methods will be called depending on the fate of the request.

* `onRejected(IRejectReason reason)` is called if the request was considered invalid by some part of the framework.
* `onCancelled()` is called if the request was valid and queued for execution but cancelled before or
during it was processed.
* `onError(Error error)` is called if something went wrong while processing the request.
* `onDone()` is called when the request has been successfully processed. This signals that command was carried out.




