package com.redbeemedia.enigma.core.util.section;

import org.junit.Assert;
import org.junit.Test;

public class SectionListBuilderTest {

    @Test
    public void testAddKindList() {
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(200, 5000, "First");
        sectionListBuilder.putItem(15000, 20000, "Fourth");
        sectionListBuilder.putItem(5000, 10000, "Second");
        sectionListBuilder.putItem(10000, 15000, "Third");

        ISectionList<String> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getSectionAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200));
        Assert.assertEquals("First", sectionList.getItemAt(4999));
        Assert.assertEquals("Second", sectionList.getItemAt(5000));
        Assert.assertEquals("Third", sectionList.getItemAt(11000));
        Assert.assertEquals("Fourth", sectionList.getItemAt(15000));
        Assert.assertEquals("Fourth", sectionList.getItemAt(16000));

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
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(200, 5000, "First");
        sectionListBuilder.putItem(5000, 10000, "Second");
        //gap
        sectionListBuilder.putItem(15000, 20000, "Third");

        ISectionList<String> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getSectionAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200));
        Assert.assertEquals("First", sectionList.getItemAt(4999));
        Assert.assertEquals("Second", sectionList.getItemAt(5000));
        Assert.assertEquals(null, sectionList.getItemAt(10000));
        Assert.assertEquals("Third", sectionList.getItemAt(15000));
        Assert.assertEquals("Third", sectionList.getItemAt(17000));

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
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(0, 22000, "FALLBACK");
        sectionListBuilder.putItem(200, 5000, "First");
        sectionListBuilder.putItem(5000, 10000, "Second");
        //gap
        sectionListBuilder.putItem(15000, 20000, "Third");

        ISectionList<String> sectionList = sectionListBuilder.build();
        Assert.assertEquals("FALLBACK", sectionList.getItemAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200));
        Assert.assertEquals("First", sectionList.getItemAt(4999));
        Assert.assertEquals("Second", sectionList.getItemAt(5000));
        Assert.assertEquals("FALLBACK", sectionList.getItemAt(10000));
        Assert.assertEquals("Third", sectionList.getItemAt(15000));
        Assert.assertEquals("Third", sectionList.getItemAt(17000));

        Assert.assertEquals("FALLBACK", sectionList.getItemAt(20000));

        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(0 ,sectionList.getFirstStart());
        Assert.assertEquals(22000 ,sectionList.getLastEnd());


        Assert.assertEquals("0[FALLBACK]200[First]5000[Second]10000[FALLBACK]15000[Third]20000[FALLBACK]22000", printSections(sectionList));
    }

    @Test
    public void testOverwrite() {
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(200, 5000, "First");
        sectionListBuilder.putItem(5000, 10000, "Second");
        //gap
        sectionListBuilder.putItem(15000, 20000, "Third");
        sectionListBuilder.putItem(0, 20000, "OVERWRITE");

        ISectionList<String> sectionList = sectionListBuilder.build();
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(0));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(200));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(4999));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(5000));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(10000));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(15000));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(17000));

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
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(200, 5000, "First");
        sectionListBuilder.putItem(5000, 10000, "Second");
        //gap
        sectionListBuilder.putItem(15000, 20000, "Third");
        sectionListBuilder.putItem(6000, 18000, "OVERWRITE");

        ISectionList<String> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getItemAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200));
        Assert.assertEquals("First", sectionList.getItemAt(4999));
        Assert.assertEquals("Second", sectionList.getItemAt(5000));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(6000));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(10000));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(15000));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(17000));
        Assert.assertEquals("Third", sectionList.getItemAt(18000));

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
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(200, 10000, "Long first");
        sectionListBuilder.putItem(10000, 20000, "Long second");
        sectionListBuilder.putItem(6000, 9000, "On top");

        ISectionList<String> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getItemAt(0));
        Assert.assertEquals("Long first", sectionList.getItemAt(200));
        Assert.assertEquals("On top", sectionList.getItemAt(6000));
        Assert.assertEquals("On top", sectionList.getItemAt(8999));
        Assert.assertEquals("Long first", sectionList.getItemAt(9000));
        Assert.assertEquals("Long first", sectionList.getItemAt(9999));
        Assert.assertEquals("Long second", sectionList.getItemAt(10000));
        Assert.assertEquals("Long second", sectionList.getItemAt(18000));

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
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(200, 5000, "First");
        sectionListBuilder.putItem(5000, 10000, "Second");
        //gap
        sectionListBuilder.putItem(15000, 20000, "Third");
        sectionListBuilder.putItem(200, 4000, "OVERWRITE");

        ISectionList<String> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getItemAt(0));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(200));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(3999));
        Assert.assertEquals("First", sectionList.getItemAt(4000));
        Assert.assertEquals("First", sectionList.getItemAt(4999));
        Assert.assertEquals("Second", sectionList.getItemAt(5000));
        Assert.assertEquals(null, sectionList.getItemAt(10000));
        Assert.assertEquals("Third", sectionList.getItemAt(15000));
        Assert.assertEquals("Third", sectionList.getItemAt(17000));

        Assert.assertEquals(null, sectionList.getSectionAt(20000));
        Assert.assertEquals(null, sectionList.getItemAt(20000));

        Assert.assertEquals(null, sectionList.getSectionAt(25000));
        Assert.assertEquals(null, sectionList.getItemAt(25000));


        Assert.assertEquals(200 ,sectionList.getFirstStart());
        Assert.assertEquals(20000 ,sectionList.getLastEnd());


        Assert.assertEquals("200[OVERWRITE]4000[First]5000[Second]10000[null]15000[Third]20000", printSections(sectionList));
    }

    @Test
    public void testOverwriteEnd() {
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(200, 5000, "First");
        sectionListBuilder.putItem(5000, 10000, "Second");
        //gap
        sectionListBuilder.putItem(15000, 20000, "Third");
        sectionListBuilder.putItem(17000, 20000, "OVERWRITE");

        ISectionList<String> sectionList = sectionListBuilder.build();
        Assert.assertEquals(null, sectionList.getItemAt(0));
        Assert.assertEquals("First", sectionList.getItemAt(200));
        Assert.assertEquals("First", sectionList.getItemAt(4999));
        Assert.assertEquals("Second", sectionList.getItemAt(5000));
        Assert.assertEquals(null, sectionList.getItemAt(10000));
        Assert.assertEquals("Third", sectionList.getItemAt(15000));
        Assert.assertEquals("Third", sectionList.getItemAt(16999));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(17000));
        Assert.assertEquals("OVERWRITE", sectionList.getItemAt(19999));

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
        ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
        sectionListBuilder.putItem(0, 0, "Test");
    }

    @Test
    public void testIsEmpty() {
        { //non-empty list
            ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();
            sectionListBuilder.putItem(0, 100, "Test");

            ISectionList<String> sectionList = sectionListBuilder.build();
            Assert.assertFalse("isEmpty() returned true but one item was added.", sectionList.isEmpty());
        }
        { //empty list
            ISectionListBuilder<String> sectionListBuilder = new SectionListBuilder<>();

            ISectionList<String> sectionList = sectionListBuilder.build();
            Assert.assertTrue("isEmpty() returned false but should have been true.",sectionList.isEmpty());
        }
    }

    private static String printSections(ISectionList<String> sectionList) {
        StringBuilder stringBuilder = new StringBuilder();

        ISection<String> section = sectionList.getSectionAt(sectionList.getFirstStart());
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
