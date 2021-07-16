package com.mtkj.cnpc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.log.L;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ainemo.sdk.otf.ConnectNemoCallback;
import com.ainemo.sdk.otf.LoginResponseData;
import com.ainemo.sdk.otf.MakeCallResponse;
import com.ainemo.sdk.otf.NemoSDK;
import com.alibaba.fastjson.JSON;
import com.mtkj.cnpc.bean.MeetingData;
import com.mtkj.cnpc.bean.MeettingInfoData;
import com.mtkj.cnpc.dao.MeettingDao;
import com.mtkj.cnpc.utils.Global;
import com.rokid.appmonitor.IAppMonitor;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.InputStream;

import io.reactivex.functions.Consumer;

/**
 * 小鱼账户登录界面
 *
 * @author zhangyazhou
 */
public class LoginActivity extends AppCompatActivity {

    public static final String LOGIN_TYPE_KEY = "login_type";

    /**
     * 用小鱼账号登录
     */
    public static final int LOGIN_TYPE_XYLINK = 0;
    /**
     * 用第三方账号登录
     */
    public static final int LOGIN_TYPE_EXTERNAL = 1;
    /**
     * 第三方鉴权登录
     */
    public static final int LOGIN_TYPE_THIRD_AUTH = 2;

    private static final String TAG = "LoginActivity";

    private NemoSDK nemoSDK = NemoSDK.getInstance();
    private int loginType;
    //    private EditText displayName;
//    private EditText externalId;
    private ProgressDialog loginDialog;
    private String name = "";
    private String password = "";
    private String callNumber;
    private boolean islogin = false;
    IAppMonitor mAppMonitorBinder;
    ServiceConnection mAppMonitorConn;
    private static final String ACTION_TOPAPP_CHANGE_STATUS = "com.rokid.action.top.app.change.status";
    boolean active = true;
    private ProgressDialog progressDialog;
    LoginReceiver receiver;
    IntentFilter filter;
    boolean isFirst = false;
    String json;
    MeetingData mMeetingData;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_background);
        checkFirst();
        active = true;
        sendTop();
        initReceiver();
        checkPermission();
    }

    private void checkFirst() {
        SharedPreferences pref = getSharedPreferences("XyLoginActivity", 0);
//取得相应的值，如果没有该值，说明还未写入，用true作为默认值
        isFirst = pref.getBoolean("isFirstIn", true);
        if (isFirst){
            Log.e(TAG, "checkFirst: ");
            readMySql();
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirstIn", false);
            editor.commit();
        }
    }

    private void readMySql() {
        json = readLocalJson(this,"meeting.txt");
        new Thread(){
            public void run() {
                mMeetingData = JSON.parseObject(json, MeetingData.class);//fastJson解析
                Log.e(TAG, "mMeetingData.size: " +mMeetingData.getData().size());
                for (int i=0;i<mMeetingData.getData().size();i++){
                    MeettingInfoData meettingInfoData = new MeettingInfoData();
                    meettingInfoData.setMeettingNum(mMeetingData.getData().get(i).getMeettingNum());
                    meettingInfoData.setMeettingName(mMeetingData.getData().get(i).getMeettingName());
                    meettingInfoData.setTime(mMeetingData.getData().get(i).getTime());
                    MeettingDao.insertMeetting(meettingInfoData);
                }
            };
        }.start();
    }

    /**读取本地JSON数据*/
    public static String readLocalJson(Context context,  String fileName){
        String jsonString="";
        String resultString="";
        try {
            InputStream inputStream=context.getResources().getAssets().open(fileName);
            byte[] buffer=new byte[inputStream.available()];
            inputStream.read(buffer);
            resultString=new String(buffer,"utf-8");
        } catch (Exception e) {
            // TODO: handle exception
        }
        return resultString;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!islogin){
            loginXyAccount();
        }else {
            inMeetingList();
        }
    }

    private void initReceiver() {
        receiver = new LoginReceiver();
        filter = new IntentFilter();
        filter.addAction(Global.EXIT_APP);
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void inMeetingList(){
        Intent intent = new Intent(LoginActivity.this, MeetingListActivity.class);
        intent.putExtra("MY_NUMBER", name);
        intent.putExtra("displayName", name);
        intent.putExtra("islogin", islogin);
        startActivity(intent);
    }



    private void sendTop() {
        Intent intent = new Intent();
        intent.setAction(ACTION_TOPAPP_CHANGE_STATUS);
        intent.putExtra("status",active?"resume":"pause");
        sendBroadcast(intent);
    }

    @SuppressLint("CheckResult")
    private void checkPermission() {
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        L.i(TAG, "request permission result:" + aBoolean);
                    }
                });
    }

    private void loginExternalAccountByToken(String account, String token) {

        nemoSDK.loginExternalAccountByToken(account, token, new ConnectNemoCallback() {
            @Override
            public void onFailed(final int i) {
                dismissDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "三方授权登录失败，错误码：" + i, Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onSuccess(LoginResponseData data, boolean isDetectingNetworkTopology) {
                dismissDialog();
                L.i(TAG, "三方授权登录失败，号码为：" + data.getCallNumber());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "三方授权登录成功", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent(LoginActivity.this, DialActivity.class);
                intent.putExtra("MY_NUMBER", data.getCallNumber());
//                intent.putExtra("displayName", displayName.getText().toString());
//                L.i(TAG, "displayNameCallActivity11=" + displayName.getText().toString() + "MY_NUMBER" + data.getCallNumber());
                startActivity(intent);
                finish();
            }

            @Override
            public void onNetworkTopologyDetectionFinished(LoginResponseData resp) {
                L.i(TAG, "net detect onNetworkTopologyDetectionFinished 2");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "网络探测已完成", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void showLoginDialog() {
        loginDialog = new ProgressDialog(this);
        loginDialog.setTitle("登录");
        loginDialog.setMessage("正在登录,请稍后...");
        loginDialog.setCancelable(false);
        loginDialog.show();
    }

    private void dismissDialog() {
        if (loginDialog != null && loginDialog.isShowing()) {
            loginDialog.dismiss();
        }
    }

    private void loginXyAccount() {
        showLoginDialog();
        SharedPreferences preferences = getSharedPreferences("xytest", Context.MODE_PRIVATE);
        name = preferences.getString("name","");
        password = preferences.getString("password","");
        Log.e(TAG, "name: "+name);
        if (name.equals("")||name==""){
            Toast.makeText(LoginActivity.this, "账号未登录,请先登录账号", Toast.LENGTH_LONG).show();
            inMeetingList();
        }else {
            nemoSDK.loginXYlinkAccount(name, password, new ConnectNemoCallback() {
                @Override
                public void onFailed(final int i) {
                    dismissDialog();
                    L.e(TAG, "使用小鱼账号登录失败，错误码：" + i);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onSuccess(LoginResponseData data, boolean isDetectingNetworkTopology) {
                    L.i(TAG, "使用小鱼账号登录成功，号码为：" + data.getCallNumber());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDialog();
                            islogin = true;
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            saveNumber(name,password);
                            inMeetingList();
                        }
                    });

                }

                @Override
                public void onNetworkTopologyDetectionFinished(LoginResponseData resp) {
                    L.i(TAG, "net detect onNetworkTopologyDetectionFinished 2");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                        Toast.makeText(LoginActivity.this, "网络探测已完成", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }

    }

    private void loginExternalAccount() {
        /*if (displayName.getText().toString().length() == 0 || externalId.getText().toString().length() == 0) {
            Toast.makeText(getApplicationContext(), "displayName或externalId为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoginDialog();
        try {
            nemoSDK.loginExternalAccount(displayName.getText().toString(), externalId.getText().toString(),
                    new ConnectNemoCallback() {
                        @Override
                        public void onFailed(final int errorCode) {
                            dismissDialog();
                            L.e(TAG, "匿名登录失败，错误码：" + errorCode);
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "匿名登录失败，错误码：" + errorCode, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                L.e(TAG, e.getMessage());
                            }
                        }

                        @Override
                        public void onSuccess(LoginResponseData data, boolean isDetectingNetworkTopology) {
                            dismissDialog();
                            L.i(TAG, "匿名登录成功，号码为：" + data);
                            try {
                                String tip = "匿名登录成功";
                                if (isDetectingNetworkTopology) {
                                    tip += ", 需要探测网络";
                                }
                                final String s = tip;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                L.e(TAG, e.getMessage());
                            }
                            saveNumber(displayName.getText().toString(),externalId.getText().toString(),0);//保存匿名账号
                            Intent intent = new Intent(LoginActivity.this, DialActivity.class);
                            intent.putExtra("MY_NUMBER", data.getCallNumber());
                            intent.putExtra("displayName", displayName.getText().toString());
                            intent.putExtra("userId", data.getUserId() + "");
                            L.i(TAG, "displayNameCallActivity11=" + displayName.getText().toString());
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onNetworkTopologyDetectionFinished(LoginResponseData resp) {
                            L.i(TAG, "net detect onNetworkTopologyDetectionFinished 1");
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "探测完成", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                L.e(TAG, e.getMessage());
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void saveNumber(String name,String password){
        SharedPreferences preferences = getSharedPreferences("xytest", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name",name);
        editor.putString("password",password);
        editor.commit();
    }

    private void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setTitle("请稍后...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    class LoginReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Global.EXIT_APP)){
                Log.e(TAG, "EXIT_APP_login");
                finish();
            }
        }
    }
}
