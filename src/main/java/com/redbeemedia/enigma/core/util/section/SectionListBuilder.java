// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util.section;

import com.redbeemedia.enigma.core.epg.IProgram;

import java.util.ArrayList;
import java.util.List;

public class SectionListBuilder implements ISectionListBuilder<IProgram> {
    private List<SectionTemplate<IProgram>> sortedList = new ArrayList<>();

    @Override
    public ISectionList<IProgram> build() {
        List<ISection<IProgram>> sections = new ArrayList<>();
        SectionListImpl sectionList = new SectionListImpl(sections);

        int index = 0;
        for(SectionTemplate<IProgram> sectionTemplate : sortedList) {
            sections.add(new SectionImpl(sectionList, index++, sectionTemplate.start, sectionTemplate.end, sectionTemplate.item));
        }

        return sectionList;
    }

    private int getIndex(long value) {
        if(sortedList.isEmpty()) {
            return -1;
        } else {
            SectionTemplate<IProgram> first = sortedList.get(0);
            if(value < first.start) {
                return -1;
            }

            SectionTemplate<IProgram> last = sortedList.get(sortedList.size()-1);
            if(value >= last.end) {
                return sortedList.size();
            }

            for(int index = 0; index < sortedList.size(); ++index) {
                SectionTemplate<IProgram> section = sortedList.get(index);
                if(value >= section.start && value < section.end) {
                    return index;
                }
            }

            throw new RuntimeException("Could not determine index!");
        }
    }

    private IMarker getMarker(long value) {
        int index = getIndex(value);
        if(index == -1) {
            return new OutsideMarker(false);
        } else if(index >= sortedList.size()) {
            return new OutsideMarker(true);
        } else {
            SectionTemplate<IProgram> section = sortedList.get(index);
            return new SectionMarker(section, value-section.start);
        }
    }


    @Override
    public void putItem(long start, long end, IProgram item) {
        if(start >= end) {
            throw new IllegalArgumentException("start >= end");
        }
        //  -1 [  0  ][   1   ][   2   ]   3

        List<SectionTemplate<IProgram>> newSortedList = new ArrayList<>();
        newSortedList.addAll(getMarker(start).cut(sortedList)[0]);
        newSortedList.add(new SectionTemplate<>(start, end, item));
        newSortedList.addAll(getMarker(end).cut(sortedList)[1]);

        //Fill in holes
        for(int i = 1; i < newSortedList.size(); ++i) {
            SectionTemplate<IProgram> last = newSortedList.get(i-1);
            SectionTemplate<IProgram> current = newSortedList.get(i);
            if(last.end != current.start) {
                newSortedList.add(i, new SectionTemplate<>(last.end, current.start, null));
                i++;
            }
        }

        sortedList = newSortedList;
    }

    @Override
    public void trim(long start, long end) {
        //Cut off start
        sortedList = getMarker(start).cut(sortedList)[1];
        //Cut off end
        sortedList = getMarker(end).cut(sortedList)[0];
    }

    private interface IMarker {
        <IProgram> List<SectionTemplate<IProgram>>[] cut(List<SectionTemplate<IProgram>> list);
    }

    private static class SectionMarker implements IMarker {
        private SectionTemplate<?> section;
        private long relativePos;

        public SectionMarker(SectionTemplate<?> section, long relativePos) {
            this.section = section;
            this.relativePos = relativePos;
            if(relativePos < 0) {
                throw new IllegalArgumentException("relativePos < 0 (was "+relativePos+")");
            }
        }

        @Override
        public <IProgram> List<SectionTemplate<IProgram>>[] cut(List<SectionTemplate<IProgram>> list) {
            List<SectionTemplate<IProgram>> beginning = new ArrayList<>();
            List<SectionTemplate<IProgram>> end = new ArrayList<>();

            int index = list.indexOf(section);
            for(int i = 0; i < list.size(); ++i) {
                SectionTemplate<IProgram> currentSection = list.get(i);
                if(i < index) {
                    beginning.add(currentSection);
                } else if(i > index) {
                    end.add(currentSection);
                } else {
                    SectionTemplate<IProgram> firstHalf = currentSection.copy();
                    SectionTemplate<IProgram> lastHalf = currentSection.copy();

                    firstHalf.end = firstHalf.start+relativePos;
                    lastHalf.start = firstHalf.end;

                    if(!firstHalf.isEmpty()) {
                        beginning.add(firstHalf);
                    }
                    if(!lastHalf.isEmpty()) {
                        end.add(lastHalf);
                    }
                }
            }

            return new List[]{beginning, end};
        }
    }

    private static class OutsideMarker implements IMarker {
        private boolean afterSpan;

        public OutsideMarker(boolean afterSpan) {
            this.afterSpan = afterSpan;
        }

        @Override
        public <IProgram> List<SectionTemplate<IProgram>>[] cut(List<SectionTemplate<IProgram>> list) {
            if(afterSpan) {
                return new List[]{new ArrayList(list), new ArrayList()};
            } else {
                return new List[]{new ArrayList(), new ArrayList(list)};
            }
        }
    }

    private static class SectionTemplate<IProgram> {
        public long start;
        public long end;
        public com.redbeemedia.enigma.core.epg.IProgram item;

        public SectionTemplate(long start, long end, com.redbeemedia.enigma.core.epg.IProgram item) {
            this.start = start;
            this.end = end;
            this.item = item;
        }

        public SectionTemplate<IProgram> copy() {
            return new SectionTemplate<>(start, end, item);
        }

        public boolean isEmpty() {
            return end - start <= 0;
        }
    }

    private static class SectionImpl implements ISection<com.redbeemedia.enigma.core.epg.IProgram> {
        private final SectionListImpl list;
        private final int index;

        private final long start;
        private final long end;
        private final com.redbeemedia.enigma.core.epg.IProgram item;

        public SectionImpl(SectionListImpl list, int index, long start, long end, com.redbeemedia.enigma.core.epg.IProgram item) {
            this.list = list;
            this.index = index;
            this.start = start;
            this.end = end;
            this.item = item;
        }

        @Override
        public com.redbeemedia.enigma.core.epg.IProgram getItem() {
            return item;
        }

        @Override
        public ISection<IProgram> getPrevious() {
            return list.getAtIndex(index-1);
        }

        @Override
        public ISection<IProgram> getNext() {
            return list.getAtIndex(index+1);
        }

        @Override
        public long getStart() {
            return start;
        }

        @Override
        public long getEnd() {
            return end;
        }
    }

    private static class SectionListImpl implements ISectionList<com.redbeemedia.enigma.core.epg.IProgram> {
        private final List<ISection<IProgram>> sections;

        public SectionListImpl(List<ISection<IProgram>> sections) {
            this.sections = sections;
        }

        public ISection<IProgram> getAtIndex(int index) {
            if(index < 0 || index >= sections.size()) {
                return null;
            } else {
                return sections.get(index);
            }
        }


        @Override
        public ISection<IProgram> getSectionAt(long value) {
            for(ISection<IProgram> section : sections) {
                long startTm = section.getItem()==null? section.getStart() : section.getItem().getStartUtcMillis();
                if(startTm <= value && section.getEnd() > value) {
                    return section;
                }
            }
            return null;
        }

        @Override
        public IProgram getItemAt(long value) {
            ISection<IProgram> section = getSectionAt(value);
            return section != null ? section.getItem() : null;
        }

        @Override
        public long getFirstStart() {
            if(!sections.isEmpty()) {
                return sections.get(0).getStart();
            } else {
                return 0;
            }
        }

        @Override
        public IProgram getFirstItem() {
            if(!sections.isEmpty()) {
                return sections.get(0).getItem();
            } else {
                return null;
            }
        }

        @Override
        public long getLastEnd() {
            if(!sections.isEmpty()) {
                return sections.get(sections.size()-1).getEnd();
            } else {
                return 0;
            }
        }

        @Override
        public boolean isEmpty() {
            return sections.isEmpty();
        }
    }
}
