package com.mtkj.cnpc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class IntroductionActivity extends AppCompatActivity {

    LinearLayout ll_meeting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);
        ll_meeting = findViewById(R.id.ll_meeting);
        ll_meeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(IntroductionActivity.this,MeetingListActivity.class));
            }
        });
    }
}
