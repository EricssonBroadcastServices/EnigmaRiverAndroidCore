package com.redbeemedia.enigma.core.player;

/*package-protected*/ interface IPlaybackSession {
    void onStart(IEnigmaPlayer enigmaPlayer);
    void onStop(IEnigmaPlayer enigmaPlayer);
}
