package com.redbeemedia.enigma.core.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p><code>ISO8601Util</code> provides conversion between the ISO8601 time format to unix time
 * (milliseconds since the Unix epoch).</p>
 *
 * <p>The methods <code>ISO8601Util.newParser()</code> (iso8601->utcMillis) and
 * <code>ISO8601Util.newWriter(TimeZone timeZone)</code> (utcMillis->iso8601) provide parsers and
 * writers respectively. Neither parsers nor writers are guaranteed to be thread-safe and should
 * therefore not be shared across threads.</p>
 */
public class ISO8601Util {
    private static final IISO8601Parser PARSER = new IISO8601Parser() {
        @Override
        public long parse(String iso8601String) throws ParseException {
            return new Parsing(iso8601String).getUtcMillis();
        }
    };

    /**
     * Returns a {@link IISO8601Parser} that converts ISO8601 strings to unix time (UTC milliseconds).
     *
     * @return
     */
    public static IISO8601Parser newParser() {
        return PARSER;
    }

    /**
     * Returns a {@link IISO8601Writer} that converts unix time (UTC milliseconds) to ISO8601 strings
     * with supplied timezone.
     *
     * @param timeZone Timezone to use in generated ISO8601 string
     * @return
     */
    public static IISO8601Writer newWriter(TimeZone timeZone) {
        return new Writer(timeZone);
    }

    /**
     * Parser that converts ISO8601 strings to unix time (UTC milliseconds).
     */
    public interface IISO8601Parser {
        /**
         * Converts ISO8601 strings to unix time (UTC milliseconds).
         *
         * @param iso8601String String in ISO8601 format
         * @return milliseconds since the unix epoch (unix time)
         * @throws ParseException If the <ocde>iso8601String</ocde> was not in a correct ISO8601 format, or otherwise not parsable.
         */
        long parse(String iso8601String) throws ParseException;
    }

    /**
     * Writer that converts unix time (UTC milliseconds) to ISO8601 strings.
     */
    public interface IISO8601Writer {
        /**
         * Converts unix time (UTC milliseconds) to ISO8601 string.
         *
         * @param utcMillis milliseconds since the unix epoch (unix time)
         * @return String in ISO8601 format
         */
        String toIso8601(long utcMillis);
    }

    private static class Writer implements IISO8601Writer {
        private final TimeZone timeZone;

        public Writer(TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        @Override
        public String toIso8601(long utcMillis) {
            Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(utcMillis);
            calendar.setTimeZone(timeZone);

            StringBuilder builder = new StringBuilder();
            builder.append(pad(calendar.get(GregorianCalendar.YEAR), 4));
            builder.append("-");
            builder.append(pad(calendar.get(GregorianCalendar.MONTH)+1, 2));
            builder.append("-");
            builder.append(pad(calendar.get(GregorianCalendar.DAY_OF_MONTH), 2));

            builder.append("T");

            builder.append(pad(calendar.get(GregorianCalendar.HOUR_OF_DAY), 2));
            builder.append(":");
            builder.append(pad(calendar.get(GregorianCalendar.MINUTE), 2));
            builder.append(":");
            builder.append(pad(calendar.get(GregorianCalendar.SECOND), 2));

            int millisecond = calendar.get(GregorianCalendar.MILLISECOND);
            if(millisecond != 0) {
                builder.append(".");
                builder.append(pad(millisecond, 3));
            }

            int offsetMinutesFromUtc = timeZone.getOffset(utcMillis)/(1000*60);
            if(offsetMinutesFromUtc == 0) {
                builder.append("Z");
            } else {
                if(offsetMinutesFromUtc < 0) {
                    offsetMinutesFromUtc = -offsetMinutesFromUtc;
                    builder.append("-");
                } else {
                    builder.append("+");
                }
                builder.append(pad(offsetMinutesFromUtc/60, 2));
                builder.append(":");
                builder.append(pad(offsetMinutesFromUtc%60, 2));
            }

            return builder.toString();
        }

        private String pad(int value, int length) {
            StringBuilder padded = new StringBuilder(String.valueOf(value));
            padded.reverse();
            while(padded.length() < length) {
                padded.append("0");
            }
            padded.reverse();
            if(padded.length() > length) {
                throw new IllegalArgumentException(padded.toString()+" has length longer than "+length);
            } else {
                return padded.toString();
            }
        }
    }

    private static class Parsing {
        private static final Pattern iso8601Pattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})(T|t)(\\d{2}):(\\d{2}):(\\d{2})(|(\\.(\\d+)))((Z|z)|((\\+|\\-)(\\d{2}):(\\d{2})))");
        private static final int YEAR_GROUP = 1;
        private static final int MONTH_GROUP = 2;
        private static final int DAY_GROUP = 3;
        private static final int HOUR_GROUP = 5;
        private static final int MINUTE_GROUP = 6;
        private static final int SECONDS_GROUP = 7;
        private static final int SECOND_FRACTION_GROUP = 10;
        private static final int TIME_ZONE_GROUP = 11;
        private static final int TIME_ZONE_OFFSET_SIGN_GROUP = 14;
        private static final int TIME_ZONE_OFFSET_HOURS_GROUP = 15;
        private static final int TIME_ZONE_OFFSET_MINUTES_GROUP = 16;

        private final long utcMillis;

        public Parsing(String iso8601String) throws ParseException {

            Matcher matcher = iso8601Pattern.matcher(iso8601String);
            if(!matcher.matches()) {
                throw new ParseException("Invalid ISO8601 string: "+iso8601String, 0);
            }

            String timeZone = matcher.group(TIME_ZONE_GROUP);
            int rawOffsetMillis;
            if("z".equals(timeZone) || "Z".equals(timeZone)) {
                rawOffsetMillis = 0;
            } else  {
                int hoursOffset = Integer.parseInt(matcher.group(TIME_ZONE_OFFSET_HOURS_GROUP));
                int minutesOffset = Integer.parseInt(matcher.group(TIME_ZONE_OFFSET_MINUTES_GROUP));
                rawOffsetMillis = (hoursOffset*60+minutesOffset)*60*1000;
                String offsetSign = matcher.group(TIME_ZONE_OFFSET_SIGN_GROUP);
                if("-".equals(offsetSign)) {
                    rawOffsetMillis = -rawOffsetMillis;
                } else if(!"+".equals(offsetSign)) {
                    throw new ParseException("Expected + or - but got '"+offsetSign+"' in string: "+iso8601String, 0);
                }
            }
            Calendar calendar = GregorianCalendar.getInstance(new SimpleTimeZone(rawOffsetMillis,"util"));
            calendar.setTimeInMillis(0);

            calendar.set(GregorianCalendar.YEAR, Integer.parseInt(matcher.group(YEAR_GROUP)));
            calendar.set(GregorianCalendar.MONTH, Integer.parseInt(matcher.group(MONTH_GROUP))-1);
            calendar.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(matcher.group(DAY_GROUP)));

            calendar.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(matcher.group(HOUR_GROUP)));
            calendar.set(GregorianCalendar.MINUTE, Integer.parseInt(matcher.group(MINUTE_GROUP)));
            calendar.set(GregorianCalendar.SECOND, Integer.parseInt(matcher.group(SECONDS_GROUP)));

            String secondFraction = matcher.group(SECOND_FRACTION_GROUP);
            if(secondFraction != null) {
                //".1" -> 1 --> 1000/10 = 100 millis = 0.1 OK!
                //".10" -> 10 --> 10000/100 = 100 millis = 0.1 OK!
                //".01" -> 01 --> 1000/100 = 10 millis = 0.01 OK!
                //etc
                int fractionLength = secondFraction.length();
                int millis = Integer.parseInt(secondFraction)*1000;
                for(int i = 0; i < fractionLength; ++i) {
                    millis = millis/10;
                }
                calendar.set(GregorianCalendar.MILLISECOND, millis);
            }

            utcMillis = calendar.getTimeInMillis();
        }

        public long getUtcMillis() {
            return utcMillis;
        }
    }
}
