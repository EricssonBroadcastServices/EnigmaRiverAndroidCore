# Sprites API
Sprites are thumbnails representing the content of a segment of the timeline. This optional implementation provides an interface to request a sprite image for a certain absolute position on the timeline. In order for sprites to be enabled, the asset has to be configured accordingly.

Sprite retrieval and configuration is accessible once the playback of an asset has started. In technical terms - once an `IPlaybackSession` has been created - the initial step for enabling (and check the availability) of sprites is to `activate` it through the `ISpriteRepository` accessible from the `IPlaybackSession`.

# Basic configuration

The sprites can come with a variety of sizes and it is possible to override how these resources are parsed and handled to the sub system. Below are examples of a basic configuration which should be suitable for most needs.

## 1. Activate sprite retrieval

This is an example of how to activate the sprites feature. `activate` will fetch the required metadata represented by the `sprites` collection and start the caching of the compressed content maps.

```java

private ISpriteRepository spriteRepository;

...

enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
    @Override
    public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
        if (to != null) {
            spriteRepository = to.getSpriteRepository();
            spriteRepository.activate(sprites -> Log.d("TAG", "Sprites available: " + (sprites.isEmpty() ? "No" : "Yes")));
        }
    }
});
```

## 2. Fetching a sprite image

Once the `ISpriteRepository`s `activate` method has been called, the caching of images commences and sprite images can be retrieved.
The images are requested through the `getSprite` method which requires either an absolute position (in milliseconds) or a `ITimeLinePosition` object representing the sprite for a requested position. The method will return `null` if the image has not yet been cached or if the download fails.

The _default_ implementation provides sprites as `Bitmap` object, but this can be overridden. Please see _#Advanced configuration_.

The following example tries to fetch a `Bitmap` image representing the sprite when the position on the timeline changes.
```java
enigmaPlayer.getTimeline().addListener(new BaseTimelineListener() {
    @Override
    public void onCurrentPositionChanged(ITimelinePosition timelinePosition) {
        spriteRepository.getSprite(timelinePosition, (BitmapImageRepository.BitmapSpriteListener) sprite -> {
            if (sprite != null) {
                Log.d("TAG", "Got bitmap with size: " + sprite.getWidth() + ", " + sprite.getHeight());
            }
        });
    }
});
```

If one wants to retrieve a sprite for an absolute position in milliseconds, it can be specified. In the following example, the sprite is positioned at the first minute.
```java
spriteRepository.getSprite(60 * 1000, (BitmapImageRepository.BitmapSpriteListener) sprite -> {
    if (sprite != null) {
        Log.d("TAG", "Got bitmap with size: " + sprite.getWidth() + ", " + sprite.getHeight());
    }
});
```

# Advanced Configuration

It might be required for optimization reasons or otherwise to override parts of the basic configuration or explicitly request additional information.

## 1. Fetching a specific sprite size

Sprites might come in a variety of sizes. The above example will provide the lowest resolution, but one can specify an explicit size (namely width). This is done when `activate` is called.

The example below will call `activate` using a specific available width fetched from `getWidth()`.
```java
if (spriteRepository.getWidths() != null && !spriteRepository.getWidths().isEmpty()) {
    ArrayList<Integer> availableWidths = new ArrayList<>(spriteRepository.getWidths());
    spriteRepository.activate(availableWidths.get(0), sprites -> Log.d("TAG", "Sprites available: " + (sprites.isEmpty() ? "No" : "Yes")));
}
```

## 2 Configure image fetching

By default, the images are retrieved as `Bitmap` objects. This might however not meet the SDK consumers requirements and thus can the image handling be overridden by implementing a custom `ISpriteImageRepository<T>` where `T` is the format returned by `ISpriteRepository`s `getSprite` method.

`ISpriteRepository` provides `setImageRepository` method which allows the SDK consumer to specify the custom handling of the images. A developer can decide to implement the interface `ISpriteImageRepository<T>` in order to gain full control of how the images are fetched or just inherit from `SpriteImageRepository<T>` if the requirement is to deserialize a different type of image object.

If `setImageRepository(null)` is invoked, only the metadata for the sprites will be fetched.

Please see the class documentation of the interfaces mentioned above for more specific details.


___
[Table of Contents](../index.md)<br/>
