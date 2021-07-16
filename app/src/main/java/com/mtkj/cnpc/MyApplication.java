package com.mtkj.cnpc;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;

import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.Settings;
import com.ainemo.sdk.otf.VideoConfig;
import com.mtkj.cnpc.config.AppConfigSp;
import com.mtkj.cnpc.gen.DaoMaster;
import com.mtkj.cnpc.gen.DaoSession;
import com.mtkj.cnpc.utils.AlertUtil;
import com.mtkj.cnpc.utils.DeviceInfoUtils;

import java.util.List;

/**
 * 自定的Application
 *
 * @author zhangyazhou
 */
public class MyApplication extends Application {

    private static Context context;
    private static DaoSession daoSession;
    String db_meet = "meeting_data.db";
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        /**
         * 小鱼
         *
         * APPID:BVNGNNNNNNVNT
         *
         * token：
         * 973970f676450cee37f841c613fab8da95a7126481ddbe4ad4454fd02d3fb9e6
         *
         * enterpriseID(AINEMO_EXT_ID)：
         * 3bd8784bc2863b9ab404344f3575e7a58c909920
         * */
        AppConfigSp appConfigSp = AppConfigSp.getInstance();
        Settings settings = new Settings(appConfigSp.getExtId());
        if (appConfigSp.isPrivateMode()) {
            settings.setPrivateCloudAddress(appConfigSp.getPrivateHost());
        } else {
            settings.setDebug(appConfigSp.isDebugMode());
        }
        // Note: 默认或者不设置为360P, 360P满足大部分场景 如特殊场景需要720P, 请综合手机性能设置720P, 如果手机性
        // 能过差会出现卡顿,无法传输的情况, 请自己权衡.
        settings.setVideoMaxResolutionTx(VideoConfig.VD_1280x720);

        AlertUtil.init(getApplicationContext());
        int pId = Process.myPid();
        String processName = "";
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> ps = am.getRunningAppProcesses();
        for (RunningAppProcessInfo p : ps) {
            if (p.pid == pId) {
                processName = p.processName;
                break;
            }
        }

        // 避免被初始化多次
        if (processName.equals(getPackageName())) {
            NemoSDK nemoSDK = NemoSDK.getInstance();
            nemoSDK.init(this, settings);

            // 被叫服务，不使用被叫功能的请忽略
            Intent incomingCallService = new Intent(this, IncomingCallService.class);
            startService(incomingCallService);
        }
        DeviceInfoUtils.init(this);

        //数据库
        setupDatabase();
    }

    private void setupDatabase() {
        //创建数据库node_device_info.db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, db_meet, null);
        //获取可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取dao对象管理者
        daoSession = daoMaster.newSession();
    }

    public static Context getContext() {
        return context;
    }

    public static DaoSession getDaoInstant() {
        return daoSession;
    }
}

