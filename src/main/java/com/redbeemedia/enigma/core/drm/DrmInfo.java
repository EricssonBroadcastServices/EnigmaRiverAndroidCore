package com.redbeemedia.enigma.core.drm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*package-protected*/ class DrmInfo implements IDrmInfo {
    private String licenseUrl;
    private Iterable<Map.Entry<String,String>> drmKeyRequestProperties;

    public DrmInfo(String licenseUrl, String playToken) {
        this.licenseUrl = licenseUrl;
        this.drmKeyRequestProperties = createEntryIterable(createDrmKeyRequestPropertiesArray(playToken));
    }

    @Override
    public String getLicenseUrl() {
        return licenseUrl;
    }

    @Override
    public Iterable<Map.Entry<String, String>> getDrmKeyRequestProperties() {
        return drmKeyRequestProperties;
    }

    private String[] createDrmKeyRequestPropertiesArray(String playToken) {
        return new String[]{"Authorization", "Bearer " + playToken};
    }

    private static <T> Iterable<Map.Entry<T,T>> createEntryIterable(T[] array) {
        class Pair implements Map.Entry<T,T> {
            private T[] array;
            private final int index;

            private Pair(T[] array, int index) {
                this.array = array;
                this.index = index;
            }

            @Override
            public T getKey() {
                return array[index];
            }

            @Override
            public T getValue() {
                return array[index+1];
            }

            @Override
            public T setValue(T value) {
                T old = array[index];
                array[index] = value;
                return old;
            }
        }
        List<Map.Entry<T,T>> list = new ArrayList<>(array.length/2);
        for(int i = 0; i+1 < array.length; i+=2) {
            list.add(new Pair(array, i));
        }
        return list;
    }
}