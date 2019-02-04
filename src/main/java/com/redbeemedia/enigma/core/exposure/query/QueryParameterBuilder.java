package com.redbeemedia.enigma.core.exposure.query;

import com.redbeemedia.enigma.core.util.IStringAppendable;

public class QueryParameterBuilder {
    public static <T> IQueryParameter<T> create(IQueryParameterSet owner, Class<T> type, String name, T defaultValue) {
        return create(owner, type,name, defaultValue, true);
    }

    public static <T> IQueryParameter<T> create(IQueryParameterSet owner, Class<T> type, String name) {
        return create(owner, type,name, null, false);
    }

    private static <T> IQueryParameter<T> create(IQueryParameterSet owner, Class<T> type, String name, T defaultValue, boolean usesDefault) {
        IQueryParameter<T> queryParameter = new IQueryParameter<T>() {
            private T value = defaultValue;
            private boolean hasBeenSet = usesDefault;

            @Override
            public void setValue(T value) {
                this.value = value;
                hasBeenSet = true;
            }

            @Override
            public IStringAppendable apply(IStringAppendable urlPath) {
                if(hasBeenSet) {
                    return urlPath.append(name).append("=").append(String.valueOf(value));
                } else {
                    return urlPath;
                }
            }
        };
        owner.add(queryParameter);
        return queryParameter;
    }
}
