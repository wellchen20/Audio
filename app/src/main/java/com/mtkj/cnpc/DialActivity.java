package com.mtkj.cnpc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.log.L;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.sdk.otf.MakeCallResponse;
import com.ainemo.sdk.otf.NemoSDK;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jakewharton.rxbinding2.view.RxView;
import com.mtkj.cnpc.utils.Global;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xylink.model.MeetingInfo;
import com.xylink.model.Pager;
import com.xylink.model.SdkCloudMeetingRoomRequest;
import com.xylink.model.SdkMeeting;
import com.mtkj.cnpc.adapters.VideoAdapter;
import com.mtkj.cnpc.api.MeetingStatisticsApi;
import com.mtkj.cnpc.config.AppConfigSp;
import com.mtkj.cnpc.api.CreateMeetingsApi;
import com.mtkj.cnpc.api.VodsApi;
import com.mtkj.cnpc.entity.DownLoadUrl;
import com.mtkj.cnpc.entity.MyVods;
import com.mtkj.cnpc.entity.PlayUrl;
import com.xylink.util.Result;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 拨号界面:
 * <p> 小鱼SDK目前的通话可以认为分两步:
 * 1. makeCall: 呼叫准备工作, 验证号码跟密码的有效性等
 * success- 成功: 说明此号码有效可以呼叫-至此可以进入通话界面展示相关UI
 * fail   - 失败: 说明呼叫无法建立(无效号码, 网络不通, 密码错误<有密码的会议室>等)
 * <p>
 * 2. setNemoSDKListener: 具体通话业务, 建立呼叫之后进入通话界面, 在通话界面监听此回调, 实现通话,白板屏幕图片共享等具体业务
 * {@link XyCallPresenter}
 * NemoSDK.getInstance().setNemoSDKListener(NemoSDKListener):
 *
 * <p>
 * 具体流程参考文档 <>http://openapi.xylink.com/android/</>
 */
public class DialActivity extends AppCompatActivity {
    private static final String TAG = "DialFragment";
    private String myNEmo;
    private String mCallNumber;
    private String mDisplayName;
    public static String roomNumber;
    private ProgressDialog progressDialog;
//    private String meetingNumber = "910030509729";910001569837
private String meetingNumber = "9005687862";
    private Button bt_create_room;
    private ListView lv_video;
    List<MyVods> vodsList;
    DownLoadUrl downLoadUrl;
    PlayUrl playUrl;
    VodsApi vodsApi;
    Handler handler;
    VideoAdapter adapter;
    boolean islogin = false;
    int pos;
    @SuppressLint({"CheckResult", "HandlerLeak"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dial_fragment_layout);
        vodsApi = new VodsApi();
        Intent intent = getIntent();
        String myNumber = intent.getStringExtra("MY_NUMBER");
        islogin = intent.getBooleanExtra("islogin",false);
        myNEmo = myNumber;
//        getRoom();
        if(meetingNumber!=null){
            ((EditText)findViewById(R.id.number)).setText(meetingNumber);
        }

        lv_video = findViewById(R.id.lv_video);
        if (null != myNumber)
            ((TextView) findViewById(R.id.local_number)).setText("我的号码：" + myNumber);
        final EditText number = findViewById(R.id.number);
        final EditText password = findViewById(R.id.password);
        final Button btMakeCall = findViewById(R.id.make_call);
        bt_create_room = findViewById(R.id.bt_create_room);
        String num = number.getText().toString();
        String pwd = password.getText().toString();
        inRoom(num,pwd);
       /* RxView.clicks(btMakeCall).throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                hideSoftKeyboard();
                if (android.text.TextUtils.isEmpty(number.getText())) {
                    Toast.makeText(DialActivity.this, "请输入呼叫号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkPermission();
                mCallNumber = number.getText().toString();
                roomNumber = mCallNumber;
                showLoading();
                String pwd = password.getText().toString().trim();
                NemoSDK.getInstance().makeCall(mCallNumber, pwd, new MakeCallResponse() {
                    @Override
                    public void onCallSuccess() {
                        // 查询号码成功, 进入通话界面
                        L.i(TAG, "success go XyCallActivity");
                        hideLoading();
                        Intent callIntent = new Intent(DialActivity.this, XyCallActivity.class);
                        callIntent.putExtra("number", mCallNumber);
                        startActivity(callIntent);
                    }

                    @Override
                    public void onCallFail(final int error, final String msg) {
                        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) throws Exception {
                                hideLoading();
                                Toast.makeText(DialActivity.this,
                                        "Error Code: " + error + ", msg: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                // query record permission
                NemoSDK.getInstance().getRecordingUri(mCallNumber);
            }
        });*/

        bt_create_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            newRoom();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent feedIntent = new Intent(DialActivity.this, FeedbackActivity.class);
                startActivity(feedIntent);
            }
        });

        findViewById(R.id.bt_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginOut();
                NemoSDK.getInstance().logout();
                finish();
            }
        });

//        getVideos();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==0){
                    if (vodsList.size()>0){
                        adapter = new VideoAdapter(DialActivity.this,vodsList);
                        lv_video.setAdapter(adapter);
                        setListeners();
                    }
                }else if (msg.what==1){
                    Intent intent = new Intent(DialActivity.this,VideoActivity.class);
                    if (!playUrl.isShared()){
                        Toast.makeText(DialActivity.this,"视频解码中，稍后再试",Toast.LENGTH_SHORT).show();
                    }else if (playUrl.isShared()){
                        intent.putExtra("path",playUrl.getInnerPlayUrl());
                        startActivity(intent);
                    }
                }else if (msg.what==2){
                    vodsList.remove(pos);
                    adapter.notifyDataSetChanged();
                    hideLoading();
                    Toast.makeText(DialActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                }
            }
        };
        hideSoftKeyboard();
    }

    public void getVideos(){
        new Thread(){
            @Override
            public void run() {
                getZhibo();
            }
        }.start();
    }

    public void inRoom(String number,String password){
        hideSoftKeyboard();
        if (android.text.TextUtils.isEmpty(number)) {
            Toast.makeText(DialActivity.this, "请输入呼叫号码", Toast.LENGTH_SHORT).show();
            return;
        }
        checkPermission();
        mCallNumber = number;
        roomNumber = mCallNumber;
        showLoading();
        String pwd = password;
        NemoSDK.getInstance().makeCall(mCallNumber, pwd, new MakeCallResponse() {
            @Override
            public void onCallSuccess() {
                // 查询号码成功, 进入通话界面
                L.i(TAG, "success go XyCallActivity");
                hideLoading();
                Intent callIntent = new Intent(DialActivity.this, XyCallActivity.class);
                callIntent.putExtra("number", mCallNumber);
                startActivity(callIntent);
            }

            @Override
            public void onCallFail(final int error, final String msg) {
                Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        hideLoading();
                        Toast.makeText(DialActivity.this,
                                "Error Code: " + error + ", msg: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // query record permission
        NemoSDK.getInstance().getRecordingUri(mCallNumber);
    }

    @Override
    protected void onResume() {
        getVideos();
        super.onResume();
    }

    private void loginOut(){
        SharedPreferences sp=getSharedPreferences("xytest", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.clear();
        editor.commit();
    }

    private void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(DialActivity.this);
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

    private void checkPermission() {
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        L.i(TAG, "request permission result:" + aBoolean);
                        Log.e(TAG, "request permission result:" + aBoolean);
                    }
                });
    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void getZhibo() {
        //获取视频列表
        String results= vodsApi.getMeetingRoomVodsUrl(AppConfigSp.XY_PRD_EXT_ID,AppConfigSp.XY_TOKEN,meetingNumber,0l,new Date().getTime());
        Log.e("getZhibo", "getZhibo: "+results);
//        initOkhttp(results,0);
    }
    AlertDialog alertDialog;
    public void setListeners(){
        lv_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MyVods myVod = vodsList.get(position);
                int vodId = myVod.getVodId();
                //获取视频列表
                String reUrl = vodsApi.getPlayVodByVodId(AppConfigSp.XY_PRD_EXT_ID,AppConfigSp.XY_TOKEN,vodId+"");
                new Thread(){
                    @Override
                    public void run() {
                        initOkhttp(reUrl,1);
                    }
                }.start();
            }
        });

        lv_video.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                alertDialog = new AlertDialog.Builder(DialActivity.this)
                        .setTitle("删除？")
                        .setMessage("确认删除此视频吗？")
                        .setIcon(R.drawable.whiteboard_clear_all)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showLoading();
                                pos = position;
                                MyVods myVod = vodsList.get(position);
                                int vodId = myVod.getVodId();
//                                String delete_url = vodsApi.deleteVideoById(AppConfigSp.XY_PRD_EXT_ID,AppConfigSp.XY_TOKEN,vodId+"");
                                new Thread(){
                                    @Override
                                    public void run() {
//                                        initOkhttp(delete_url,2);
                                        try {
                                            //此处用的自带的删除方法，自定义不可用
                                            Result result = vodsApi.deleteVideo(AppConfigSp.XY_PRD_EXT_ID,AppConfigSp.XY_TOKEN,vodId+"");
                                            Log.e("delete", "delete: "+result);
                                            if (result.getErrorStatus()==200){
                                                handler.sendEmptyMessage(2);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alertDialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();

                return true;
            }
        });
    }


    public void initOkhttp(String url,int flag){
        //创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建Requst对象
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        //创建Call对象
        Call call = okHttpClient.newCall(request);
        /**
         * 调用call的enqueue并重写onResponse方法
         */
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (flag==0){
                    Log.e("onResponse0", "onResponse0: " + json);
                    vodsList = JSONArray.parseArray(json,MyVods.class);
                    handler.sendEmptyMessage(0);
                } else if (flag==1){
                    Log.e("onResponse1", "onResponse1: " + json);
//                    downLoadUrl = JSON.parseObject(json,DownLoadUrl.class);
                    playUrl = JSON.parseObject(json,PlayUrl.class);
                    handler.sendEmptyMessage(1);
                }/*else if (flag==2){
                    Log.e("onResponse2", "onResponse2: " + json);
                    handler.sendEmptyMessage(2);
                }*/
            }
        });
    }

    //新建云会议室
    public void newRoom() throws IOException {
//        getRoom();
        if (null == meetingNumber) {
            // 创建云会议号
            SdkMeeting sdkMeeting = createRoom();
            System.out.println("云会议室：" + sdkMeeting);
            meetingNumber = sdkMeeting.getMeetingNumber();
            /*if (meetingNumber!=null){
                SharedPreferences sp = getSharedPreferences("meeting",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("room",meetingNumber);
                editor.commit();
            }*/

            Log.e("meetingNumber", "meetingNumber: "+meetingNumber );
        }else {
            ((EditText)findViewById(R.id.number)).setText(meetingNumber);
        }
    }

    // 创建云会议室
    public SdkMeeting createRoom() throws IOException {
        CreateMeetingsApi createMeetingApi = new CreateMeetingsApi();
        SdkCloudMeetingRoomRequest sdkMeetingReq = new SdkCloudMeetingRoomRequest();
        sdkMeetingReq.setMeetingName("我的云会议室1");
        return createMeetingApi.createMeetingV2(AppConfigSp.XY_PRD_EXT_ID, AppConfigSp.XY_TOKEN, sdkMeetingReq);
    }

    public String getRoom() {
        SharedPreferences sp = getSharedPreferences("meeting",Context.MODE_PRIVATE);
        meetingNumber = sp.getString("room",null);
        return meetingNumber;
    }



    @Override
    public void onBackPressed() {
        // 被叫服务，不使用被叫功能的请忽略
        if (islogin){
            Intent incomingCallService = new Intent(this, IncomingCallService.class);
            stopService(incomingCallService);
            finish();
        }else {
            super.onBackPressed();
        }
    }

}
