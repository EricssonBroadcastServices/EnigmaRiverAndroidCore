package com.redbeemedia.enigma.core.time;

import com.redbeemedia.enigma.core.util.SimpleParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>{@code SimpleDurationFormat} provides a simple way to define a {@link IDurationFormat}.</p>
 * <p></p>A {@code String} pattern is supplied in the constructor.
 * When applied to a {@code Duration}, "tags" in the pattern are replaced with data from
 * the {@code Duration}.</p>
 * <p>
 *  The following tags are currently supported:
 *  <ul>
 *      <li>${hou} - Hours within day (00-23) (length 2)</li>
 *      <li>${hour_in_day} = Hours within day (0-23) (length 1 or 2)</li>
 *      <li>${min} - Minutes within hour (00-59) (length 2)</li>
 *      <li>${sec} - Seconds within minute (00-59) (length 2)</li>
 *      <li>${millis} = Milliseconds within second (000-999) (length 3)</li>
 *      <li>${seconds} = The total number of whole seconds of the {@link Duration}</li>
 *      <li>${minutes} - The total number of whole minutes of the {@link Duration}</li>
 *      <li>${hours} = The total number of whole hours of the {@link Duration}</li>
 *      <li>${days} = The total number of whole days of the {@link Duration}</li>
 *  </ul>
 * </p>
 * <h3>Examples:</h3>
 * <p>
 *   {@code pattern "${minutes}:${sec}"}
 *   <div style="margin-left: 10px;">
 *      <div>5 seconds -> {@code "0:05"}</div>
 *      <div>20.5 seconds -> {@code "0:20"}</div>
 *      <div>60 seconds -> {@code "1:00"}</div>
 *      <div>120 minutes-> {@code "120:00"}</div>
 *   </div>
 *   {@code pattern "${hours}:${min}:${sec}"}
 *   <div style="margin-left: 10px;">
 *      <div>5 seconds -> {@code "0:00:05"}</div>
 *      <div>20.5 seconds -> {@code "0:00:20"}</div>
 *      <div>60 seconds -> {@code "0:01:00"}</div>
 *      <div>120 minutes-> {@code "2:00:00"}</div>
 *   </div>
 *   {@code pattern "${hours}h ${min}m ${sec}.${millis}s"}
 *   <div style="margin-left: 10px;">
 *      <div>5 seconds -> {@code "0h 00m 05.000s"}</div>
 *      <div>20.5 seconds -> {@code "0h 00m 20.500s"}</div>
 *      <div>60 seconds -> {@code "0h 01m 00.000s"}</div>
 *      <div>120 minutes-> {@code "2h 00m 00.000s"}</div>
 *   </div>
 *   {@code pattern "Length is ${seconds} seconds"}
 *   <div style="margin-left: 10px;">
 *      <div>5 seconds -> {@code "Length is 5 seconds"}</div>
 *      <div>20.5 seconds -> {@code "Length is 20 seconds"}</div>
 *      <div>60 seconds -> {@code "Length is 60 seconds"}</div>
 *      <div>120 minutes-> {@code "Length is 7200 seconds"}</div>
 *   </div>
 *   {@code pattern "Starting in ${days} day(s) and ${hour_in_day} hours!"}
 *   <div style="margin-left: 10px;">
 *      <div>24 hours -> {@code "Starting in 1 day(s) and 0 hours!"}</div>
 *      <div>22 hours -> {@code "Starting in 0 day(s) and 23 hours!"}</div>
 *      <div>40 hours -> {@code "Starting in 1 day(s) and 16 hours!"}</div>
 *      <div>50 hours -> {@code "Starting in 2 day(s) and 2 hours!"}</div>
 *   </div>
 * </p>
 *
 * @see Duration
 */
public class SimpleDurationFormat implements IDurationFormat {
    private static final Map<String, IDurationFormatToken> SUPPORTED_VARS;
    static {
        Map<String,IDurationFormatToken> supportedVars = new HashMap<>();
        supportedVars.put("minutes", new WholeUnits(Duration.Unit.MINUTES));
        supportedVars.put("min", new PartialWholeUnits(Duration.Unit.MINUTES, 60L));
        supportedVars.put("seconds", new WholeUnits(Duration.Unit.SECONDS));
        supportedVars.put("sec", new PartialWholeUnits(Duration.Unit.SECONDS, 60L));
        supportedVars.put("millis", new PartialWholeUnits(Duration.Unit.MILLISECONDS, 1000L));
        supportedVars.put("hours", new WholeUnits(Duration.Unit.HOURS));
        supportedVars.put("hou", new PartialWholeUnits(Duration.Unit.HOURS, 24L));
        supportedVars.put("hour_in_day", new PartialWholeUnits(Duration.Unit.HOURS, 24L, false));
        supportedVars.put("days", new WholeUnits(Duration.Unit.DAYS));
        SUPPORTED_VARS = Collections.unmodifiableMap(supportedVars);
    }

    private List<IDurationFormatToken> durationFormatTokens;

    /**
     * @param pattern The pattern to use. See {@link SimpleDurationFormat}.
     */
    public SimpleDurationFormat(String pattern) {
        this.durationFormatTokens = parsePattern(pattern);
    }

    @Override
    public String format(Duration duration) {
        StringBuilder stringBuilder = new StringBuilder();
        for(IDurationFormatToken durationFormatToken : durationFormatTokens) {
            durationFormatToken.append(duration, stringBuilder);
        }
        return stringBuilder.toString();
    }

    private static List<IDurationFormatToken> parsePattern(String pattern) {
        List<IDurationFormatToken> tokens = new ArrayList<>();

        SimpleParser.parse(pattern, new SimpleParser.IParseHandler() {
            @Override
            public void onText(String text) {
                tokens.add(new TextDurationFormatToken(text));
            }

            @Override
            public void onCode(String code) {
                IDurationFormatToken durationFormatToken = SUPPORTED_VARS.get(code);
                if(durationFormatToken != null) {
                    tokens.add(durationFormatToken);
                } else {
                    onText("${"+code+"}");
                }
            }
        });

        return tokens;
    }

    private interface IDurationFormatToken {
        void append(Duration duration, StringBuilder stringBuilder);
    }

    private static class WholeUnits implements IDurationFormatToken {
        private final Duration.Unit unit;

        public WholeUnits(Duration.Unit unit) {
            this.unit = unit;
        }

        @Override
        public void append(Duration duration, StringBuilder stringBuilder) {
            stringBuilder.append(duration.inWholeUnits(unit));
        }
    }

    private static class PartialWholeUnits implements IDurationFormatToken {
        private final Duration.Unit unit;
        private final long mod;
        private final int padding;

        public PartialWholeUnits(Duration.Unit unit, long mod) {
            this(unit, mod, true);
        }

        public PartialWholeUnits(Duration.Unit unit, long mod, boolean usePadding) {
            this.unit = unit;
            this.mod = mod;
            this.padding = usePadding ? String.valueOf(mod-1).length() : 0;
        }

        @Override
        public void append(Duration duration, StringBuilder stringBuilder) {
            String value = String.valueOf(duration.inWholeUnits(unit)%mod);

            //Pad with 0's
            for(int i = 0; i < padding-value.length(); ++i) {
                stringBuilder.append("0");
            }
            stringBuilder.append(value);
        }
    }

    private static class TextDurationFormatToken implements IDurationFormatToken {
        private String text;

        public TextDurationFormatToken(String text) {
            this.text = text;
        }

        @Override
        public void append(Duration duration, StringBuilder stringBuilder) {
            stringBuilder.append(text);
        }
    }
}
