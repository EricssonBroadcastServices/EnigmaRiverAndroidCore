# Add cast custom images on sender screen (Album Art)
#### How to set the custom images on the chromecast sender screen

Custom images can be set by extending `ImagePicker`, See following example. Now ImagePickerImpl will
send the custom images to the sender app

Read more about it on : 
https://developers.google.com/android/reference/com/google/android/gms/cast/framework/media/widget/ExpandedControllerActivity
https://developers.google.com/android/reference/com/google/android/gms/cast/framework/media/widget/MiniControllerFragment


```java
class ImagePickerImpl extends ImagePicker {

    public static final int WIDTH = 100; //<set width>;
    public static final int HEIGHT = 200; //<set width>;

    @Override
    // this will add image on ExpandedControllerActivity and MiniController Activity
    public WebImage onPickImage(@NonNull MediaMetadata mediaMetadata, ImageHints hints) {
        JSONObject var1 = new JSONObject();
        try {
            var1.put("url", CUSTOM_CAST_ALBUM_ART_THUMBNAIL_URL);
            var1.put("width", WIDTH);
            var1.put("height", HEIGHT);
        } catch (JSONException ignored) {
        }
        return new WebImage(var1);
    }
}
```

Use ImagePickerImpl in the CastOptions

```java

public class ReferenceAppCastProvider extends EnigmaCastOptionsProvider {

    //...

    @Override
    protected void buildCastOptions(CastOptions.Builder builder) {
        super.buildCastOptions(builder);
        CastMediaOptions castMediaOptions = new CastMediaOptions.Builder()
                .setMediaSessionEnabled(true)
                .setImagePicker(new ImagePickerImpl())
                .setExpandedControllerActivityClassName(ExpandedControlActivity.class.getName()).build();
        builder.setCastMediaOptions(castMediaOptions);
    }
}
```

Add castOptionsProvider in the AndroidManifest xml

```xml

<meta-data android:name="com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
    android:value="com.redbeemedia.enigma.referenceapp.cast.ReferenceAppCastProvider" />
```

Also recommended to use 'ExpandedControlActivity' where the album art will be shown

```java
public class ExpandedControlActivity extends ExpandedControllerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.expanded_controller, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }
}
```

```xml

<activity android:name="com.redbeemedia.enigma.referenceapp.cast.ExpandedControlActivity"
    android:exported="true" android:launchMode="singleTask" tools:node="merge">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
    </intent-filter>
    <meta-data android:name="android.support.PARENT_ACTIVITY"
        android:value="com.redbeemedia.enigma.referenceapp.PlayerActivity" />
</activity>
```


___
[Table of Contents](../index.md)<br/>
