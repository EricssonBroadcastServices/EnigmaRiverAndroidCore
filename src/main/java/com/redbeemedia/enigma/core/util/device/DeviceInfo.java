package com.redbeemedia.enigma.core.util.device;

/*
 * Copyright (c) 2018 Ericsson. All Rights Reserved
 *
 * This SOURCE CODE FILE, which has been provided by Ericsson as part
 * of an Ericsson software product for use ONLY by licensed users of the
 * product, includes CONFIDENTIAL and PROPRIETARY information of Ericsson.
 *
 * USE OF THIS SOFTWARE IS GOVERNED BY THE TERMS AND CONDITIONS OF
 * THE LICENSE STATEMENT AND LIMITED WARRANTY FURNISHED WITH
 * THE PRODUCT.
 */

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class DeviceInfo implements IDeviceInfo {
    private static final String TAG = "DeviceInfo";

    private static final double TABLET_SIZE_TRESHOLD = 7;
    private static final String FALLBACK_ID = "AndroidId";

    private final String deviceId;
    private final DisplayMetrics displayMetrics;
    private boolean isTv;

    public DeviceInfo(Application application) {
        this.deviceId = getDeviceId(application);
        this.displayMetrics = getDisplayMetrics(application);
        this.isTv = detectIfTV(application);
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    private static String getDeviceId(Context applicationContext) {
        try {
            String android_id = Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (android_id == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    android_id = Build.SERIAL;
                }
            }
            if (android_id == null) {
                android_id = FALLBACK_ID;
            }
            return android_id;
        }
        catch (Exception e) {
            Log.e(TAG, "Error getting device id: " + e.toString());
            return FALLBACK_ID;
        }
    }

    @Override
    public String getModel() {
        return Build.MODEL;
    }

    @Override
    public String getOS() {
        return "Android";
    }

    @Override
    public String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    @Override
    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    @Override
    public boolean isDeviceRooted() {
        return CheckRootUtil.isDeviceRooted();
    }

    @Override
    @SuppressLint("WrongConstant")
    public String getWidevineDrmSecurityLevel() {
        UUID widevineUUID = new UUID(-1301668207276963122L, -6645017420763422227L);
        try {
            MediaDrm mediaDrm = new MediaDrm(widevineUUID);
            try {
                return mediaDrm.getPropertyString("securityLevel");
            } catch (RuntimeException e) {
                return "Error_"+e.getClass().getSimpleName();
            } finally {
                if(Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT <= 27) {
                    mediaDrm.release();
                } else if(Build.VERSION.SDK_INT >= 28) {
                    mediaDrm.close();
                }
            }
        } catch (UnsupportedSchemeException e) {
            return "Widevine N/A";
        }
    }

    private static boolean diagonalLargerThanSize(double width, double height, double diagonalTreshold) {
        Log.v(TAG, String.format("Width %f\" Height %f\" Diagonal Treshold %f\"",width, height, diagonalTreshold));
        return width*width + height*height > diagonalTreshold*diagonalTreshold;
    }

    private static DisplayMetrics getDisplayMetrics(Application application) {
        return application.getResources().getDisplayMetrics();
    }

    private boolean detectIfTV(Application application) {
        try {
            UiModeManager uiModeManager = (UiModeManager)application.getApplicationContext().getSystemService(Application.UI_MODE_SERVICE);
            return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public static JSONObject getDeviceInfoJson(IDeviceInfo deviceInfo) throws JSONException {
        JSONObject deviceJSON = new JSONObject();
        deviceJSON.put("height", deviceInfo.getHeightPixels())
                .put("width", deviceInfo.getWidthPixels())
                .put("model", deviceInfo.getModel())
                .put("name", deviceInfo.getName())
                .put("os", deviceInfo.getOS())
                .put("osVersion", deviceInfo.getOSVersion())
                .put("manufacturer", deviceInfo.getManufacturer())
                .put("deviceId", deviceInfo.getDeviceId())
                .put("type", deviceInfo.getType());

        return deviceJSON;
    }

    @Override
    public int getHeightPixels() {
        return displayMetrics.heightPixels;
    }

    @Override
    public int getWidthPixels() {
        return displayMetrics.widthPixels;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getType() {
        if (isTv) { return "SMART_TV"; }
        boolean isTablet = diagonalLargerThanSize(displayMetrics.widthPixels/displayMetrics.xdpi,
                displayMetrics.heightPixels/displayMetrics.ydpi, TABLET_SIZE_TRESHOLD);
        return isTablet ? "TABLET" : "MOBILE";
    }
}
