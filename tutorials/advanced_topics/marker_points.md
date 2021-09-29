# Marker points
Description : Cue points for the active material will be exposed on the content/asset metadata through exposure.

Types of cue points that can be set
```
Intro
Point
Chapter
Credits
```

#CODE 
SDK exposes MarkerPoints information as : 
```java
    getEnigmaPlayer().getMarkerPointsDetector()
```

and following information is exposed which can be used
```java
public interface IMarkerPointsDetector {
/**
* Method return all marker points
* @return
*/
    List<MarkerPoint> getMarkerPointsList();

    /**
     * this method return current MarkerPoint otherwise it return null, if there is no marker point
     * @return
     */
    MarkerPoint getCurrentMarkerPoint();


    /**
     * this method return last MarkerPoint otherwise it return null, if there is no marker point before this current time
     * @return
     */
    MarkerPoint getLastMarkerPoint();
```

# Configuration

In our implementation we will use Intro and Credits as data points, but visualize only Points and Chapters in the skin and timeline.


### Example configuration

```
"markerPoints": [
{
"type": "",
"offset": 0,
"endOffset": 0,
"thumbnail": "",
"localized": [
{
"locale": "",
"title": "",
"image": {
"height": 0,
"orientation": "",
"type": "",
"url": "",
"width": 0
}
}
]
}
]

```




___
[Table of Contents](../index.md)<br/>
