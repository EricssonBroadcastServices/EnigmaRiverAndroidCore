package com.redbeemedia.enigma.core.player.timeline;

import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.IDurationFormat;
import com.redbeemedia.enigma.core.time.SimpleDurationFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimelinePositionFormat {
    private IDurationFormat durationFormat;
    private DateFormat dateFormat;

    private TimelinePositionFormat(IDurationFormat durationFormat, DateFormat dateFormat) {
        this.durationFormat = durationFormat;
        this.dateFormat = dateFormat;
    }

    public String formatDuration(Duration duration) {
        return durationFormat.format(duration);
    }

    public String formatDate(Date date) {
        if(dateFormat instanceof SimpleDateFormat) {
            synchronized (dateFormat) {
                return dateFormat.format(date);
            }
        } else {
            return dateFormat.format(date);
        }
    }

    public static TimelinePositionFormat newFormat(IDurationFormat durationFormat, DateFormat dateFormat) {
        return new TimelinePositionFormat(durationFormat, dateFormat);
    }

    public static TimelinePositionFormat newFormat(String durationPattern, DateFormat dateFormat) {
        return newFormat(new SimpleDurationFormat(durationPattern), dateFormat);
    }

    public static TimelinePositionFormat newFormat(String durationPattern, String datePattern) {
        return newFormat(durationPattern, new SimpleDateFormat(datePattern));
    }

    public static TimelinePositionFormat newFormat(IDurationFormat durationFormat, String datePattern) {
        return newFormat(durationFormat, new SimpleDateFormat(datePattern));
    }
}
