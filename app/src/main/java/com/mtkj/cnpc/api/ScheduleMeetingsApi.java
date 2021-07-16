//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mtkj.cnpc.api;

import android.util.Log;

import com.xylink.config.SDKConfigMgr;
import com.xylink.model.Pager;
import com.xylink.model.ReminderMeeting;
import com.xylink.util.HttpUtil;
import com.xylink.util.Result;

import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class ScheduleMeetingsApi {
    private static SignatureSamples signatureSamples = new SignatureSamples();
    private static final String prefixUrl = "/api/rest/external/v1/meetingreminders";

    public ScheduleMeetingsApi() {
    }

    public Result remindMeeting(String enterpriseId, String token, ReminderMeeting reminderMeeting, int maxParticipantCount) throws IOException {
        String surl = this.getPrefixUrl() + "?enterpriseId=" + enterpriseId + "&maxParticipant=" + maxParticipantCount;
        String jsonEntity = (new ObjectMapper()).writeValueAsString(reminderMeeting);
        String signature = signatureSamples.computeSignature(jsonEntity, "POST", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "POST", jsonEntity, Map.class);
    }

    public Result remindMeeting(String enterpriseId, String token, ReminderMeeting reminderMeeting) throws IOException {
        String surl = this.getPrefixUrl() + "?enterpriseId=" + enterpriseId;
        String jsonEntity = (new ObjectMapper()).writeValueAsString(reminderMeeting);
        String signature = signatureSamples.computeSignature(jsonEntity, "POST", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "POST", jsonEntity, Map.class);
    }

    public Result updateMeeting(String enterpriseId, String token, String meetingId, ReminderMeeting reminderMeeting, int maxParticipantCount) throws IOException {
        String surl = this.getPrefixUrl() + "/" + meetingId + "?enterpriseId=" + enterpriseId + "&maxParticipant=" + maxParticipantCount;
        String jsonEntity = (new ObjectMapper()).writeValueAsString(reminderMeeting);
        String signature = signatureSamples.computeSignature(jsonEntity, "PUT", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "PUT", jsonEntity, (Class)null);
    }

    public Result updateMeeting(String enterpriseId, String token, String meetingId, ReminderMeeting reminderMeeting) throws IOException {
        String surl = this.getPrefixUrl() + "/" + meetingId + "?enterpriseId=" + enterpriseId;
        String jsonEntity = (new ObjectMapper()).writeValueAsString(reminderMeeting);
        String signature = signatureSamples.computeSignature(jsonEntity, "PUT", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "PUT", jsonEntity, (Class)null);
    }

    public Result deleteMeeting(String enterpriseId, String token, String meetingId) throws IOException {
        String surl = this.getPrefixUrl() + "/" + meetingId + "?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "DELETE", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "DELETE", (String)null, (Class)null);
    }

    public Result<ReminderMeeting> getMeeting(String enterpriseId, String token, String meetingId) throws IOException {
        String surl = this.getPrefixUrl() + "/" + meetingId + "?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", (String)null, ReminderMeeting.class);
    }

    public Result<ReminderMeeting[]> getAllMeeting(String enterpriseId, String token, long endTime) throws IOException {
        String surl = this.getPrefixUrl() + "?enterpriseId=" + enterpriseId + "&endTime=" + endTime;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", (String)null, ReminderMeeting[].class);
    }

    public Result<Pager<ReminderMeeting>> getMeeting(String enterpriseId, String token, int page, int size, long endTime) throws IOException {
        String surl = this.getPrefixUrl() + "/page?enterpriseId=" + enterpriseId + "&pageIndex=" + page + "&pageSize=" + size + "&endTime=" + endTime;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        Result<String> resp = HttpUtil.getResponse(surl, "GET", (String)null, String.class);
        Result<Pager<ReminderMeeting>> result = new Result();
        result.setSuccess(resp.isSuccess());
        result.setErrorStatus(resp.getErrorStatus());
        result.setErrorMsg(resp.getErrorMsg());
        if (resp.isSuccess() && StringUtils.isNotBlank((CharSequence)resp.getData())) {
            ObjectMapper objectMapper = new ObjectMapper();
            Pager<ReminderMeeting> pager = (Pager)objectMapper.readValue((String)resp.getData(), new TypeReference<Pager<ReminderMeeting>>() {
            });
            result.setData(pager);
        }

        return result;
    }

    public Result<Pager> getMeetingByConfrenceNumber(String enterpriseId, String token, Integer pageIndex, Integer pageSize, String meetingRoomNumber) throws IOException {
        String surl = this.getPrefixUrl() + "/conference?enterpriseId=" + enterpriseId + "&pageIndex=" + pageIndex + "&pageSize=" + pageSize + "&meetingRoomNumber=" + meetingRoomNumber;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        Log.e("surl", surl);
        return HttpUtil.getResponse(surl, "GET", (String)null, Pager.class);
    }

    private ReminderMeeting convert2ReminderMeeting(Map<String, Object> map) {
        ReminderMeeting reminderMeeting = new ReminderMeeting();
        reminderMeeting.setId((String)map.get("id"));
        return reminderMeeting;
    }

    private String getPrefixUrl() {
        return SDKConfigMgr.getServerHost() + "/api/rest/external/v1/meetingreminders";
    }

    public Result<String> getInfoByConfrenceNumber(String enterpriseId,String token,String meetingRoomNumber) throws IOException{
        String surl = SDKConfigMgr.getServerHost() + "/api/rest/external/v1/conferenceControl/currentMeeting/"+meetingRoomNumber+"/detail?enterpriseId="+enterpriseId+"&needQuality=false&pageIndex=0&pageSize=20";
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        Log.e("surl", surl);
        Result<String> resp = HttpUtil.getResponse(surl, "GET", (String)null, String.class);
        return resp;
    }

    public Result<String> getstatusByConfrenceNumber(String enterpriseId,String token,String meetingRoomNumber) throws IOException{
        String surl = SDKConfigMgr.getServerHost() + "/api/rest/external/v1/meetingInfo/"+meetingRoomNumber+"?enterpriseId="+enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        Log.e("surl", surl);
        Result<String> resp = HttpUtil.getResponse(surl, "GET", (String)null, String.class);
        return resp;
    }

    public static void main(String[] args) throws Exception {
        ScheduleMeetingsApi scheduleMeetingApi = new ScheduleMeetingsApi();
        ReminderMeeting reminderMeeting = new ReminderMeeting();
        reminderMeeting.setTitle("test-yihui1-1");
        reminderMeeting.setAutoRecord(1);
        reminderMeeting.setStartTime(System.currentTimeMillis() + 60000L);
        reminderMeeting.setEndTime(System.currentTimeMillis() + 300000L);
        reminderMeeting.setMeetingRoomType(1);
        Result result = scheduleMeetingApi.updateMeeting("ccba01147577386a1960462392eaeecf7bbe4b1c", "62e902a6a3c2427d60d52c166ae057260a167fb3c74d1eda86d5f0e0967a579c", "ff8080815dfb1e16015e377057df3173", reminderMeeting, -1);
        System.out.println(result);
        Result<ReminderMeeting[]> list = scheduleMeetingApi.getAllMeeting("ccba01147577386a1960462392eaeecf7bbe4b1c", "62e902a6a3c2427d60d52c166ae057260a167fb3c74d1eda86d5f0e0967a579c", 0L);
        System.out.println(list);
    }
}
