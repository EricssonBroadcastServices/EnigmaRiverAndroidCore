package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.util.Collector;
import com.redbeemedia.enigma.core.util.HandlerWrapper;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;
import com.redbeemedia.enigma.core.video.IVideoTrack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class will be removed or finalized in a future version. It is only here temporarily to allow direct playback by URLs.
 */
// TODO remove class
/*package-protected*/ class MinimalPlaceholderPlaybackSession implements IInternalPlaybackSession {
    private final IEnigmaPlayerConnection playerConnection = new IEnigmaPlayerConnection() {
        @Override
        public void openConnection(ICommunicationsChannel communicationsChannel) {

        }

        @Override
        public void severConnection() {

        }
    };
    private final ListenerCollector listeners = new ListenerCollector();
    private final OpenContainer<List<ISubtitleTrack>> subtitleTracks = new OpenContainer<>(new ArrayList<>());
    private final OpenContainer<List<IAudioTrack>> audioTracks = new OpenContainer<>(new ArrayList<>());
    private final OpenContainer<IAudioTrack> selectedAudioTrack = new OpenContainer<>(null);
    private final OpenContainer<ISubtitleTrack> selectedSubtitleTrack = new OpenContainer<>(null);
    private final OpenContainer<IVideoTrack> selectedVideoTrack = new OpenContainer<>(null);
    private final IContractRestrictions contractRestrictions = new IContractRestrictions() {
        @Override
        public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
            if(restriction == ContractRestriction.REWIND_ENABLED) {
                return (T) Boolean.TRUE;
            } else if(restriction == ContractRestriction.FASTFORWARD_ENABLED) {
                return (T) Boolean.TRUE;
            }
            return fallback;
        }
    };

    @Override
    public void onStart(IEnigmaPlayer enigmaPlayer) {
    }

    @Override
    public void onStop(IEnigmaPlayer enigmaPlayer) {

    }

    @Override
    public StreamInfo getStreamInfo() {
        return null;
    }

    @Override
    public IStreamPrograms getStreamPrograms() {
        return null;
    }

    @Override
    public IPlaybackSessionInfo getPlaybackSessionInfo() {
        return null;
    }

    @Override
    public IEnigmaPlayerConnection getPlayerConnection() {
        return playerConnection;
    }

    @Override
    public void setPlayingFromLive(boolean live) {

    }

    @Override
    public void fireEndReached() {
        listeners.onEndReached();
    }

    @Override
    public void fireSeekCompleted() {
        // REMEMBER Remember to send to listeners when such an event is exposed to app developers.
    }

    @Override
    public void setTracks(Collection<? extends IPlayerImplementationTrack> tracks) {
        List<IAudioTrack> newAudioTracks = new ArrayList<>();
        List<ISubtitleTrack> newSubtitleTracks = new ArrayList<>();
        for(IPlayerImplementationTrack track : tracks) {
            addIfNotNull(newAudioTracks, track.asAudioTrack());
            addIfNotNull(newSubtitleTracks, track.asSubtitleTrack());
        }
        OpenContainerUtil.setValueSynchronized(audioTracks, newAudioTracks, (oldValue, newValue) -> listeners.onAudioTracks(newValue));
        OpenContainerUtil.setValueSynchronized(subtitleTracks, newSubtitleTracks, (oldValue, newValue) -> listeners.onSubtitleTracks(newValue));
    }

    private static <T> void addIfNotNull(Collection<T> collection, T obj) {
        if(obj != null) {
            collection.add(obj);
        }
    }

    @Override
    public void setSelectedSubtitleTrack(ISubtitleTrack track) {
        OpenContainerUtil.setValueSynchronized(selectedSubtitleTrack, track, listeners::onSelectedSubtitleTrackChanged);
    }

    @Override
    public void setSelectedAudioTrack(IAudioTrack track) {
        OpenContainerUtil.setValueSynchronized(selectedAudioTrack, track, listeners::onSelectedAudioTrackChanged);
    }

    @Override
    public void setSelectedVideoTrack(IVideoTrack track) {
        OpenContainerUtil.setValueSynchronized(selectedVideoTrack, track, null);
    }

    @Override
    public void addListener(IPlaybackSessionListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void addListener(IPlaybackSessionListener listener, Handler handler) {
        listeners.addListener(listener, new HandlerWrapper(handler));
    }

    @Override
    public void removeListener(IPlaybackSessionListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public IPlayable getPlayable() {
        return null;
    }

    @Override
    public boolean isPlayingFromLive() {
        return false;
    }

    @Override
    public boolean isSeekToLiveAllowed() {
        return true;
    }

    @Override
    public boolean isSeekAllowed() {
        return true;
    }

    @Override
    public IContractRestrictions getContractRestrictions() {
        return contractRestrictions;
    }

    @Override
    public void setContractRestrictions(IContractRestrictions contractRestrictions) {

    }

    @Override
    public List<ISubtitleTrack> getSubtitleTracks() {
        return OpenContainerUtil.getValueSynchronized(subtitleTracks);
    }

    @Override
    public ISubtitleTrack getSelectedSubtitleTrack() {
        return OpenContainerUtil.getValueSynchronized(selectedSubtitleTrack);
    }

    @Override
    public List<IAudioTrack> getAudioTracks() {
        return OpenContainerUtil.getValueSynchronized(audioTracks);
    }

    @Override
    public IAudioTrack getSelectedAudioTrack() {
        return OpenContainerUtil.getValueSynchronized(selectedAudioTrack);
    }

    private static class ListenerCollector extends Collector<IPlaybackSessionListener> implements IPlaybackSessionListener {
        public ListenerCollector() {
            super(IPlaybackSessionListener.class);
        }


        @Override
        public void _dont_implement_IPlaybackSessionListener___instead_extend_BasePlaybackSessionListener_() {
        }

        @Override
        public void onPlayingFromLiveChanged(boolean live) {
            forEach(listener -> listener.onPlayingFromLiveChanged(live));
        }

        @Override
        public void onEndReached() {
            forEach(listener -> listener.onEndReached());
        }

        @Override
        public void onSubtitleTracks(List<ISubtitleTrack> tracks) {
            forEach(listener -> listener.onSubtitleTracks(tracks));
        }

        @Override
        public void onSelectedSubtitleTrackChanged(ISubtitleTrack oldSelectedTrack, ISubtitleTrack newSelectedTrack) {
            forEach(listener -> listener.onSelectedSubtitleTrackChanged(oldSelectedTrack, newSelectedTrack));
        }

        @Override
        public void onAudioTracks(List<IAudioTrack> tracks) {
            forEach(listener -> listener.onAudioTracks(tracks));
        }

        @Override
        public void onSelectedAudioTrackChanged(IAudioTrack oldSelectedTrack, IAudioTrack newSelectedTrack) {
            forEach(listener -> listener.onSelectedAudioTrackChanged(oldSelectedTrack, newSelectedTrack));
        }

        @Override
        public void onContractRestrictionsChanged(IContractRestrictions oldContractRestrictions, IContractRestrictions newContractRestrictions) {
            forEach(listener -> listener.onContractRestrictionsChanged(oldContractRestrictions, newContractRestrictions));
        }
    }
}
