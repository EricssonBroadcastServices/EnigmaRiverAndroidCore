<!--
SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>

SPDX-License-Identifier: MIT
-->

### Migration guide (Android SDK 2.0 -> Enigma River Android SDK)
# Structural changes
While the flow of information throughout the SDK is still very similar (since both the old SDK and
the new integrate towards the same Red Bee OTT backend) there are a few structural changes in the
new SDK compared to the old one.

## "Tech" is replaced by "PlayerImplementation"

The old SDK contains the concept of a `Tech` as an abstraction for a player technology. The new SDK
contains a core-module that can work with any player technology. A player technology is now called
a "player implementation". ExoPlayer is one player implementation. The player implementation used
by an app has to be added separately as a dependency and injected into `EnigmaPlayer`.

## Playback session

The new SDK introduces the `PlaybackSession` component which represents the playback of a stream
beginning at the end of a successful play-call to the backend, and ending when the stream is stopped.
This new component is essential and provides information about contract restrictions for the stream,
tracks, and states of the stream (for example, live/not live).



___
[Table of Contents](../index.md)<br/>
[Introduction](introduction.md)<br/>
&bull; Structural changes (current)<br/>
[Changes to SDK initialization](sdk_initialization.md)<br/>
[Changes to authentication/login](login.md)<br/>
[Changes to asset metadata retrieval](asset_metadata.md)<br/>
[Changes to playback](playback.md)<br/>
[Further reading](further_reading.md)<br/>
