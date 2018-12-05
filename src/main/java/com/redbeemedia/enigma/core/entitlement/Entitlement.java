package com.redbeemedia.enigma.core.entitlement;

import java.text.Format;
import java.util.List;

public class Entitlement {

    private String entitlementType;
    private String productId;
    private String playSessionId;
    private String playToken;
    private long playTokenExpiration;
    private boolean live;
    private List<Format> formats;
    private boolean airplayEnabled;
    private boolean timeshiftEnabled;
    private boolean rwEnabled;
    private boolean ffEnabled;
    private boolean lastViewedTime;
    private boolean lastViewedOffset;

    public String getEntitlementType() {
        return entitlementType;
    }

    public void setEntitlementType(String entitlementType) {
        this.entitlementType = entitlementType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPlaySessionId() {
        return playSessionId;
    }

    public void setPlaySessionId(String playSessionId) {
        this.playSessionId = playSessionId;
    }

    public String getPlayToken() {
        return playToken;
    }

    public void setPlayToken(String playToken) {
        this.playToken = playToken;
    }

    public long getPlayTokenExpiration() {
        return playTokenExpiration;
    }

    public void setPlayTokenExpiration(long playTokenExpiration) {
        this.playTokenExpiration = playTokenExpiration;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public List<Format> getFormats() {
        return formats;
    }

    public void setFormats(List<Format> formats) {
        this.formats = formats;
    }

    public boolean isAirplayEnabled() {
        return airplayEnabled;
    }

    public void setAirplayEnabled(boolean airplayEnabled) {
        this.airplayEnabled = airplayEnabled;
    }

    public boolean isTimeshiftEnabled() {
        return timeshiftEnabled;
    }

    public void setTimeshiftEnabled(boolean timeshiftEnabled) {
        this.timeshiftEnabled = timeshiftEnabled;
    }

    public boolean isRwEnabled() {
        return rwEnabled;
    }

    public void setRwEnabled(boolean rwEnabled) {
        this.rwEnabled = rwEnabled;
    }

    public boolean isFfEnabled() {
        return ffEnabled;
    }

    public void setFfEnabled(boolean ffEnabled) {
        this.ffEnabled = ffEnabled;
    }

    public boolean isLastViewedTime() {
        return lastViewedTime;
    }

    public void setLastViewedTime(boolean lastViewedTime) {
        this.lastViewedTime = lastViewedTime;
    }

    public boolean isLastViewedOffset() {
        return lastViewedOffset;
    }

    public void setLastViewedOffset(boolean lastViewedOffset) {
        this.lastViewedOffset = lastViewedOffset;
    }
}