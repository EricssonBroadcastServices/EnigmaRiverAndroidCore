# Timeline API
The timeline API is used for implementing/integrating a custom timeline UI element.
It can be accessed using `IEnigmaPlayer#getTimeline`.

## ITimeline

Let's take a look at the `ITimeline` interface.
```java
public interface ITimeline {
    void addListener(ITimelineListener listener);
    void addListener(ITimelineListener listener, Handler handler);
    void removeListener(ITimelineListener listener);
    ITimelinePosition getCurrentPosition();
    ITimelinePosition getCurrentStartBound();
    ITimelinePosition getCurrentEndBound();
    boolean getVisibility();
}
```

The interface provides 4 properties:
* `currentPosition` - A `ITimelinePosition` representing the current playback position in the stream.
* `currentStartBound` -  A `ITimelinePosition` representing the earliest position of the current stream.
* `CurrentEndBound` -  A `ITimelinePosition` representing the latest position of the current stream.
* `visibility` - A `boolean` used to indicate when a timeline UI element should be active or not.

For each of these, `ITimeline` provides a getter as well as an associated change-event that can be
listened to by extending `BaseTimelineListener`:

```java
IEnigmaPlayer player = ...;
ITimeline timeline = player.getTimeline();
timeline.addListener(new BaseTimelineListener() {
    @Override
    public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
        //Current position changed
    }

    @Override
    public void onBoundsChanged(ITimelinePosition start, ITimelinePosition end) {
        //Timeline bounds changed
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        //Recommended visibility value changed
    }

    @Override
    public void onDashMetadata(Metadata metadata){
        // handle Dash metadata event
    }


    @Override
    public void onHlsMetadata(metadata: HlsMediaPlaylist?) {
        // handle Hls metadata event
    }
```


## ITimelinePosition

A `ITimelinePosition` represents a position on the timeline.
```java
public interface ITimelinePosition {
    ...
    ITimelinePosition add(Duration duration);
    ITimelinePosition subtract(Duration duration);
    Duration subtract(ITimelinePosition other);
    ...
}
```
We can add and subtract `Duration`s from a `ITimelinePosition` to get a new one.
We can also subtract one `ITimelinePosition` from another one to get the duration between them.
It is also possible to use `ITimelinePosition`s for seeking with the `IEnigmaPlayerControls`.
```java
public interface IEnigmaPlayerControls {
    ...
    void seekTo(ITimelinePosition timelinePos);
    void seekTo(ITimelinePosition timelinePos, IControlResultHandler resultHandler);
    ...
}
```

#### Example - Seeking to the middle of the timeline
```java
public void seekToMiddle(IEnigmaPlayer player) {
    ITimeline timeline = player.getTimeline();

    if(timeline.getVisibility()) {
        ITimelinePosition start = timeline.getCurrentStartBound();
        ITimelinePosition end = timeline.getCurrentEndBound();

        if(start != null && end != null) {
            Duration timelineLength = end.subtract(start);
            Duration halfTimelineLength = timelineLength.multiply(0.5f);
            ITimelinePosition middleOfTimeline = start.add(halfTimelineLength);

            player.getControls().seekTo(middleOfTimeline);
        }
    }
}
```
#### Example - Seeking 25 seconds into the stream
```java
public void seekToExample(IEnigmaPlayer player) {
    ITimeline timeline = player.getTimeline();

    if(timeline.getVisibility()) {
        ITimelinePosition start = timeline.getCurrentStartBound();

        if(start != null) {
            ITimelinePosition examplePos = start.add(Duration.seconds(25));

            player.getControls().seekTo(examplePos);
        }
    }
}
```


### Metadata eventStreams listener
To get EventStream events listen to Timeline listener
```java
IEnigmaPlayer player = ...;
ITimeline timeline = player.getTimeline();
timeline.addListener(new BaseTimelineListener() {
    
    @Override
    public void onDashMetadata(Metadata metadata){
        // handle meta data event
        for (i in 0 until metadata!!.length()) {
            val entry = metadata[i]
            println(String((entry as EventMessage).messageData))
            println((metadata[0] as EventMessage).schemeIdUri)
        }
    });

    @Override
    public void onHlsMetadata(metadata: HlsMediaPlaylist?) {
        for (tag in metadata!!.tags) {
            if (tag.lowercase(Locale.ROOT).contains("X-COM-DAICONNECT-TRACK".toLowerCase(Locale.ROOT))) {
                // We only interested in DAICONNECT-TRACK
                val base64 =
                tag.split("X-COM-DAICONNECT-TRACK=\"").toTypedArray()[1].replace("\"","")
                var encodedString: String? = String(Base64.getDecoder().decode(base64))
            }
        }
    }
```

### TimelinePositionFormat

To get a `String` representation of a `ITimelinePosition` we need to specify a
`TimelinePositionFormat`. A `TimelinePositionFormat` is simply a combination of a `java.text.DateFormat` and a `IDurationFormat`.
```java
public interface ITimelinePosition {
    ...
    String toString(TimelinePositionFormat timelinePositionFormat);
    ...
}
```

If the asset is a live stream (and has defined a start time), the `java.text.DateFormat` provided will be used to format a `Date`
to a `String`. Otherwise the `IDurationFormat` will be used to convert a `Duration` to a `String`.

```java
...
private TimelinePositionFormat timelinePositionFormat =
    TimelinePositionFormat.newFormat("${minutes}m${sec}s", new SimpleDateFormat("hh:mm a"));
...
...
    public String getCurrentPositionAsString(ITimeline timeline) {
        ITimelinePosition current = timeline.getCurrentPosition();
        if(current != null) {
            return current.toString(timelinePositionFormat);
        } else {
            return null;
        }
    }
...
```


## IAdIncludedTimeline

Helper functions for timeline position with ads

```java
public interface IAdIncludedTimeline extends ITimeline, ITimelineListener {

    /** Returns the duration of all completed ads prior to current position. */
    Duration getPastAdDuration();

    /** Returns the current ad break (or null if not playing an ad break). The start time of the ad break is transposed.*/
    @Nullable AdBreak getCurrentAdBreak();

    /** If `active`, the timeline will take ad information into account. */
    void setIsActive(boolean isActive);

    /** Returns true if `setIsActive` has been invoked and if ads has been detected. */
    boolean isActive();

    /** Returns the transposed ad breaks for this timeline as points in the timeline. */
    @Nullable List<ITimelinePosition> getAdBreaksPositions();

    /** Returns the ad breaks for this timeline  */
    @Nullable List<AdBreak> getAdBreaks();
}
```



___
[Table of Contents](../index.md)<br/>
