### Custom UI controls series
# Custom timeline
We are missing a very important UI element that every respectable player
should have, and that is the timeline, a progress bar indicating how
much was played from our current video.

We will use a very basic custom UI element, just to demonstrate the
basics of a timeline.

The visual part of it works as following:

```java
...
public class TimelineView extends View{
...
    private int timelinePad = 15;
...
    @Override
    protected void onDraw(Canvas canvas) {
        //draw the whole bar
        paint.setColor(Color.BLACK);
        canvas.drawRect(canvas.getClipBounds(),paint);

        //draw the progress
        Rect rect = new Rect();
        rect.set(timelinePad ,timelinePad, ((int) ((getMeasuredWidth()-2*timelinePad)*currentPos)+timelinePad), getMeasuredHeight()-timelinePad);
        paint.setColor(Color.GREEN);
        float cornerRadius = timelinePad;
        canvas.drawRoundRect(new RectF(rect), cornerRadius, cornerRadius, paint);

        //draw time bounds
        paint.setColor(Color.WHITE);
        paint.setTextSize(1.5f*(getMeasuredHeight()-timelinePad*2)/2);
        float fontPad = (getMeasuredHeight()-paint.getFontMetrics(null)*0.8f)/2;
        if(pos != null && end != null) {
            String text = pos.toString(timelinePositionFormat)+" / "+end.toString(timelinePositionFormat);
            canvas.drawText(text, rect.left+(getMeasuredWidth()-timelinePad*2-paint.measureText(text))/2, getMeasuredHeight()-fontPad, paint);
        }
    }
...
}
```

We have some unknowns here, so let's try to clarify them one by one.

`currentPos` represents the current position of the player from the time
perspective, and `pos` represents the progress perspective, so in order
to get them we need to see how our timeline integrates with the enigma
player:

```java
...
public void connectTo(IEnigmaPlayer enigmaPlayer) {
    this.controls = enigmaPlayer.getControls();
    enigmaPlayer.getTimeline().addListener(new BaseTimelineListener() {
        ...

        private void recalculatePos() {
            if(start != null && end != null && pos != null) {
                TimelineView.this.currentPos = pos.subtract(start).inUnits(Duration.Unit.MILLISECONDS)/end.subtract(start).inUnits(Duration.Unit.MILLISECONDS);
            } else {
                TimelineView.this.currentPos = 0f;
            }
            TimelineView.this.postInvalidate();
        }

        @Override
        public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
            TimelineView.this.pos = timelinePosition;
            recalculatePos();
        }

        ...
    }, handler);
}
...
```

`timelinePositionFormat` is just a simple format to display the time:

```java
private TimelinePositionFormat timelinePositionFormat = TimelinePositionFormat.newFormat("${minutes}m${sec}s", new SimpleDateFormat("hh:mm:ss a"));
```

And just like with the play/pause button, we connect the player with the
timeline in the playback Activity:

```java
...
IEnigmaPlayer player = new EnigmaPlayer(session, exoPlayerTech);
...
timelineView.connectTo(player);
...
```

And that's all there is to it!<br />  
Please follow the next chapters to learn about more components.


___
[Table of Contents](../index.md)<br/>
[Prerequisites](prerequisites.md)<br/>
[Basics: play, pause and seeking](basics.md)<br/>
[Play/Pause Button](play_pause_button.md)<br/>
Custom timeline (current)<br/>
[Spinner and Live Indicator](spinner_and_live.md)<br/>
[Custom UI app](custom_ui_app.md)<br/>
