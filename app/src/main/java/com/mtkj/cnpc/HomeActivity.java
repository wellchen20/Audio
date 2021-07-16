package com.mtkj.cnpc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * 登录选择界面
 * @author zhangyazhou
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    Intent intent;
    String name = "";
    String password = "";
    int type = -1;
    boolean islogin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Button threePartyAccountNumber = findViewById(R.id.bt_third_party_account);
        Button xylinkAccountNumber = findViewById(R.id.bt_xylink_account);
        Button xylinkThirdAuth = findViewById(R.id.bt_xylink_third_auth);
        Button bt_into_Intro = findViewById(R.id.bt_into_Intro);

        threePartyAccountNumber.setOnClickListener(this);
        xylinkAccountNumber.setOnClickListener(this);
        xylinkThirdAuth.setOnClickListener(this);
        bt_into_Intro.setOnClickListener(this);
        if (getDatas()){
            Intent loginIntent =new Intent(HomeActivity.this, LoginActivity.class);
            switch (type){
                case 0:
                    loginIntent.putExtra(LoginActivity.LOGIN_TYPE_KEY, LoginActivity.LOGIN_TYPE_EXTERNAL);
                    loginIntent.putExtra("name",name);
                    loginIntent.putExtra("password",password);
                    loginIntent.putExtra("islogin",true);
                    startActivity(loginIntent);
                    finish();
                    break;
                case 1:
                    loginIntent.putExtra(LoginActivity.LOGIN_TYPE_KEY, LoginActivity.LOGIN_TYPE_XYLINK);
                    loginIntent.putExtra("name",name);
                    loginIntent.putExtra("password",password);
                    loginIntent.putExtra("islogin",true);
                    startActivity(loginIntent);
                    finish();
                    break;
            }
        }
    }



    private boolean getDatas() {
        SharedPreferences sp=getSharedPreferences("xytest", Context.MODE_PRIVATE);
        //第一个参数是键名，第二个是默认值
        name=sp.getString("name", "");
        password = sp.getString("password","");
        type = sp.getInt("type",-1);
        boolean flag = sp.getBoolean("flag",false);
        return flag;
    }

    @Override
    public void onClick(View view) {
        intent = new Intent(this, LoginActivity.class);
        switch (view.getId()) {
            case R.id.bt_third_party_account:
                intent.putExtra(LoginActivity.LOGIN_TYPE_KEY, LoginActivity.LOGIN_TYPE_EXTERNAL);
                intent.putExtra("islogin",false);
                startActivity(intent);
                break;
            case R.id.bt_xylink_account:
                intent.putExtra(LoginActivity.LOGIN_TYPE_KEY, LoginActivity.LOGIN_TYPE_XYLINK);
                intent.putExtra("islogin",false);
                startActivity(intent);
                break;
            case R.id.bt_xylink_third_auth:
                intent.putExtra(LoginActivity.LOGIN_TYPE_KEY, LoginActivity.LOGIN_TYPE_THIRD_AUTH);
                intent.putExtra("islogin",false);
                startActivity(intent);
                break;
            case R.id.bt_into_Intro:
                intent = new Intent(this,IntroductionActivity.class);
                startActivity(intent);
            default:
                break;

        }
    }

    public void onSetClick(View view) {
        Intent intent = new Intent(this, SetServerActivity.class);
        startActivity(intent);
    }
}
