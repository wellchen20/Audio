package com.mtkj.cnpc.entity;

import java.io.Serializable;

public class DownLoadUrl implements Serializable {
    /**
     * downloadUrl : http://prdvoddownload.xylink.com/vodfiles/downloadfiles/201912/19_12_17_10_48_34_1576719683386.mp4?auth_key=1576721699-0-0-b980c4c101033f616f40f02069076211
     * status : 777000
     */

    private String downloadUrl;
    private int status;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
