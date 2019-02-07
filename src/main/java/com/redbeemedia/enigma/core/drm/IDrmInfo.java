package com.redbeemedia.enigma.core.drm;

import java.util.Map;

public interface IDrmInfo {
    String getLicenseUrl();
    Iterable<Map.Entry<String,String>> getDrmKeyRequestProperties();
}
