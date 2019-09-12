package com.redbeemedia.enigma.core.time;

public final class Duration {
    private static final IDurationFormat DEFAULT_FORMAT = new SimpleDurationFormat("${hours}h ${min}m ${sec}s ${millis}ms");

    private final long milliseconds;

    private Duration(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public float inUnits(Unit unit) {
        return (float)(((double) milliseconds)/unit.millisPerUnit);
    }

    public long inWholeUnits(Unit unit) {
        return milliseconds/unit.millisPerUnit;
    }

    public Duration add(Duration other) {
        return new Duration(this.milliseconds+other.milliseconds);
    }

    public Duration subtract(Duration other) {
        return new Duration(this.milliseconds-other.milliseconds);
    }

    public Duration multiply(float factor) {
        return new Duration((long) (this.milliseconds*factor));
    }

    public static Duration millis(long milliseconds) {
        return new Duration(milliseconds);
    }

    public static Duration of(long amount, Unit unit) {
        return millis(unit.millisPerUnit*amount);
    }

    public static Duration seconds(long seconds) {
        return of(seconds, Unit.SECONDS);
    }

    public static Duration minutes(long minutes) {
        return of(minutes, Unit.MINUTES);
    }

    public static Duration hours(long hours) {
        return of(hours, Unit.HOURS);
    }

    public static Duration days(long days) {
        return of(days, Unit.DAYS);
    }

    public enum Unit {
        MILLISECONDS(1L), SECONDS(1000L), MINUTES(1000L*60L), HOURS(1000L*60L*60L), DAYS(1000L*60L*60L*24L);

        private long millisPerUnit;

        Unit(long millisPerUnit) {
            this.millisPerUnit = millisPerUnit;
        }
    }

    @Override
    public int hashCode() {
        return Long.valueOf(milliseconds).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Duration && ((Duration) obj).milliseconds == this.milliseconds;
    }

    @Override
    public String toString() {
        return DEFAULT_FORMAT.format(this);
    }
}
