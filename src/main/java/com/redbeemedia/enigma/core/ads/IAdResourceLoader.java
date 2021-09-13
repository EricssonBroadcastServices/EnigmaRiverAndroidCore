package com.redbeemedia.enigma.core.ads;

/** Responsible of fetching and providing meta data required by SSAI */
public interface IAdResourceLoader {

    interface IAdsResourceLoaderDelegate {

        void onEntriesLoaded(VastAdEntrySet entries);

    }

    void load(IAdsResourceLoaderDelegate delegate);

}
