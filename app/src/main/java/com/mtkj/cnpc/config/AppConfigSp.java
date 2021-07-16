package com.mtkj.cnpc.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.mtkj.cnpc.MyApplication;

/**
 * App配置
 *
 * @author zhangyazhou
 */
public class AppConfigSp {

    private static final String SP_NAME = "config_sp";
    private static final String EXT_ID = "ext_id";
    private static final String PRIVATE_HOST = "private_host";
    private static final String PRIVATE_MODE = "private_mode";
    private static final String DEBUG_MODE = "debug_mode";
    /**
     * 小鱼开发环境extID: 40260e9046bae2da238ac0b0c572326b91726a83
     */
    public static final String XY_DEV_EXT_ID = "40260e9046bae2da238ac0b0c572326b91726a83";
    /**
     * 小鱼生产环境extID
     * 12e53a6df2e91e6177e627c8e336a6888ff98104
     *
     */
    public static final String XY_PRD_EXT_ID = "3bd8784bc2863b9ab404344f3575e7a58c909920";

    public static final String XY_TOKEN = "973970f676450cee37f841c613fab8da95a7126481ddbe4ad4454fd02d3fb9e6";
    public static final String XY_APPID = "BKNVBZVVKNNNT";//BVNGNNNNNNVNT

    private SharedPreferences prefs;

    private static class InstanceHolder {
        private static final AppConfigSp INSTANCE = new AppConfigSp();
    }

    public static AppConfigSp getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private AppConfigSp() {
        prefs = MyApplication.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public void saveExtId(String extId) {
        prefs.edit().putString(EXT_ID, extId).apply();
    }

    public String getExtId() {
        return prefs.getString(EXT_ID, XY_PRD_EXT_ID);
    }

    public void saveDebugMode(boolean debugMode) {
        prefs.edit().putBoolean(DEBUG_MODE, debugMode).apply();
    }

    public boolean isDebugMode() {
        return prefs.getBoolean(DEBUG_MODE, false);
    }

    public void savePrivateMode(boolean privateMode) {
        prefs.edit().putBoolean(PRIVATE_MODE, privateMode).apply();
    }

    public boolean isPrivateMode() {
        return prefs.getBoolean(PRIVATE_MODE, false);
    }

    public void savePrivateHost(String host) {
        prefs.edit().putString(PRIVATE_HOST, host).apply();
    }

    public String getPrivateHost() {
        return prefs.getString(PRIVATE_HOST, "");
    }
}
