//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mtkj.cnpc.api;

import com.xylink.config.SDKConfigMgr;
import com.xylink.model.MeetingInfo;
import com.xylink.model.Pager;
import com.xylink.model.SdkCloudMeetingRoomRequest;
import com.xylink.model.SdkMeeting;
import com.xylink.model.SdkMeetingReq;
import com.xylink.util.HttpUtil;
import com.xylink.util.Result;
import java.io.IOException;
import java.net.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
/**
 *因云服务api的base64jar包无法在Android中使用
 * 重写了此方法
 * */
public class CreateMeetingsApi {
    private static SignatureSamples signatureSamples = new SignatureSamples();
    private static final String prefixUrlMeetingInfo = "/api/rest/external/v1/meetingInfo/";

    public CreateMeetingsApi() {
    }

    public SdkMeeting createMeeting(String enterpriseId, String token, SdkMeetingReq sdkMeetingReq) throws IOException {
        String url = SDKConfigMgr.getServerHost() + "/api/rest/external/v1/create_meeting?enterprise_id=" + enterpriseId;
        if (sdkMeetingReq != null && sdkMeetingReq.getMeetingName() != null) {
            url = url + "&meeting_name=" + URLEncoder.encode(sdkMeetingReq.getMeetingName(), "utf-8");
            url = url + "&start_time=" + sdkMeetingReq.getStartTime();
            url = url + "&end_time=" + (sdkMeetingReq.getStartTime() + sdkMeetingReq.getDuration());
            url = url + "&max_participant=" + sdkMeetingReq.getMaxParticipantCount();
            url = url + "&require_password=" + sdkMeetingReq.isRequirePassword();
            url = url + "&autoRecord=" + sdkMeetingReq.isAutoRecord();
            if (sdkMeetingReq.getPassword() != null && !sdkMeetingReq.getPassword().trim().isEmpty()) {
                url = url + "&password=" + sdkMeetingReq.getPassword();
            }

            if (sdkMeetingReq.getControlPassword() != null && !sdkMeetingReq.getControlPassword().trim().isEmpty()) {
                url = url + "&controlPassword=" + sdkMeetingReq.getControlPassword();
            }

            if (sdkMeetingReq.getMeetingNumber() != null && !sdkMeetingReq.getMeetingNumber().trim().isEmpty()) {
                url = url + "&meetingNumber=" + sdkMeetingReq.getMeetingNumber();
            }

            url = url + "&autoMute=" + sdkMeetingReq.getAutoMute();
            if (sdkMeetingReq.getSmartMutePerson() != 0) {
                url = url + "&smartMutePerson=" + sdkMeetingReq.getSmartMutePerson();
            }

            String signature = (new SignatureSamples()).computeSignature("", "GET", token, url);
            url = url + "&signature=" + signature;

            try {
                Result<SdkMeeting> sdkMeeting = HttpUtil.getResponse(url, "GET", "", SdkMeeting.class);
                if (sdkMeeting.isSuccess()) {
                    return (SdkMeeting)sdkMeeting.getData();
                } else {
                    throw new RuntimeException(sdkMeeting.getErrorStatus() + "");
                }
            } catch (IOException var7) {
                throw var7;
            }
        } else {
            return null;
        }
    }

    public SdkMeeting createMeetingV2(String enterpriseId, String token, SdkCloudMeetingRoomRequest sdkCloudMeetingRoomRequest) throws IOException {
        String url = SDKConfigMgr.getServerHost() + "/api/rest/external/v2/create_meeting?enterpriseId=" + enterpriseId;
        if (sdkCloudMeetingRoomRequest != null && sdkCloudMeetingRoomRequest.getMeetingName() != null) {
            String jsonEntity = (new ObjectMapper()).writeValueAsString(sdkCloudMeetingRoomRequest);
            String signature = (new SignatureSamples()).computeSignature(jsonEntity, "POST", token, url);
            url = url + "&signature=" + signature;

            try {
                Result<SdkMeeting> sdkMeeting = HttpUtil.getResponse(url, "POST", jsonEntity, SdkMeeting.class);
                if (sdkMeeting.isSuccess()) {
                    return (SdkMeeting)sdkMeeting.getData();
                } else {
                    throw new RuntimeException(sdkMeeting.getErrorStatus() + "");
                }
            } catch (IOException var8) {
                throw var8;
            }
        } else {
            return null;
        }
    }

    public Result<MeetingInfo> getMeetingInfo(String enterpriseId, String token, String meetingRoomNumber) throws IOException {
        String surl = this.getMeetingInfoPrefixUrl() + meetingRoomNumber + "?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", (String)null, MeetingInfo.class);
    }

    public Result updateMeetingInfo(String enterpriseId, String token, String meetingRoomNumber, MeetingInfo meetingInfo) throws IOException {
        String surl = this.getMeetingInfoPrefixUrl() + meetingRoomNumber + "?enterpriseId=" + enterpriseId;
        String jsonEntity = (new ObjectMapper()).writeValueAsString(meetingInfo);
        String signature = signatureSamples.computeSignature(jsonEntity, "PUT", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "PUT", jsonEntity, (Class)null);
    }

    public Result<MeetingInfo[]> getBatchMeetingInfo(String enterpriseId, String token, String[] meetingRoomNumbers) throws IOException {
        String surl = this.getMeetingInfoPrefixUrl() + "batch?enterpriseId=" + enterpriseId;
        String jsonEntity = (new ObjectMapper()).writeValueAsString(meetingRoomNumbers);
        String signature = signatureSamples.computeSignature(jsonEntity, "PUT", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "PUT", jsonEntity, MeetingInfo[].class);
    }

    public Result deleteMeetingInfo(String enterpriseId, String token, String meetingRoomNumber) throws IOException {
        String surl = this.getMeetingInfoPrefixUrl() + meetingRoomNumber + "?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "DELETE", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "DELETE", (String)null, (Class)null);
    }

    /** @deprecated */
    @Deprecated
    public Result<MeetingInfo[]> getSdkMeetingRooms(String enterpriseId, String token) throws IOException {
        String surl = this.getMeetingInfoPrefixUrl() + enterpriseId + "/meetingRoomInfo?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", (String)null, MeetingInfo[].class);
    }

    public Result<Pager<MeetingInfo>> getSdkMeetingRooms(String enterpriseId, String token, int page, int size) throws IOException {
        String surl = this.getMeetingInfoPrefixUrl() + enterpriseId + "/meetingRoomInfo/page?enterpriseId=" + enterpriseId + "&pageIndex=" + page + "&pageSize=" + size;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        Result<String> resp = HttpUtil.getResponse(surl, "GET", (String)null, String.class);
        Result<Pager<MeetingInfo>> result = new Result();
        result.setSuccess(resp.isSuccess());
        result.setErrorStatus(resp.getErrorStatus());
        result.setErrorMsg(resp.getErrorMsg());
        if (resp.isSuccess() && StringUtils.isNotBlank((CharSequence)resp.getData())) {
            ObjectMapper objectMapper = new ObjectMapper();
            Pager<MeetingInfo> pager = (Pager)objectMapper.readValue((String)resp.getData(), new TypeReference<Pager<MeetingInfo>>() {
            });
            result.setData(pager);
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        CreateMeetingsApi cma = new CreateMeetingsApi();

        try {
            Result<MeetingInfo[]> meetings = cma.getSdkMeetingRooms("ef9d546e67480c50f2cb2029c1b2c716df0a84d1", "3aa3d5b2bdde8a074c1921a9d908321ada31af88e46399d18d37c3e894cdc367");
            System.out.print(meetings);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    private String getMeetingInfoPrefixUrl() {
        return SDKConfigMgr.getServerHost() + "/api/rest/external/v1/meetingInfo/";
    }
}
