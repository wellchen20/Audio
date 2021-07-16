package com.mtkj.cnpc;

import android.Manifest;
import android.content.Intent;
import android.log.L;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.sdk.otf.NemoSDK;
import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;


/**
 * 拨号界面
 *
 * @author zhangyazhou
 * @deprecated 参考 {@link DialActivity}
 */
public class DialFragment extends Fragment {

    private static final String TAG = "DialFragment";
    private String myNumber;
    private String mCallNumber;
    private CallNumberInterface callBack;
    private String mDisplayName;

    public static DialFragment newInstance() {
        return new DialFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dial_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        if (null != myNumber)
            ((TextView) view.findViewById(R.id.local_number)).setText("我的号码：" + myNumber);

        final EditText number = view.findViewById(R.id.number);
        final EditText password = view.findViewById(R.id.password);
        final Button btMakeCall = view.findViewById(R.id.make_call);

        RxView.clicks(btMakeCall).throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (number.getText().toString().trim().length() == 0) {
                            Toast.makeText(getActivity(), "请输入呼叫号码", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        checkPermission();
                        mCallNumber = number.getText().toString();
                        callBack.getResult(mCallNumber);
                        callBack.getDisplayName(mDisplayName);

                        NemoSDK.getInstance().makeCall(mCallNumber, password.getText().toString());
                        NemoSDK.getInstance().getRecordingUri(mCallNumber);
                    }
                });
        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FeedbackActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.bt_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NemoSDK.getInstance().logout();
                getActivity().finish();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public void setMyNumber(String myNumber) {
        this.myNumber = myNumber;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    private void checkPermission() {
        RxPermissions permissions = new RxPermissions(getActivity());
        permissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        L.i(TAG, "request permission result:" + aBoolean);
                    }
                });
    }

    /*接口*/
    public interface CallNumberInterface {
        /*定义一个获取信息的方法*/
        public void getResult(String callNumber);

        public void getDisplayName(String displayName);

    }

    /*设置监听器*/
    public void setCallBack(CallNumberInterface callBack) {
        /*获取文本框的信息,当然你也可以传其他类型的参数,看需求咯*/
        this.callBack = callBack;
    }

}
