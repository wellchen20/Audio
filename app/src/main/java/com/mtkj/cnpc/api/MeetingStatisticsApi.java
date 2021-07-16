//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mtkj.cnpc.api;

import android.util.Log;

import com.xylink.config.SDKConfigMgr;
import com.xylink.model.ListData;
import com.xylink.model.Pager;
import com.xylink.model.statis.SdkMeetingExportDto;
import com.xylink.model.statis.SdkParticipantExportDto;
import com.xylink.util.HttpUtil;
import com.xylink.util.Result;

import java.io.IOException;

public class MeetingStatisticsApi {
    private static SignatureSamples signatureSamples = new SignatureSamples();
    private static final String prefixUrl = "/api/rest/external/v1/meeting/statistic/";
    private static final String prefixUrl_1 = "/api/rest/external/v1/";

    public MeetingStatisticsApi() {
    }

    public Result<ListData<SdkMeetingExportDto>> getByTime(String enterpriseId, String token, long timeBegin, long timeEnd) throws IOException {
        String surl = this.getPrefixUrl() + "enterprise?enterpriseId=" + enterpriseId + "&timeBegin=" + timeBegin + "&timeEnd=" + timeEnd;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", "", ListData.class);
    }

    public Result<ListData<SdkParticipantExportDto>> getByParticipant(String enterpriseId, String token, long timeBegin, long timeEnd) throws IOException {
        String surl = this.getPrefixUrl() + "participant?enterpriseId=" + enterpriseId + "&timeBegin=" + timeBegin + "&timeEnd=" + timeEnd;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", "", ListData.class);
    }

    public Result<Pager> getByNemoNumber(String enterpriseId, String token, Long timeBegin, Long timeEnd, String nemoNumber) throws IOException {
        String surl = this.getPrefixUrl1() + "meeting/list?enterpriseId=" + enterpriseId + "&timeBegin=" + timeBegin + "&timeEnd=" + timeEnd + "&nemoNumber=" + nemoNumber;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        System.out.println(surl);
        System.out.println("------------------");
        Log.e("surl", "surl: "+surl );
        return HttpUtil.getResponse(surl, "GET", "", Pager.class);
    }

    public Result<Pager> getByMeetingId(String enterpriseId, String token, String meetingId) throws IOException {
        String surl = this.getPrefixUrl() + "participant/detail?enterpriseId=" + enterpriseId + "&meetingId=" + meetingId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", "", Pager.class);
    }

    private String getPrefixUrl() {
        return SDKConfigMgr.getServerHost() + "/api/rest/external/v1/meeting/statistic/";
    }

    private String getPrefixUrl1() {
        return SDKConfigMgr.getServerHost() + "/api/rest/external/v1/";
    }
}
