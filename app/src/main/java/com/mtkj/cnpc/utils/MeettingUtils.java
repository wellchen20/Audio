package com.mtkj.cnpc.utils;

import android.util.Log;

import com.mtkj.cnpc.api.CreateMeetingsApi;
import com.mtkj.cnpc.api.MeetingStatisticsApi;
import com.mtkj.cnpc.config.AppConfigSp;
import com.xylink.model.MeetingInfo;
import com.xylink.model.Pager;
import com.xylink.util.Result;

import java.io.IOException;

public class MeettingUtils {

    public static void getMeetingInfo(String meetingNumber) throws IOException {
        Result<MeetingInfo> infoResult = getInfo(meetingNumber);
        Log.e("getMeetingInfo", "getMeetingInfo: "+infoResult );
    }



    public static Result<MeetingInfo> getInfo( String meetingNumber) throws IOException {
        CreateMeetingsApi createMeetingApi = new CreateMeetingsApi();
        return createMeetingApi.getMeetingInfo(AppConfigSp.XY_PRD_EXT_ID, AppConfigSp.XY_TOKEN,meetingNumber);
    }

    //按照结束时间获取会议
    public static Result<Pager> getAllMeeting(String myNEmo) throws IOException {//账号
        MeetingStatisticsApi msa = new MeetingStatisticsApi();
        long endtime = System.currentTimeMillis();
        return msa.getByNemoNumber(AppConfigSp.XY_PRD_EXT_ID, AppConfigSp.XY_TOKEN, 0l,endtime,myNEmo);
//        return msa.getByTime(AppConfigSp.XY_PRD_EXT_ID, AppConfigSp.XY_TOKEN, 0l,endtime);
    }
}
