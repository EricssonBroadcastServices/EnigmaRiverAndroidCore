// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class ApplicationDetailProvider {
    private final String embeddedAppName;
    private final String embeddedAppVersion;
    public ApplicationDetailProvider(Context context){
        if(context != null) {
            embeddedAppName = getApplicationName(context);
            embeddedAppVersion = getApplicationVersion(context);
        } else {
            embeddedAppName = "";
            embeddedAppVersion = "";
        }
    }

    public String getEmbeddedAppName() {
        return embeddedAppName;
    }

    public String getEmbeddedAppVersion() {
        return embeddedAppVersion;
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationContext().getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        if(stringId != 0) {
            return context.getString(stringId);
        } else if(applicationInfo.nonLocalizedLabel != null) {
            return applicationInfo.nonLocalizedLabel.toString();
        } else {
            return "";
        }
    }

    public static String getApplicationVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "";
        }
    }
}
