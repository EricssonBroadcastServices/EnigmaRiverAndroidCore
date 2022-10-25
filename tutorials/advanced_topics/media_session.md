# Media Session
This tutorial describes how to configure media session connector.
Media sessions are an integral link between the Android platform and media apps. Not only does it inform Android that media is playing—so that it can forward media actions into the correct session—but it also informs the platform what is playing and how it can be controlled.

## Set up Media Session Connector

```java
        MediaSessionCompat mediaSession = exoPlayerTech.createMediaSession(getApplicationContext());
        mediaSession.setActive(true);
```

API
```java
        exoPlayerTech.createMediaSession(getApplicationContext());
```

API will return MediaSessionCompat and it can be used to make it aware of different media sessions.

## Disclaimer

Please note that this tutorial will be a subject for change and will be extended in future.



___
[Table of Contents](../index.md)<br/>
