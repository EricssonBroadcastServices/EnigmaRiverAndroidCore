package com.redbeemedia.enigma.core.util.section;

public interface ISectionList<T> {
    /**
     * <p>If {@code value} is greater than or equal to {@code getFirstStart()} and less than {@code getLastEnd()}, then this method must return a non-null result.</p>
     *
     * <p>In the case where {@code isEmpty()} is {@code true}, there are no sections. {@code getFirstStart() == getLastEnd()} so this method will return {@code null}.</p>
     *
     * @param value
     * @return section at {@code value} if within bounds.
     */
    ISection<T> getSectionAt(long value);
    T getItemAt(long value);
    long getFirstStart();
    long getLastEnd();
    boolean isEmpty();
}
