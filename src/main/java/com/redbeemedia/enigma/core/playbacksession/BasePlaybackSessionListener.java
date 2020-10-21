package com.redbeemedia.enigma.core.playbacksession;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.video.IVideoTrack;

import java.util.List;

public class BasePlaybackSessionListener implements IPlaybackSessionListener {
    @Deprecated
    @Override
    public final void _dont_implement_IPlaybackSessionListener___instead_extend_BasePlaybackSessionListener_() {
    }

    @Override
    public void onPlayingFromLiveChanged(boolean live) {
    }

    @Override
    public void onEndReached() {
    }

    @Override
    public void onSubtitleTracks(List<ISubtitleTrack> tracks) {
    }

    @Override
    public void onSelectedSubtitleTrackChanged(ISubtitleTrack oldSelectedTrack, ISubtitleTrack newSelectedTrack) {
    }

    @Override
    public void onAudioTracks(List<IAudioTrack> tracks) {
    }

    @Override
    public void onSelectedAudioTrackChanged(IAudioTrack oldSelectedTrack, IAudioTrack newSelectedTrack) {
    }

    @Override
    public void onVideoTracks(List<IVideoTrack> tracks) {
    }

    @Override
    public void onSelectedVideoTrackChanged(IVideoTrack oldSelectedTrack, IVideoTrack newSelectedTrack) {
    }

    @Override
    public void onContractRestrictionsChanged(IContractRestrictions oldContractRestrictions, IContractRestrictions newContractRestrictions) {
    }
}
