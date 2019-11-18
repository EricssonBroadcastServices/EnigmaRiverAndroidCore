package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.EnigmaPlayerState;
import com.redbeemedia.enigma.core.player.IEnigmaPlayer;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.virtualui.IVirtualControlsSettings;

/*package-protected*/ interface IVirtualButtonContainer {
    void addButton(AbstractVirtualButtonImpl virtualButton);
    IEnigmaPlayerControls getPlayerControls();
    EnigmaPlayerState getPlayerState();
    IContractRestrictions getContractRestrictions();
    IEnigmaPlayer getEnigmaPlayer();
    IVirtualControlsSettings getSettings();
    IPlaybackSession getPlaybackSession();
}
