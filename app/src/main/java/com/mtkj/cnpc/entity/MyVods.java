package com.mtkj.cnpc.entity;

import java.io.Serializable;

public class MyVods implements Serializable {
    /**
     * vodId : 2873097
     * displayName : 19/12/17 10:48:34
     * startTime : 1576550914176
     * endTime : 1576550930624
     * fileSize : 944120
     * meetingRoomNumber : 910030509729
     * nemoNumber : null
     * vodMetadataType : SERVER_RECORD
     */

    private int vodId;
    private String displayName;
    private long startTime;
    private long endTime;
    private int fileSize;
    private String meetingRoomNumber;
    private Object nemoNumber;
    private String vodMetadataType;

    public int getVodId() {
        return vodId;
    }

    public void setVodId(int vodId) {
        this.vodId = vodId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getMeetingRoomNumber() {
        return meetingRoomNumber;
    }

    public void setMeetingRoomNumber(String meetingRoomNumber) {
        this.meetingRoomNumber = meetingRoomNumber;
    }

    public Object getNemoNumber() {
        return nemoNumber;
    }

    public void setNemoNumber(Object nemoNumber) {
        this.nemoNumber = nemoNumber;
    }

    public String getVodMetadataType() {
        return vodMetadataType;
    }

    public void setVodMetadataType(String vodMetadataType) {
        this.vodMetadataType = vodMetadataType;
    }
}
