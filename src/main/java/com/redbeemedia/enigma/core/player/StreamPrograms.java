// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.response.IEpgResponse;
import com.redbeemedia.enigma.core.util.section.ISection;
import com.redbeemedia.enigma.core.util.section.ISectionList;
import com.redbeemedia.enigma.core.util.section.ISectionListBuilder;
import com.redbeemedia.enigma.core.util.section.SectionListBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/*package-protected*/ class StreamPrograms implements IStreamPrograms {
    private final long startUtcMillis;
    private final ISectionList<IProgram> sections;
    private final boolean isPlayingLive;
    private long deviceUtcTimeDifference;

    public StreamPrograms(IEpgResponse epgResponse, boolean isPlayingLive, Long deviceUtcTimeDifference) {
        this.isPlayingLive = isPlayingLive;
        this.deviceUtcTimeDifference = deviceUtcTimeDifference;
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        sectionListBuilder.putItem(epgResponse.getStartUtcMillis(), epgResponse.getEndUtcMillis(), null);

        List<IProgram> modifiablePrograms = new ArrayList<>();
        modifiablePrograms.addAll(epgResponse.getPrograms());
        Collections.sort(modifiablePrograms, (o1, o2) -> Long.compare(o1.getStartUtcMillis(), o2.getStartUtcMillis()));

        for(IProgram program : modifiablePrograms) {
            sectionListBuilder.putItem(program.getStartUtcMillis(), program.getEndUtcMillis(), program);
        }

        sectionListBuilder.trim(epgResponse.getStartUtcMillis(), epgResponse.getEndUtcMillis());

        this.sections = sectionListBuilder.build();

        if (isPlayingLive) {
            this.startUtcMillis = new Date().getTime();
        } else {
            this.startUtcMillis = this.sections.getFirstItem().getStartUtcMillis();
        }
    }

    @Override
    public IProgram getProgram() {
        return getProgram(10000L);
    }

    @Override
    public IProgram getProgramForEntitlementCheck() {
        return getProgram(-120 * 1000);
    }

    private IProgram getProgram(long offset) {
        long utcMillis;
        if (isPlayingLive) {
            utcMillis = new Date().getTime() - deviceUtcTimeDifference - offset;
        } else {
            if (!this.sections.isEmpty()) {
                utcMillis = this.sections.getFirstItem().getStartUtcMillis();
            } else {
                return null;
            }
        }
        ISection<IProgram> section = sections.getSectionAt(utcMillis);
        return section != null ? section.getItem() : null;
    }

    @Override
    public Long getNeighbouringSectionStartOffset(long fromOffset, boolean searchBackwards) {
        if(sections.isEmpty()) {
            return null;
        }
        long utcMillis = startUtcMillis + fromOffset;
        ISection<IProgram> section = sections.getSectionAt(utcMillis);
        if(section != null) {
            ISection<IProgram> neighbour = searchBackwards ? section.getPrevious() : section.getNext();
            return neighbour != null ? (neighbour.getStart()-startUtcMillis) : null;
        } else {
            if(utcMillis < sections.getFirstStart() && !searchBackwards) {
                ISection<IProgram> firstSection = sections.getSectionAt(sections.getFirstStart());
                return firstSection.getStart()-startUtcMillis;
            } else if(utcMillis >= sections.getLastEnd() && searchBackwards) {
                ISection<IProgram> lastSection = sections.getSectionAt(sections.getLastEnd()-1);
                return lastSection.getStart()-startUtcMillis;
            } else {
                return null;
            }
        }
    }
}
