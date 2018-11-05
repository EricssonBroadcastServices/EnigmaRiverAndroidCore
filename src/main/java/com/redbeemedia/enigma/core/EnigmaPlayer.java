package com.redbeemedia.enigma.core;

import java.net.URL;

public class EnigmaPlayer implements IEnigmaPlayer {
    private IPlayerImplementation playerImplementation;

    public EnigmaPlayer(IPlayerImplementation playerImplementation) {
        this.playerImplementation = playerImplementation;
    }


    @Override
    public void play(IPlayRequest playRequest) {
        IPlayable playable = playRequest.getPlayable();
        if(playable == null) {
            playRequest.onError("Playable was null");
        } else {
            playable.useWith(new PlayableHandler(playRequest));
        }
    }

    private class PlayableHandler implements IPlayableHandler {
        private IPlayRequest playRequest;

        public PlayableHandler(IPlayRequest playRequest) {
            this.playRequest = playRequest;
        }

        @Override
        public void startUsingAssetId(String assetId) {

        }

        @Override
        public void startUsingUrl(URL url) {
            playRequest.onError("Not yet implemented");
            //TODO or should we throw an exception here?
        }
    }
}
