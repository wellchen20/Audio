package com.mtkj.cnpc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ainemo.sdk.otf.NemoSDK;
import com.mtkj.cnpc.utils.AlertUtil;

/**
 * 问题反馈界面
 * @author zhangyazhou
 */
public class FeedbackActivity extends AppCompatActivity {

    private Button mSendFeedbackButton;
    private EditText feedbackEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mSendFeedbackButton = findViewById(R.id.send_feedback_bt);
        LinearLayout mFeedBackLayout = findViewById(R.id.action_layout);
        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (feedbackEditText.getText().length() > 0) {
                    mSendFeedbackButton.setEnabled(true);
                } else {
                    mSendFeedbackButton.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        feedbackEditText = findViewById(R.id.FeedbackEditText);
        feedbackEditText.addTextChangedListener(textWatcher);

        mSendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFeedback();
            }
        });

        mSendFeedbackButton.setEnabled(false);

        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setTitle(R.string.feedback);

        mFeedBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void sendFeedback() {
        Log.i("TAG", "sendFeedback Android_feedback_=" + feedbackEditText.getText().toString());
        NemoSDK.getInstance().sendFeedbackLog(feedbackEditText.getText().toString());
        AlertUtil.toastText(R.string.feedback_success);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
