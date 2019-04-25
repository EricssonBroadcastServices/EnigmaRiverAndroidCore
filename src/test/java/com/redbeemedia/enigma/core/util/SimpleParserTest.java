package com.redbeemedia.enigma.core.util;

import org.junit.Assert;
import org.junit.Test;

public class SimpleParserTest {
    @Test
    public void testEmptyString() {
        LoggingParseHandler loggingParseHandler = new LoggingParseHandler();
        SimpleParser.parse("", loggingParseHandler);
        Assert.assertEquals("", loggingParseHandler.log.toString());
    }

    @Test
    public void testJustText() {
        LoggingParseHandler loggingParseHandler = new LoggingParseHandler();
        SimpleParser.parse("This is just text", loggingParseHandler);
        Assert.assertEquals("text[This is just text]", loggingParseHandler.log.toString());
    }

    @Test
    public void testOneCode() {
        LoggingParseHandler loggingParseHandler = new LoggingParseHandler();
        SimpleParser.parse("${all code}", loggingParseHandler);
        Assert.assertEquals("code[all code]", loggingParseHandler.log.toString());
    }

    @Test
    public void testMultipleCode() {
        LoggingParseHandler loggingParseHandler = new LoggingParseHandler();
        SimpleParser.parse("${all code}${code 2}${code 3}", loggingParseHandler);
        Assert.assertEquals("code[all code]code[code 2]code[code 3]", loggingParseHandler.log.toString());
    }

    @Test
    public void testComplexEndingWithCode() {
        LoggingParseHandler loggingParseHandler = new LoggingParseHandler();
        SimpleParser.parse("Text here and ${variable here} or ${it might be code}", loggingParseHandler);
        Assert.assertEquals("text[Text here and ]code[variable here]text[ or ]code[it might be code]", loggingParseHandler.log.toString());
    }

    @Test
    public void testComplexEndingWithText() {
        LoggingParseHandler loggingParseHandler = new LoggingParseHandler();
        SimpleParser.parse("Text here and ${variable here} or ${it might be code}.", loggingParseHandler);
        Assert.assertEquals("text[Text here and ]code[variable here]text[ or ]code[it might be code]text[.]", loggingParseHandler.log.toString());
    }

    private static class LoggingParseHandler implements SimpleParser.IParseHandler {
        private StringBuilder log = new StringBuilder();

        @Override
        public void onText(String text) {
            log.append("text["+text+"]");
        }

        @Override
        public void onCode(String code) {
            log.append("code["+code+"]");
        }
    }
}
