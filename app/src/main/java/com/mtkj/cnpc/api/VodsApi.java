//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mtkj.cnpc.api;

import android.util.Log;

import com.xylink.config.SDKConfigMgr;
import com.xylink.model.VodInfo;
import com.xylink.util.HttpUtil;
import com.xylink.util.Result;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
/**
 *因云服务api的base64jar包无法在Android中使用
 * 重写了此方法
 * */
public class VodsApi {
    private static SignatureSamples signatureSamples = new SignatureSamples();
    private static final String prefixUrl = "/api/rest/external/v1/";

    public VodsApi() {
    }

    public Result<VodInfo[]> getVods(String enterpriseId, String token, long startTime, long endTime) throws IOException {
        String surl = this.getPrefixUrl() + "vods?enterpriseId=" + enterpriseId + "&startTime=" + startTime + "&endTime=" + endTime;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", (String)null, VodInfo[].class);
    }

    public Result<VodInfo[]> getNemoVods(String enterpriseId, String token, String nemoNumber, long startTime, long endTime) throws IOException {
        String surl = this.getPrefixUrl() + "nemo/" + nemoNumber + "/vods?enterpriseId=" + enterpriseId + "&startTime=" + startTime + "&endTime=" + endTime;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", (String)null, VodInfo[].class);
    }

    public Result<VodInfo[]> getMeetingRoomVods(String enterpriseId, String token, String meetingRoomNumber, Long startTime, Long endTime) throws IOException {
        String surl = this.getPrefixUrl() + "meetingroom/" + meetingRoomNumber + "/vods?enterpriseId=" + enterpriseId;
        if (startTime != null) {
            surl = surl + "&startTime=" + startTime;
        }

        if (endTime != null) {
            surl = surl + "&endTime=" + endTime;
        }

        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        System.out.println(surl);
        System.out.println("------------------");
        Log.e("surl", "surl: "+surl );
        return HttpUtil.getResponse(surl, "GET", (String)null, VodInfo[].class);
    }
//<!--  自定义接口  开始-->

    //根据会议室号获取录像列表
    public String getMeetingRoomVodsUrl(String enterpriseId, String token, String meetingRoomNumber, Long startTime, Long endTime){
        String surl = this.getPrefixUrl() + "meetingroom/" + meetingRoomNumber + "/vods?enterpriseId=" + enterpriseId;
        if (startTime != null) {
            surl = surl + "&startTime=" + startTime;
        }

        if (endTime != null) {
            surl = surl + "&endTime=" + endTime;
        }

        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return surl;
    }

    //根据录像vodid获取下载地址
    public String getDownloadUrlByVodId(String enterpriseId, String token, String vodId){
        String surl = this.getPrefixUrl() + "vods/" + vodId + "/getdownloadurl?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return surl;
    }

    //根据录像vodid获取播放链接
    public String getPlayVodByVodId(String enterpriseId, String token, String vodId){
        String surl = this.getPrefixUrl() + "vods/" + vodId + "/sharedInfo?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return surl;
    }
    //根据录像vodid删除录像
    public String deleteVideoById(String enterpriseId, String token, String vodId) {
        String surl = this.getPrefixUrl() + "vods/" + vodId + "?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "DELETE", token, surl);
        surl = surl + "&signature=" + signature;
        Log.e("DELETE", "DELETE: "+surl );
        return surl;
    }
    //<!--  自定义接口  结束-->

    public Result getVodThumbnail(String enterpriseId, String token, String vodId) throws IOException {
        String surl = this.getPrefixUrl() + "vods/" + vodId + "/thumbnail?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getByteStreamResponse(surl, "GET", (String)null);
    }

    public void convertByteArrayToImageOrVideo(byte[] bytes, String path) {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(path);
            fos.write(bytes);
        } catch (FileNotFoundException var5) {
            System.out.println("File Path not found!");
        } catch (IOException var6) {
            System.out.println("Convert byte array to image,IO Errot!");
        }

    }

    public Result videoDownload(String enterpriseId, String token, String vodId) throws IOException {
        String surl = this.getPrefixUrl() + "vods/" + vodId + "/download?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getByteStreamResponse(surl, "GET", (String)null);
    }

    public Result deleteVideo(String enterpriseId, String token, String vodId) throws IOException {
        String surl = this.getPrefixUrl() + "vods/" + vodId + "?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "DELETE", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "DELETE", (String)null, (Class)null);
    }

    public Result deleteMeetingRoomVods(String enterpriseId, String token, String meetingRoomNumber) throws IOException {
        String surl = this.getPrefixUrl() + "meetingroom/" + meetingRoomNumber + "/vods?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "DELETE", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "DELETE", (String)null, (Class)null);
    }

    public Result getDownloadurl(String enterpriseId, String token, String vodId) throws IOException {
        String surl = this.getPrefixUrl() + "vods/" + vodId + "/getdownloadurl?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", (String)null, Map.class);
    }

    public Result getDownloadurlBySessionId(String enterpriseId, String token, String sessionId) throws IOException {
        String surl = this.getPrefixUrl() + "vods/session/" + sessionId + "/downloadurl?enterpriseId=" + enterpriseId;
        String signature = signatureSamples.computeSignature("", "GET", token, surl);
        surl = surl + "&signature=" + signature;
        return HttpUtil.getResponse(surl, "GET", (String)null, Map.class);
    }

    private String getPrefixUrl() {
        return SDKConfigMgr.getServerHost() + "/api/rest/external/v1/";
    }
}
