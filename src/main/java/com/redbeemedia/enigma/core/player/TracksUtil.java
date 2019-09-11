package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.util.OpenContainer;

import java.util.ArrayList;
import java.util.List;

/*package-protected*/ class TracksUtil {
    public interface IPlayerImplementationTrackConverter<T> {
        T convert(IPlayerImplementationTrack track);
    }

    public interface ITracksChangedAction<T> {
        void onChanged(List<T> newTracks);
    }

    public static class TracksUpdate<T> {
        private final IPlayerImplementationTrackConverter<T> converter;
        private final ITracksChangedAction<T> tracksChangedAction;
        private final OpenContainer<List<T>> tracks;
        private boolean changed = false;
        private List<T> newTracks = new ArrayList<>();

        public TracksUpdate(IPlayerImplementationTrackConverter<T> converter, ITracksChangedAction<T> tracksChangedAction, OpenContainer<List<T>> tracks) {
            this.converter = converter;
            this.tracksChangedAction = tracksChangedAction;
            this.tracks = tracks;
        }

        public void onPossibleNew(IPlayerImplementationTrack track) {
            T convertedTrack = converter.convert(track);
            if(convertedTrack != null) {
                newTracks.add(convertedTrack);
            }
        }

        public void update() {
            synchronized (tracks) {
                if(tracks.value == null || !tracks.value.equals(newTracks)) {
                    tracks.value = newTracks;
                    changed = true;
                }
            }
        }

        public void fireIfChanged() {
            if(changed) {
                tracksChangedAction.onChanged(newTracks);
            }
        }
    }
}
