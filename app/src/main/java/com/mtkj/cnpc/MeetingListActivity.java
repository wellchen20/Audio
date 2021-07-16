package com.mtkj.cnpc;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.log.L;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.sdk.model.AICaptionInfo;
import com.ainemo.sdk.model.AIParam;
import com.ainemo.sdk.otf.ConnectNemoCallback;
import com.ainemo.sdk.otf.LoginResponseData;
import com.ainemo.sdk.otf.MakeCallResponse;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.NemoSDKListener;
import com.ainemo.sdk.otf.RosterWrapper;
import com.ainemo.sdk.otf.VideoInfo;
import com.mtkj.cnpc.adapters.MeetingNameAdapter;
import com.mtkj.cnpc.api.CreateMeetingsApi;
import com.mtkj.cnpc.api.ScheduleMeetingsApi;
import com.mtkj.cnpc.api.VodsApi;
import com.mtkj.cnpc.bean.MeettingInfoData;
import com.mtkj.cnpc.config.AppConfigSp;
import com.mtkj.cnpc.dao.MeettingDao;
import com.mtkj.cnpc.entity.MeetingInfo;
import com.mtkj.cnpc.utils.Global;
import com.mtkj.cnpc.utils.MeettingUtils;
import com.mtkj.cnpc.zbar.CaptureActivity;
import com.mtkj.cnpc.zxing.camera.MipcaActivityCapture;
import com.rokid.appmonitor.IAppMonitor;
import com.xylink.model.Pager;
import com.xylink.model.ReminderMeeting;
import com.xylink.model.SdkCloudMeetingRoomRequest;
import com.xylink.model.SdkMeeting;
import com.xylink.util.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MeetingListActivity extends AppCompatActivity {
    private static final String TAG = "MeetingListActivity";
    private String myNEmo;
    private String mCallNumber;
    private String mDisplayName;
    private ProgressDialog progressDialog;
    private String meetingNumber = "9005687862";
    private TextView tv_user;
    boolean islogin = false;
    int pos;
    ListView lv_meeting;
    public List<MeettingInfoData> mList = new ArrayList<>();
    MeetingInfo info;
    MeetingListReceiver receiver;
    IntentFilter filter;
    TextView tv_meeting_begin;
    private static final String ACTION_TOPAPP_CHANGE_STATUS = "com.rokid.action.top.app.change.status";
    boolean active = true;
    MeetingNameAdapter adapter;
    //音响
    IAppMonitor mAppMonitorBinder;
    ServiceConnection mAppMonitorConn;
    int count = 0;
    int requestCapCode = 100;
    int requestProxyCode = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_list);
        initReceiver();
        initData();
        setViews();
        setAdapters();
        setListeners();
    }

    private void initReceiver() {
        receiver = new MeetingListReceiver();
        filter = new IntentFilter();
        filter.addAction(Global.SELECT_ITEM);
        filter.addAction(Global.EXIT_APP);
        filter.addAction(Global.CALL_NUMBER);
        filter.addAction(Global.BEGIN_MEETTING);
        filter.addAction(Global.NEXT_PAGE);
        filter.addAction(Global.LAST_PAGE);
        filter.addAction(Global.DELETE_ITEM);
        filter.addAction(Global.LOGIN_XY);
        filter.addAction(Global.SET_PROXY);
        filter.addAction(Global.DELETE_PROXY);
        registerReceiver(receiver,filter);
    }


    private void initData() {
        Intent intent = getIntent();
        String myNumber = intent.getStringExtra("MY_NUMBER");
        islogin = intent.getBooleanExtra("islogin",false);
        myNEmo = myNumber;
    }

    private void setViews() {
        lv_meeting = findViewById(R.id.lv_meeting);
        tv_meeting_begin = findViewById(R.id.tv_meeting_begin);
        tv_user = findViewById(R.id.tv_user);
        if (myNEmo.equals("")||myNEmo==""){
            tv_user.setText("请登录");
        }else {
            tv_user.setText(myNEmo);
        }

    }

    private void setAdapters() {
        mList = MeettingDao.getAllMeetting();
        adapter = new MeetingNameAdapter(this,mList);
        lv_meeting.setAdapter(adapter);
    }

    private void setListeners() {
        tv_meeting_begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(Global.SELECT_ITEM);
                intent.putExtra("num","1");
                sendBroadcast(intent);*/
                Intent intentCap = new Intent(MeetingListActivity.this, CaptureActivity.class);
                startActivityForResult(intentCap,requestCapCode);
            }
        });


//        available = validateMicAvailability();
//        Log.e(TAG, "available: "+available );
    }

    public void inRoom(String number,String password){
        if (android.text.TextUtils.isEmpty(number)) {
            Toast.makeText(MeetingListActivity.this, "请输入呼叫号码", Toast.LENGTH_SHORT).show();
            return;
        }
        mCallNumber = number;
        showLoading();
        String pwd = password;

        NemoSDK.getInstance().makeCall(mCallNumber, pwd, new MakeCallResponse() {
            @Override
            public void onCallSuccess() {
                // 查询号码成功, 进入通话界面
                L.i(TAG, "success go XyCallActivity");
                hideLoading();
                Intent callIntent = new Intent(MeetingListActivity.this, XyCallActivity.class);
                callIntent.putExtra("number", mCallNumber);
                startActivity(callIntent);
                insertOrReplace();
            }



            @Override
            public void onCallFail(final int error, final String msg) {
                Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        hideLoading();
                        Toast.makeText(MeetingListActivity.this,
                                "Error Code: " + error + ", msg: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // query record permission
        NemoSDK.getInstance().getRecordingUri(mCallNumber);
    }

    private void insertOrReplace() {
        try {
//            Result<Pager> pagerResult = new ScheduleMeetingsApi().getMeetingByConfrenceNumber(AppConfigSp.XY_PRD_EXT_ID,AppConfigSp.XY_TOKEN,0,10,mCallNumber);
            Result<String> pagerResult = new ScheduleMeetingsApi().getstatusByConfrenceNumber(AppConfigSp.XY_PRD_EXT_ID,AppConfigSp.XY_TOKEN,mCallNumber);
            Log.e(TAG, "pagerResult: "+pagerResult.toString());
            MeettingInfoData data = new MeettingInfoData();
            data.setMeettingName("最近的会议室");
            data.setMeettingNum(mCallNumber);
            data.setTime(new Date().getTime());
            MeettingDao.insertMeetting(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //新建云会议室
    public void newRoom() throws IOException {
        // 创建云会议号
        SdkMeeting sdkMeeting = createRoom();
        System.out.println("云会议室：" + sdkMeeting);
        meetingNumber = sdkMeeting.getMeetingNumber();
        Log.e("meetingNumber", "meetingNumber: "+meetingNumber );

    }

    // 创建云会议室
    public SdkMeeting createRoom() throws IOException {
        CreateMeetingsApi createMeetingApi = new CreateMeetingsApi();
        SdkCloudMeetingRoomRequest sdkMeetingReq = new SdkCloudMeetingRoomRequest();
        sdkMeetingReq.setMeetingName("我的云会议室1");
        return createMeetingApi.createMeetingV2(AppConfigSp.XY_PRD_EXT_ID, AppConfigSp.XY_TOKEN, sdkMeetingReq);
    }

    private void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MeetingListActivity.this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy " );
        unregisterReceiver(receiver);
        try {
            mAppMonitorBinder.active(0);
            unbindService(mAppMonitorConn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public class MeetingListReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Global.SELECT_ITEM)){//打开第几个/加入x号会议室
                Log.e(TAG, "SELECT_ITEM");
                String num = intent.getStringExtra("num");
                double order1 = Double.parseDouble(num);
                int order = (int)order1;
                if (order>0 && order<=mList.size()){
                    String meetingNum = mList.get(order-1).getMeettingNum();
                    inRoom(meetingNum,null);
                }else {
                    Toast.makeText(MeetingListActivity.this,"会议号错误",Toast.LENGTH_SHORT).show();
                }
            }else if (intent.getAction().equals(Global.DELETE_ITEM)){//删除第几个/删除#号会议室
                Log.e(TAG, "DELETE_ITEM");
                String num = intent.getStringExtra("num");
                double order1 = Double.parseDouble(num);
                int order = (int)order1;
                if (order>0 && order<=mList.size()){
                    String meetingNum = mList.get(order-1).getMeettingNum();
                    //删除会议号
                    MeettingInfoData meeting = MeettingDao.getMeetingByNum(meetingNum);
                    MeettingDao.deleteMeettingByKey(meeting.getId());
                    updateList();
                    Toast.makeText(MeetingListActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MeetingListActivity.this,"会议号错误",Toast.LENGTH_SHORT).show();
                }
            }else if (intent.getAction().equals(Global.EXIT_APP)){//退出程序
                Log.e(TAG, "EXIT_APP_Meet");
                finish();
            }else if (intent.getAction().equals(Global.CALL_NUMBER)){//呼叫/拨号+number
                Log.e(TAG, "CALL_NUMBER");
                String num = intent.getStringExtra("num");
                if (num!=null && num.contains(".000000")){
                    String newRoom = num.split(".000000")[0];
                    Log.e(TAG, "newRoom: "+newRoom );
                    inRoom(newRoom,null);
                }
            }else if (intent.getAction().equals(Global.BEGIN_MEETTING)){//发起会议进入默认会议室
                Log.e(TAG, "BEGIN_MEETTING");
                inRoom(meetingNumber,null);
            }else if (intent.getAction().equals(Global.NEXT_PAGE)){//下一页
                if (mList.size()>6){
                    lv_meeting.setSelection(6);
                }
                Toast.makeText(MeetingListActivity.this,"下一页",Toast.LENGTH_SHORT).show();
            }else if (intent.getAction().equals(Global.LAST_PAGE)){//上一页
                lv_meeting.setSelection(0);
                Toast.makeText(MeetingListActivity.this,"上一页",Toast.LENGTH_SHORT).show();
            }else if (intent.getAction().equals(Global.LOGIN_XY)){//重新登录
                Intent intentCap = new Intent(MeetingListActivity.this, CaptureActivity.class);
                startActivityForResult(intentCap,requestCapCode);
            }else if (intent.getAction().equals(Global.SET_PROXY)){//设置代理
                Intent intentCap = new Intent(MeetingListActivity.this, CaptureActivity.class);
                startActivityForResult(intentCap,requestProxyCode);
            }else if (intent.getAction().equals(Global.DELETE_PROXY)){//清除代理
                Settings.Global.putString(MeetingListActivity.this.getContentResolver(), "http_proxy",null);
                Settings.Global.putString(MeetingListActivity.this.getContentResolver(), "global_http_proxy_host",null);
                Settings.Global.putString(MeetingListActivity.this.getContentResolver(), "global_http_proxy_port",null);
                String proxy = System.getProperty( "http.proxyHost" );
                String port = System.getProperty( "http.proxyPort" );
                Log.e(TAG, "proxy: "+proxy+" port:"+port);
                Toast.makeText(MeetingListActivity.this, "请重启设备后生效", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==requestCapCode && resultCode==RESULT_OK){
            String result = data.getStringExtra("resultString");
            if(result.length()>0)
            {
                String[] str=result.split(";");
                if(str!=null&&str.length==2)
                {
                    Log.e("baichaoqun", str[0]+"  "+str[1]);
                    saveNumber(str[0],str[1]);
                    init(str[0],str[1]);
                }

            }
        }else if (requestCode==requestProxyCode && resultCode==RESULT_OK){
            String result = data.getStringExtra("resultString");
            Settings.Global.putString(MeetingListActivity.this.getContentResolver(), "http_proxy",result);
            String proxy = System.getProperty( "http.proxyHost" );
            String port = System.getProperty( "http.proxyPort" );
            Log.e(TAG, "proxy: "+proxy+" port:"+port);
            Toast.makeText(MeetingListActivity.this, "代理已生效"+result, Toast.LENGTH_SHORT).show();
        }
    }

    public void init(String name,String password){
        NemoSDK.getInstance().logout();
        NemoSDK.getInstance().loginXYlinkAccount(name, password, new ConnectNemoCallback() {
            @Override
            public void onFailed(final int i) {
                L.e(TAG, "使用小鱼账号登录失败，错误码：" + i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MeetingListActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(LoginResponseData data, boolean isDetectingNetworkTopology) {
                L.i(TAG, "使用小鱼账号登录成功，号码为：" + data.getCallNumber());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        islogin = true;
                        Toast.makeText(MeetingListActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        saveNumber(name,password);
                        tv_user.setText(name);
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

    public void saveNumber(String name,String password){
        SharedPreferences preferences = getSharedPreferences("xytest", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name",name);
        editor.putString("password",password);
        editor.commit();
    }

    private void updateList(){
        mList.clear();
        List<MeettingInfoData> linshi = MeettingDao.getAllMeetting();
        for (int i=0;i<linshi.size();i++){
            mList.add(linshi.get(i));
        }
        linshi = null;
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
        if(mAppMonitorBinder!=null){
            try {
                mAppMonitorBinder.active(2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            startPage();
        }
        Log.e(TAG, "onResume " );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart " );
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause " );
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop " );
    }

    //音响
    private void startPage() {
        mAppMonitorConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                mAppMonitorBinder = IAppMonitor.Stub.asInterface(binder);
                try {
                    mAppMonitorBinder.setBinder(IAppMonitor.APPID_VIDEOCALL, binder);//mBinderForAppMonitor
                    mAppMonitorBinder.active(2);
                    Log.e("mAppMonitor", "mAppMonitor!!.active: 2");
                } catch ( Exception e) {
                    Log.e(TAG, e.getMessage() );
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.e("Disconnected", "Disconnected");
            }
        };
        Intent intent = new Intent();
        intent.setAction("com.rokid.appmonitor.AppMonitorService");
        intent.setPackage("com.rokid.appmonitor");
        bindService(intent,mAppMonitorConn, Context.BIND_AUTO_CREATE);
    }

}
