package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Represents a set of VAST ad entries.
 */
class VastAdEntrySet {

    private final Collection<VastAdEntry> entries;

    VastAdEntrySet(Collection<VastAdEntry> entries) {
        this.entries = entries;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof VastAdEntrySet)) { return false; }

        VastAdEntrySet other = (VastAdEntrySet)o;
        if(entries.size() != other.entries.size()) {
            return false;
        }

        ArrayList<VastAdEntry> myEntries = new ArrayList<>(entries);
        ArrayList<VastAdEntry> otherEntries = new ArrayList<>(other.entries);

        for(int i = 0; i < entries.size(); i++) {
            if(!myEntries.get(i).equals(otherEntries.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Nullable VastAdEntry getEntry(long currentTime) {
        Iterator<VastAdEntry> iterator = entries.iterator();
        while(iterator.hasNext()) {
            VastAdEntry entry = iterator.next();
            long endTime = entry.getStartTime() + entry.getDuration();

            long graceTime = Math.max(0, VastAdEntry.AD_GRACE_MAXIMUM_MS - entry.getDuration());

            if(currentTime >= entry.getStartTime() - graceTime &&
               currentTime <= endTime + graceTime &&
               entry.getDuration() > 0) {
                   entry.setCurrentTime(currentTime);
                   return entry;
            }
        }
        return null;
    }

    Collection<VastAdEntry> getEntries() { return entries; }
}
