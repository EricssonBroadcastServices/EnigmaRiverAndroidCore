package com.redbeemedia.enigma.core.analytics;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class EnigmaStorageManager {
    private static final String OFFLINE_ASSET_SESSIONID_COUNTER = "OFFLINE_ASSET_SESSIONID_COUNTER";
    private final SharedPreferences sharedPreferences;

    public EnigmaStorageManager(Application application) {
        this.sharedPreferences = application.getSharedPreferences("ENIGMA_RIVER_DOWNLOAD_METADATA", Context.MODE_PRIVATE);
    }

    public EnigmaStorageManager() {
        this.sharedPreferences = null;
    }

    public void store(String contentId, byte[] data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(contentId, Base64.encodeToString(data, Base64.DEFAULT));
        if (!editor.commit()) {
            throw new RuntimeException("Failed to store metadata for contentId " + contentId);
        }
    }

    public void clear(String contentId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(contentId);
        if (!editor.commit()) {
            throw new RuntimeException("Failed to clear metadata for contentId " + contentId);
        }
    }

    public byte[] load(String contentId) {
        String metadataBase64Encoded = sharedPreferences.getString(contentId, null);
        if (metadataBase64Encoded == null) {
            return null;
        } else {
            return Base64.decode(metadataBase64Encoded, Base64.DEFAULT);
        }
    }

    // first check if there is playbacksessionId stored
    public String getOfflineEventsPlaybackSessionId(String assetId, String playSessionId) throws JSONException {
        try {
            byte[] storedAssetSessionIdCounterObject = EnigmaRiverContext.getEnigmaStorageManager().load(OFFLINE_ASSET_SESSIONID_COUNTER);
            JSONObject storedAssetSessionIdCounterMap = null;
            int playCounter = 1;
            String key = assetId + "-" + playSessionId;
            if (storedAssetSessionIdCounterObject != null) {
                String storedAssetSessionIdCounterJson = new String(storedAssetSessionIdCounterObject, StandardCharsets.UTF_8);
                try {
                    storedAssetSessionIdCounterMap = new JSONObject(storedAssetSessionIdCounterJson);
                    playCounter = storedAssetSessionIdCounterMap.optInt(key);
                    playCounter = playCounter + 1;
                    storedAssetSessionIdCounterMap.put(key, playCounter);
                } catch (JSONException e) {
                    Log.w("Offline Analytics", e.getMessage());
                }
            } else {
                storedAssetSessionIdCounterMap = new JSONObject();
                storedAssetSessionIdCounterMap.put(key, playCounter);
            }
            if (storedAssetSessionIdCounterMap != null) {
                store(OFFLINE_ASSET_SESSIONID_COUNTER, storedAssetSessionIdCounterMap.toString().getBytes(StandardCharsets.UTF_8));
            }
            return playSessionId + "_" + playCounter;
        } catch (Exception e) {
            e.printStackTrace();
            return playSessionId;
        }
    }
}
