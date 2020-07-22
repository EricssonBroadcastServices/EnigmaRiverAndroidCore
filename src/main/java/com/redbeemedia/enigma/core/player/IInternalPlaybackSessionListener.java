package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.util.IInternalListener;

/*package-protected*/ interface IInternalPlaybackSessionListener extends IInternalListener {
    void onStart(OnStartArgs args);
    void onStop(OnStopArgs args);

    class OnStartArgs {
        public final IInternalPlaybackSession internalPlaybackSession;
        public final IEnigmaPlayer enigmaPlayer;
        public final IEnigmaPlayerConnection.ICommunicationsChannel communicationsChannel;

        public OnStartArgs(IInternalPlaybackSession internalPlaybackSession, IEnigmaPlayer enigmaPlayer, IEnigmaPlayerConnection.ICommunicationsChannel communicationsChannel) {
            this.internalPlaybackSession = internalPlaybackSession;
            this.enigmaPlayer = enigmaPlayer;
            this.communicationsChannel = communicationsChannel;
        }
    }

    class OnStopArgs {
        public final IInternalPlaybackSession internalPlaybackSession;
        public final IEnigmaPlayer enigmaPlayer;

        public OnStopArgs(IInternalPlaybackSession internalPlaybackSession, IEnigmaPlayer enigmaPlayer) {
            this.internalPlaybackSession = internalPlaybackSession;
            this.enigmaPlayer = enigmaPlayer;
        }
    }
}
