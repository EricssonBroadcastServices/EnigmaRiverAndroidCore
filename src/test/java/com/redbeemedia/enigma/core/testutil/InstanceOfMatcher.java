package com.redbeemedia.enigma.core.testutil;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class InstanceOfMatcher<T> extends BaseMatcher<Object> {
    private Class<T> type;

    public InstanceOfMatcher(Class<T> type) {
        this.type = type;
    }

    @Override
    public final boolean matches(Object item) {
        return type.isInstance(item) && matchesSpecific(type.cast(item));
    }

    public boolean matchesSpecific(T item) {
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("instance of ");
        description.appendValue(type.getName());
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if(item == null) {
            description.appendText("was null");
        } else {
            if(!type.isInstance(item)) {
                description.appendText("had type ").appendValue(item.getClass().getName());
            } else if(!matchesSpecific(type.cast(item))) {
                describeSpecificMismatch(type.cast(item), description);
            }
        }
    }

    public void describeSpecificMismatch(T item, Description description) {
    }
}
