package com.mtkj.cnpc.bean;

import java.io.Serializable;
import java.util.List;

public class MeetingData implements Serializable {
    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * meettingName : 陈威的会议室
         * meettingNum : 9005687862
         * reMark :
         * time : 1615359205
         */

        private String meettingName;
        private String meettingNum;
        private String reMark;
        private long time;

        public String getMeettingName() {
            return meettingName;
        }

        public void setMeettingName(String meettingName) {
            this.meettingName = meettingName;
        }

        public String getMeettingNum() {
            return meettingNum;
        }

        public void setMeettingNum(String meettingNum) {
            this.meettingNum = meettingNum;
        }

        public String getReMark() {
            return reMark;
        }

        public void setReMark(String reMark) {
            this.reMark = reMark;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }
}
