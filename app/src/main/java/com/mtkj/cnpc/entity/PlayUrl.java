package com.mtkj.cnpc.entity;

import java.io.Serializable;

public class PlayUrl implements Serializable {
    /**
     * playUrl : http://prd-vodcdn.xylink.com/vodfiles/sharefiles/201912/fd6d6cc1-cf40-496f-bdf2-183259e82fb1.mp4
     * innerPlayUrl : http://prd-vodcdn.xylink.com/vodfiles/sharefiles/201912/fd6d6cc1-cf40-496f-bdf2-183259e82fb1.mp4
     * shared : true
     */

    private String playUrl;
    private String innerPlayUrl;
    private boolean shared;

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getInnerPlayUrl() {
        return innerPlayUrl;
    }

    public void setInnerPlayUrl(String innerPlayUrl) {
        this.innerPlayUrl = innerPlayUrl;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }
}
