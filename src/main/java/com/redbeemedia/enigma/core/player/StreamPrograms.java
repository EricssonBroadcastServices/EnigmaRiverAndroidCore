package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.response.IEpgResponse;
import com.redbeemedia.enigma.core.util.section.ISection;
import com.redbeemedia.enigma.core.util.section.ISectionList;
import com.redbeemedia.enigma.core.util.section.ISectionListBuilder;
import com.redbeemedia.enigma.core.util.section.SectionListBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*package-protected*/ class StreamPrograms implements IStreamPrograms {
    private final long startUtcMillis;
    private final ISectionList<IProgram> sections;

    public StreamPrograms(IEpgResponse epgResponse) {
        this.startUtcMillis = epgResponse.getStartUtcMillis();

        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(epgResponse.getStartUtcMillis(), epgResponse.getEndUtcMillis(), null);

        List<IProgram> modifiablePrograms = new ArrayList<>();
        modifiablePrograms.addAll(epgResponse.getPrograms());
        Collections.sort(modifiablePrograms, (o1, o2) -> Long.compare(o1.getStartUtcMillis(), o2.getStartUtcMillis()));

        for(IProgram program : modifiablePrograms) {
            sectionListBuilder.putItem(program.getStartUtcMillis(), program.getEndUtcMillis(), program);
        }

        sectionListBuilder.trim(epgResponse.getStartUtcMillis(), epgResponse.getEndUtcMillis());

        this.sections = sectionListBuilder.build();
    }

    @Override
    public IProgram getProgramAtOffset(long offset) {
        long utcMillis = startUtcMillis + offset;
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
