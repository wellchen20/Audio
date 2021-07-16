package com.mtkj.cnpc.entity;

import java.io.Serializable;

public class MeetingInfo implements Serializable {
    String MeettingNum;
    String MeettingName;
    String remark;

    public String getMeettingNum() {
        return MeettingNum;
    }

    public void setMeettingNum(String meettingNum) {
        MeettingNum = meettingNum;
    }

    public String getMeettingName() {
        return MeettingName;
    }

    public void setMeettingName(String meettingName) {
        MeettingName = meettingName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
