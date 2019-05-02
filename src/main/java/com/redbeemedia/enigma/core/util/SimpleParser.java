package com.redbeemedia.enigma.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public final class SimpleParser {
    private static final Pattern codePattern = Pattern.compile("\\$\\{(.*?)\\}");

    public static void parse(String pattern, IParseHandler parseHandler) {
        Matcher matcher = codePattern.matcher(pattern);
        int last = 0;
        while(matcher.find()) {
            String text = pattern.substring(last, matcher.start());
            if(text.length() > 0) {
                parseHandler.onText(text);
            }
            parseHandler.onCode(matcher.group(1));
            last = matcher.end();
        }
        String remainingText = pattern.substring(last);
        if(remainingText.length() > 0) {
            parseHandler.onText(remainingText);
        }
    }

    public interface IParseHandler {
        void onText(String text);
        void onCode(String code);
    }
}
