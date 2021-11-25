package com.redbeemedia.enigma.core.util.section;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.MockProgram;

import org.junit.Assert;
import org.junit.Test;

public class SectionListBuilderTest {

    @Test
    public void testAddKindList() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        addItem(sectionListBuilder, 200, 5000, "First");
        addItem(sectionListBuilder, 15000, 20000, "Fourth");
        addItem(sectionListBuilder, 5000, 10000, "Second");
        addItem(sectionListBuilder, 10000, 15000, "Third");

        ISectionList<IProgram> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getSectionAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200).toString());
        Assert.assertEquals("First", sectionList.getItemAt(4999).toString());
        Assert.assertEquals("Second", sectionList.getItemAt(5000).toString());
        Assert.assertEquals("Third", sectionList.getItemAt(11000).toString());
        Assert.assertEquals("Fourth", sectionList.getItemAt(15000).toString());
        Assert.assertEquals("Fourth", sectionList.getItemAt(16000).toString());

        Assert.assertEquals(null, sectionList.getSectionAt(20000));
        Assert.assertEquals(null, sectionList.getItemAt(20000));
        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));

        Assert.assertEquals(200 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());

        Assert.assertEquals("200[First]5000[Second]10000[Third]15000[Fourth]20000", printSections(sectionList));
    }

    @Test
    public void testListWithGap() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        addItem(sectionListBuilder, 200, 5000, "First");
        addItem(sectionListBuilder, 5000, 10000, "Second");
        //gap
        addItem(sectionListBuilder, 15000, 20000, "Third");

        ISectionList<IProgram> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getSectionAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200).toString());
        Assert.assertEquals("First", sectionList.getItemAt(4999).toString());
        Assert.assertEquals("Second", sectionList.getItemAt(5000).toString());
        Assert.assertEquals(null, sectionList.getItemAt(10000));
        Assert.assertEquals("Third", sectionList.getItemAt(15000).toString());
        Assert.assertEquals("Third", sectionList.getItemAt(17000).toString());

        Assert.assertEquals(null, sectionList.getSectionAt(20000));
        Assert.assertEquals(null, sectionList.getItemAt(20000));
        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(200 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());


        Assert.assertEquals("200[First]5000[Second]10000[null]15000[Third]20000", printSections(sectionList));
    }

    @Test
    public void testListWithGapAndFallback() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        addItem(sectionListBuilder, 0, 22000, "FALLBACK");
        addItem(sectionListBuilder, 200, 5000, "First");
        addItem(sectionListBuilder, 5000, 10000, "Second");
        //gap
        addItem(sectionListBuilder, 15000, 20000, "Third");

        ISectionList<IProgram> sectionList = sectionListBuilder.build();
        Assert.assertEquals("FALLBACK", sectionList.getItemAt(0).toString());
        Assert.assertEquals("First", sectionList.getItemAt(200).toString());
        Assert.assertEquals("First", sectionList.getItemAt(4999).toString());
        Assert.assertEquals("Second", sectionList.getItemAt(5000).toString());
        Assert.assertEquals("FALLBACK", sectionList.getItemAt(10000).toString());
        Assert.assertEquals("Third", sectionList.getItemAt(15000).toString());
        Assert.assertEquals("Third", sectionList.getItemAt(17000).toString());

        Assert.assertEquals("FALLBACK", sectionList.getItemAt(20000).toString());

        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(0 ,sectionList.getFirstStart());
        Assert.assertEquals(22000 ,sectionList.getLastEnd());


        Assert.assertEquals("0[FALLBACK]200[First]5000[Second]10000[FALLBACK]15000[Third]20000[FALLBACK]22000", printSections(sectionList));
    }

    @Test
    public void testOverwrite() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        addItem(sectionListBuilder, 200, 5000, "First");
        addItem(sectionListBuilder, 5000, 10000, "Second");
        //gap
        addItem(sectionListBuilder, 15000, 20000, "Third");
        addItem(sectionListBuilder, 0, 20000, "OVERWRITE");

        ISectionList<IProgram> sectionList = sectionListBuilder.build();
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(0).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(200).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(4999).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(5000).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(10000).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(15000).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(17000).toString());

        Assert.assertEquals(null, sectionList.getSectionAt(20000));
        Assert.assertEquals(null, sectionList.getItemAt(20000));

        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(0 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());



        Assert.assertEquals("0[OVERWRITE]20000", printSections(sectionList));
    }

    @Test
    public void testPartialOverwrite() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        addItem(sectionListBuilder, 200, 5000, "First");
        addItem(sectionListBuilder, 5000, 10000, "Second");
        //gap
        addItem(sectionListBuilder, 15000, 20000, "Third");
        addItem(sectionListBuilder, 6000, 18000, "OVERWRITE");

        ISectionList<IProgram> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getItemAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200).toString());
        Assert.assertEquals("First", sectionList.getItemAt(4999).toString());
        Assert.assertEquals("Second", sectionList.getItemAt(5000).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(6000).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(10000).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(15000).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(17000).toString());
        Assert.assertEquals("Third", sectionList.getItemAt(18000).toString());

        Assert.assertEquals(null, sectionList.getSectionAt(20000));
        Assert.assertEquals(null, sectionList.getItemAt(20000));

        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(200 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());


        Assert.assertEquals("200[First]5000[Second]6000[OVERWRITE]18000[Third]20000", printSections(sectionList));
    }

    @Test
    public void testSplit() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        sectionListBuilder.putItem(200, 10000, new MockProgram("Long first",200,10000));
        sectionListBuilder.putItem(10000, 20000, new MockProgram("Long second",10000, 20000));
        sectionListBuilder.putItem(6000, 9000, new MockProgram("On top",6000, 9000));

        ISectionList<IProgram> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getItemAt(0));
        Assert.assertEquals("Long first", sectionList.getItemAt(200).toString());
        Assert.assertEquals("On top", sectionList.getItemAt(6000).toString());
        Assert.assertEquals("On top", sectionList.getItemAt(8999).toString());
        Assert.assertEquals("Long first", sectionList.getItemAt(9000).toString());
        Assert.assertEquals("Long first", sectionList.getItemAt(9999).toString());
        Assert.assertEquals("Long second", sectionList.getItemAt(10000).toString());
        Assert.assertEquals("Long second", sectionList.getItemAt(18000).toString());

        Assert.assertEquals(null, sectionList.getSectionAt(20000));
        Assert.assertEquals(null, sectionList.getItemAt(20000));

        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(200 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());


        Assert.assertEquals("200[Long first]6000[On top]9000[Long first]10000[Long second]20000", printSections(sectionList));
    }

    @Test
    public void testOverwriteStart() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        addItem(sectionListBuilder, 200, 5000, "First");
        addItem(sectionListBuilder, 5000, 10000, "Second");
        //gap
        addItem(sectionListBuilder, 15000, 20000, "Third");
        addItem(sectionListBuilder, 200, 4000, "OVERWRITE");

        ISectionList<IProgram> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getItemAt(0));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(200).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(3999).toString());
        Assert.assertEquals("First", sectionList.getItemAt(4000).toString());
        Assert.assertEquals("First", sectionList.getItemAt(4999).toString());
        Assert.assertEquals("Second", sectionList.getItemAt(5000).toString());
        Assert.assertEquals(null, sectionList.getItemAt(10000));
        Assert.assertEquals("Third", sectionList.getItemAt(15000).toString());
        Assert.assertEquals("Third", sectionList.getItemAt(17000).toString());

        Assert.assertEquals(null, sectionList.getSectionAt(20000));
        Assert.assertEquals(null, sectionList.getItemAt(20000));

        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(200 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());


        Assert.assertEquals("200[OVERWRITE]4000[First]5000[Second]10000[null]15000[Third]20000", printSections(sectionList));
    }

    private void addItem(ISectionListBuilder<IProgram> sectionListBuilder, int start, int end, String name) {
        sectionListBuilder.putItem(start, end, new MockProgram(name, start, end));
    }

    @Test
    public void testOverwriteEnd() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        addItem(sectionListBuilder, 200, 5000, "First");
        addItem(sectionListBuilder, 5000, 10000, "Second");
        //gap
        addItem(sectionListBuilder, 15000, 20000, "Third");
        addItem(sectionListBuilder, 17000, 20000, "OVERWRITE");

        ISectionList<IProgram> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getItemAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200).toString());
        Assert.assertEquals("First", sectionList.getItemAt(4999).toString());
        Assert.assertEquals("Second", sectionList.getItemAt(5000).toString());
        Assert.assertEquals(null, sectionList.getItemAt(10000));
        Assert.assertEquals("Third", sectionList.getItemAt(15000).toString());
        Assert.assertEquals("Third", sectionList.getItemAt(16999).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(17000).toString());
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(19999).toString());

        Assert.assertEquals(null, sectionList.getSectionAt(20000));
        Assert.assertEquals(null, sectionList.getItemAt(20000));

        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(200 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());


        Assert.assertEquals(200 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());


        Assert.assertEquals("200[First]5000[Second]10000[null]15000[Third]17000[OVERWRITE]20000", printSections(sectionList));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEndBiggerThanStart() {
        ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
        addItem(sectionListBuilder, 0, 0, "Test");
    }

    @Test
    public void testIsEmpty() {
        { //non-empty list
            ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();
            addItem(sectionListBuilder, 0, 100, "Test");

            ISectionList<IProgram> sectionList = sectionListBuilder.build();
            Assert.assertFalse("isEmpty() returned true but one item was added.", sectionList.isEmpty());
        }
        { //empty list
            ISectionListBuilder<IProgram> sectionListBuilder = new SectionListBuilder();

            ISectionList<IProgram> sectionList = sectionListBuilder.build();
            Assert.assertTrue("isEmpty() returned false but should have been true.",sectionList.isEmpty());
        }
    }

    private static String printSections(ISectionList<IProgram> sectionList) {
        StringBuilder stringBuilder = new StringBuilder();

        ISection<IProgram> section = sectionList.getSectionAt(sectionList.getFirstStart());
        boolean first = true;
        while(section != null) {
            if(first) {
                stringBuilder.append(section.getStart());
                first = false;
            }

            stringBuilder.append("["+section.getItem()+"]");
            stringBuilder.append(section.getEnd());
            section = section.getNext();
        }

        return stringBuilder.toString();
    }
}
