//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mtkj.cnpc.api;

import com.xylink.config.SDKConfigMgr;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
/**
 *因云服务api的base64jar包无法在Android中使用
 * 重写了此方法
 * */
public class SignatureSamples {
    private String requestUriPrefix = "/api/rest/external/v1/";

    public SignatureSamples() {
    }

    protected String computeStringToSign(String requestPath, Map<String, String> reqParams, String reqJsonEntity, String reqMethod) throws Exception {
        String prefix = SDKConfigMgr.getServerHost() + this.requestUriPrefix;
        StringBuffer strToSign = new StringBuffer(reqMethod);
        strToSign.append("\n");
        strToSign.append(requestPath.substring(prefix.length()));
        strToSign.append("\n");
        List<String> params = new ArrayList(reqParams.keySet());
        Collections.sort(params);
        Iterator var8 = params.iterator();

        String ret;
        while(var8.hasNext()) {
            ret = (String)var8.next();
            strToSign.append(ret);
            strToSign.append("=");
            strToSign.append((String)reqParams.get(ret));
            strToSign.append("&");
        }

        strToSign.deleteCharAt(strToSign.length() - 1);
        strToSign.append("\n");
        byte[] reqEntity = new byte[0];
        if (StringUtils.isNotBlank(reqJsonEntity)) {
            reqEntity = reqJsonEntity.getBytes("utf-8");
        }

        byte[] data;
        if (reqEntity.length == 0) {
            data = DigestUtils.sha256("");
            strToSign.append(Base64.encodeToString(data,Base64.NO_WRAP));
        } else {
            ret = null;
            if (reqEntity.length <= 100) {
                data = reqEntity;
            } else {
                data = Arrays.copyOf(reqEntity, 100);
            }

            byte[] entity = DigestUtils.sha256(data);
            strToSign.append(Base64.encodeToString(entity,Base64.NO_WRAP));
        }

        ret = strToSign.toString();
        System.out.println(ret);
        System.out.println("------------------");
        return ret;
    }

    private void printArray(byte[] data) {
        StringBuffer sb = new StringBuffer();
        byte[] var3 = data;
        int var4 = data.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            byte d = var3[var5];
            sb.append(d);
            sb.append(",");
        }

        System.out.println(sb.toString());
    }

    private String calculateHMAC(String data, String key) throws SignatureException {
        try {
            SecretKeySpec e = new SecretKeySpec(key.getBytes("UTF8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(e);
            byte[] rawHmac = mac.doFinal(data.getBytes("UTF8"));
            String result = Base64.encodeToString(rawHmac,Base64.NO_WRAP);
            return result;
        } catch (Exception var7) {
            throw new SignatureException("Failed to generate HMAC : " + var7.getMessage());
        }
    }

    public String computeSignature(String jsonEntity, String method, String token, String reqPath) {
        try {
            Map<String, String> reqParams = new HashMap();
            int idx = reqPath.indexOf("?");
            String[] params = reqPath.substring(idx + 1).split("&");
            String[] var8 = params;
            int var9 = params.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                String param = var8[var10];
                String[] pair = param.split("=");
                if (pair.length == 1) {
                    reqParams.put(pair[0], "");
                } else {
                    reqParams.put(pair[0], pair[1]);
                }
            }

            reqPath = reqPath.substring(0, idx);
            String strToSign = this.computeStringToSign(reqPath, reqParams, jsonEntity, method);
            String mySignature = this.calculateHMAC(strToSign, token);
            mySignature = mySignature.replace(" ", "+");
            return URLEncoder.encode(mySignature, "utf-8");
        } catch (Exception var13) {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        String url = "https://sdk.xylink.com/api/rest/external/v1/meetingroom/918813020407/vods?endTime=0&enterpriseId=123f7b15161e26feb231e45e96e457e7c2774a1b&startTime=0";
        String token = "6f1e0891d5b413900bbcf94be78e979368cf3789b9919bc59de4de729185bec0";
        String jsonEntity = "[{\"title\":\"企业sdk测试\",\"startTime\":177796400000,\"endTime\":1797803600000,\"participants\":[\"957140\",\"469632\"],\"conferenceNumber\":\"915737369402\",\"address\":\"北京\",\"details\":\"1029d\",\"autoInvite\":1}]";
        System.out.println((new SignatureSamples()).computeSignature(jsonEntity, "GET", token, url));
        System.out.println(DigestUtils.md5Hex("QT1jZ3Albl3LiiErQFkhWuH2hVVEiKHkI+t6aBcxcPA="));
    }
}
