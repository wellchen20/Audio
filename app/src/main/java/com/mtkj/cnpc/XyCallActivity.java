package com.mtkj.cnpc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.log.L;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringDef;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.module.call.data.Enums;
import com.ainemo.module.call.data.FECCCommand;
import com.ainemo.module.call.data.RemoteUri;
import com.ainemo.sdk.model.AIParam;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.NemoSDKListener;
import com.ainemo.sdk.otf.Orientation;
import com.ainemo.sdk.otf.RecordCallback;
import com.ainemo.sdk.otf.VideoInfo;
import com.ainemo.sdk.otf.WhiteboardChangeListener;
import com.ainemo.shared.UserActionListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mtkj.cnpc.bean.MeettingInfoData;
import com.mtkj.cnpc.dao.MeettingDao;
import com.mtkj.cnpc.face.FaceView;
import com.mtkj.cnpc.share.SharingValues;
import com.mtkj.cnpc.share.picture.CirclePageIndicator;
import com.mtkj.cnpc.share.picture.Glide4Engine;
import com.mtkj.cnpc.share.picture.PictureFragment;
import com.mtkj.cnpc.share.picture.PicturePagerAdapter;
import com.mtkj.cnpc.share.screen.ScreenPresenter;
import com.mtkj.cnpc.share.whiteboard.view.WhiteBoardCell;
import com.mtkj.cnpc.utils.ActivityUtils;
import com.mtkj.cnpc.utils.CommonTime;
import com.mtkj.cnpc.utils.GalleryLayoutBuilder;
import com.mtkj.cnpc.utils.Global;
import com.mtkj.cnpc.utils.LayoutMode;
import com.mtkj.cnpc.utils.SpeakerLayoutBuilder;
import com.mtkj.cnpc.utils.TextUtils;
import com.mtkj.cnpc.utils.VolumeManager;
import com.mtkj.cnpc.uvc.UVCCameraPresenter;
import com.mtkj.cnpc.view.CustomAlertDialog;
import com.mtkj.cnpc.view.Dtmf;
import com.mtkj.cnpc.view.FeccBar;
import com.mtkj.cnpc.view.GalleryVideoView;
import com.mtkj.cnpc.view.SpeakerVideoGroup;
import com.mtkj.cnpc.view.VideoCell;
import com.mtkj.cnpc.view.VideoCellLayout;
import com.rokid.appmonitor.IAppMonitor;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import vulture.module.call.nativemedia.NativeDataSourceManager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * ????????????demo:
 * ???????????????????????????, ????????????, ??????, ????????????, ????????????, ????????????????????????, ????????????????????????????????????????????????
 * <p>
 * ??????????????????, ??????????????????????????????, ???CallPresenter(????????????), ScreenPresenter(????????????)???????????????,
 * {@link XyCallPresenter#start()} ????????????, XyCallPresenter??????????????????{@link XyCallActivity}????????????
 * <p>
 * Note: ????????????: ??????, ??????, ???????????????????????????, ?????????????????????content, ????????????????????????????????????????????????????????????
 * ???????????????????????????????????????????????????????????????. demo?????????????????????, ???????????????????????????, ???????????????.
 * <p>
 * ???????????????????????? <>http://openapi.xylink.com/android/</>
 *
 */
public class XyCallActivity extends AppCompatActivity implements View.OnClickListener, XyCallContract.View {
    private static final String TAG = "XyCallActivity";
    private XyCallContract.Presenter callPresenter;
    private View viewToolbar;
    private SpeakerVideoGroup mVideoView;
    private GalleryVideoView mGalleryVideoView;
    private ImageView ivNetworkState; // ??????
    private TextView tvCallDuration; // ????????????
    private TextView toolbarCallNumber; // ??????
    private ImageButton ibDropCall; // ??????
    private ImageButton btMore; // ??????
    private ImageButton btStartRecord; // ??????
    private ImageButton btAudioOnly; // ????????????
    private TextView tvAudioOnly; // ????????????
    private ImageButton btMuteMic; // ??????
    private TextView tvMuteMic; // ??????
    private ImageButton btCloseVideo; // ????????????
    private TextView tvCloseVideo; // ????????????
    private LinearLayoutCompat llMoreDialog; // ??????dialog
    private TextView tvKeyboared; // ??????
    private TextView tvSwithcLayout; // ????????????
    private TextView tvClosePip; // ???????????????
    private TextView tvWhiteboard; // ??????
    private TextView tvShareScreen; // ????????????
    private TextView tvSharePhoto; // ????????????
    private LinearLayout llRecording;
    private TextView tvRecordingDuration; // ????????????
    private LinearLayout llLockPeople; // ???????????????
    private LinearLayout llSwitchCamera; // ???????????????
    private ImageButton btSwitchCamera; // ???????????????
    private View whiteboardLaodingView;
    private View shareScreenView;
    private View volumeView; // ???????????????
    private View viewInvite; // ???????????????
    private TextView tvInviteNumber; // ???????????????
    private View viewCallDetail; // ??????/????????????UI
    private TextView tvCallNumber; // number
    private ImageButton btCallAccept; // ????????????
    private ViewPager pagerPicture; // ????????????
    private CirclePageIndicator pageIndicator;
    private ImageView ivRecordStatus;
    private TextView tvStartRecord;
    private FeccBar feccBar;
    private View dtmfLayout;
    private Dtmf dtmf;

    private boolean isToolbarShowing = false; // toolbar????????????
    private boolean audioMode = false;
    private boolean isMuteBtnEnable = true;
    private String muteStatus = null;
    private boolean defaultCameraFront = false; // ?????????????????????
    private boolean isVideoMute = false;
    private boolean isStartRecording = true;
    private boolean isShowingPip = true;
    private boolean isSharePicture = false;
    private int inviteCallIndex = -1;
    private LayoutMode layoutMode = LayoutMode.MODE_SPEAKER;
    private VideoInfo fullVideoInfo;
    private boolean isCallStart;
    private List<VideoInfo> mRemoteVideoInfos;

    private static final int sDefaultTimeout = 5000;
    private Handler handler = new Handler();

    private CompositeDisposable compositeDisposable;
    private VolumeManager mVolumeManager;

    // ??????????????????
    private OrientationEventListener orientationEventListener;
    private boolean enableScreenOrientation = false;

    // share screen
    private ScreenPresenter screenPresenter;
    private static final int REQUEST_CODE_CHOOSE = 23;

    // ????????????
    private PicturePagerAdapter picturePagerAdapter;
    private List<String> picturePaths;
    private String outgoingNumber;

    // uvc
    private boolean isNeedUVC = false;//??????????????? ?????????
    private UVCCameraPresenter uvcCameraPresenter;


    XyReceiver receiver;
    IntentFilter filter;
    private static final String ACTION_TOPAPP_CHANGE_STATUS = "com.rokid.action.top.app.change.status";
    boolean active = true;
    @StringDef({
            MuteStatus.HAND_UP, MuteStatus.HAND_DOWN, MuteStatus.END_SPEACH
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface MuteStatus {
        String HAND_UP = "HAND_UP";
        String HAND_DOWN = "HAND_DOWN";
        String END_SPEACH = "END_SPEACH";
    }

    @IntDef({
            VideoStatus.VIDEO_STATUS_NORMAL, VideoStatus.VIDEO_STATUS_LOW_AS_LOCAL_BW,
            VideoStatus.VIDEO_STATUS_LOW_AS_LOCAL_HARDWARE, VideoStatus.VIDEO_STATUS_LOW_AS_REMOTE,
            VideoStatus.VIDEO_STATUS_NETWORK_ERROR, VideoStatus.VIDEO_STATUS_LOCAL_WIFI_ISSUE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface VideoStatus {
        int VIDEO_STATUS_NORMAL = 0;
        int VIDEO_STATUS_LOW_AS_LOCAL_BW = 1;
        int VIDEO_STATUS_LOW_AS_LOCAL_HARDWARE = 2;
        int VIDEO_STATUS_LOW_AS_REMOTE = 3;
        int VIDEO_STATUS_NETWORK_ERROR = 4;
        int VIDEO_STATUS_LOCAL_WIFI_ISSUE = 5;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_call);
        new XyCallPresenter(this); // init presenter
        compositeDisposable = new CompositeDisposable();
        initView();
        initListener();
        initData();
        initReceiver();
        //????????????
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mVideoView.setLandscape(true);
        NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateMuteStatus(true);
            }
        },1000);
    }

    private void initReceiver() {
        receiver = new XyReceiver();
        filter = new IntentFilter();
        filter.addAction(Global.EXIT_APP);
        filter.addAction(Global.HUNG_UP_MEETTING);
        filter.addAction(Global.OPEN_VIDEO);
        filter.addAction(Global.CLOSE_VIDEO);
        filter.addAction(Global.OPEN_MIKE);
        filter.addAction(Global.CLOSE_MIKE);
        filter.addAction(Global.START_RECORDING);
        filter.addAction(Global.STOP_RECORDING);
        filter.addAction(Global.SWITCH_LAYOUT);
        filter.addAction(Global.HOLD_ON);
        filter.addAction(Global.HANG_UP);
        registerReceiver(receiver,filter);
    }

    @Override
    public void setPresenter(XyCallContract.Presenter presenter) {
        callPresenter = presenter;
    }

    @Override
    protected void onStart() {
        super.onStart();
        callPresenter.start(); // Note: business start here,??????????????????
        if (uvcCameraPresenter != null) {
            uvcCameraPresenter.onStart();
        }
        defaultCameraFront = NemoSDK.defaultCameraId() == 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume " );
        mVideoView.startRender();
        mGalleryVideoView.startRender();
        if (screenPresenter != null) {
            screenPresenter.hideFloatView();
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        mGalleryVideoView.pauseRender();
        mVideoView.pauseRender();
        Log.e(TAG, "onPause " );
//        NemoSDK.getInstance().hangup();
//        NemoSDK.getInstance().releaseLayout();
//        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop " );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && screenPresenter != null && screenPresenter.isSharingScreen()) {
            screenPresenter.onStop();
        }
        if (uvcCameraPresenter != null) {
            uvcCameraPresenter.onStop();
        }
    }

    @Override
    public void onBackPressed() {
        // Intercept back event
    }

    // remember to release resource when destroy

    @Override
    public void onDestroy() {
        L.e(TAG, "on destroy");
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        if (screenPresenter != null) {
            screenPresenter.onDestroy();
        }
        if (mMediaPlayer!=null){
            mMediaPlayer.release();//????????????
        }
        mVideoView.destroy();
        NemoSDK.getInstance().releaseCamera();
        orientationEventListener.disable();
        pictureData = null;
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void initView() {
        viewToolbar = findViewById(R.id.group_visibility);
        mVideoView = findViewById(R.id.speak_video_view);
        mGalleryVideoView = findViewById(R.id.gallery_video_view);
        ivNetworkState = findViewById(R.id.network_state);
        tvCallDuration = findViewById(R.id.network_state_timer);
        toolbarCallNumber = findViewById(R.id.tv_call_number);
        ibDropCall = findViewById(R.id.drop_call);
        btMore = findViewById(R.id.hold_meeting_more);
        btStartRecord = findViewById(R.id.start_record_video);
        tvStartRecord = findViewById(R.id.record_video_text);
        btAudioOnly = findViewById(R.id.audio_only_btn);
        tvAudioOnly = findViewById(R.id.audio_only_text);
        btMuteMic = findViewById(R.id.mute_mic_btn);
        tvMuteMic = findViewById(R.id.mute_mic_btn_label);
        btCloseVideo = findViewById(R.id.close_video);
        tvCloseVideo = findViewById(R.id.video_mute_text);
        llMoreDialog = findViewById(R.id.more_layout_dialog);
        tvKeyboared = findViewById(R.id.keyboard);
        tvSwithcLayout = findViewById(R.id.switch_layout);
        tvClosePip = findViewById(R.id.textView2);
        tvWhiteboard = findViewById(R.id.tv_whiteboard);
        tvShareScreen = findViewById(R.id.tv_share_screen);
        tvSharePhoto = findViewById(R.id.tv_share_photo);
        ivRecordStatus = findViewById(R.id.video_recording_icon);
        llRecording = findViewById(R.id.conversation_recording_layout);
        tvRecordingDuration = findViewById(R.id.video_recording_timer);
        llLockPeople = findViewById(R.id.layout_lock_people);
        llSwitchCamera = findViewById(R.id.switch_camera_layout);
        btSwitchCamera = findViewById(R.id.switch_camera);
        whiteboardLaodingView = findViewById(R.id.view_whiteboard_loading);
        shareScreenView = findViewById(R.id.share_screen);
        volumeView = findViewById(R.id.operation_volume_brightness);
        // ???????????????
        viewInvite = findViewById(R.id.view_call_invite);
        viewInvite.findViewById(R.id.bt_invite_accept).setOnClickListener(this);
        viewInvite.findViewById(R.id.bt_invite_drop).setOnClickListener(this);
        tvInviteNumber = viewInvite.findViewById(R.id.tv_invite_number);
        // ??????/??????UI
        viewCallDetail = findViewById(R.id.view_call_detail);
        viewCallDetail.findViewById(R.id.bt_call_drop).setOnClickListener(this);
        btCallAccept = viewCallDetail.findViewById(R.id.bt_call_accept);
        tvCallNumber = viewCallDetail.findViewById(R.id.tv_call_name);
        // ????????????
        pagerPicture = findViewById(R.id.pager_picture);
        pageIndicator = findViewById(R.id.pager_indicator);
        //FECC
        feccBar = findViewById(R.id.fecc_bar);
        feccBar.setFeccListener(new FeccActionListener());
        // ??????
        dtmfLayout = findViewById(R.id.dtmf);
    }

    private void initListener() {
        ibDropCall.setOnClickListener(this);
        btCallAccept.setOnClickListener(this);
        btStartRecord.setOnClickListener(this);
        btAudioOnly.setOnClickListener(this);
        btMuteMic.setOnClickListener(this);
        btCloseVideo.setOnClickListener(this);
        btMore.setOnClickListener(this);
        tvKeyboared.setOnClickListener(this);
        tvSwithcLayout.setOnClickListener(this);
        tvClosePip.setOnClickListener(this);
        tvWhiteboard.setOnClickListener(this);
        tvShareScreen.setOnClickListener(this);
        tvSharePhoto.setOnClickListener(this);
        llLockPeople.setOnClickListener(this);
        btSwitchCamera.setOnClickListener(this);
        feccBar.initFeccEventListeners();
    }

    private void initData() {
        // ??????: 1+N
        mVideoView.setLocalVideoInfo(buildLocalLayoutInfo());
        mVideoView.setOnVideoCellListener(videoCellListener);
        mVideoView.setShowingPip(isShowingPip);
        // ??????: ??? ??? ???
        mGalleryVideoView.setLocalVideoInfo(buildLocalLayoutInfo());
        mGalleryVideoView.setOnVideoCellListener(galleryVideoCellListener);

        // ??????
        dtmf = new Dtmf(dtmfLayout, new Dtmf.DtmfListener() {
            @Override
            public void onDtmfKey(String key) {
                if (buildLocalLayoutInfo() != null) {
                    if (mRemoteVideoInfos != null && mRemoteVideoInfos.size() > 0) {
                        NemoSDK.getInstance().sendDtmf(mRemoteVideoInfos.get(0).getRemoteID(), key);
                    }
                }
            }
        });


        // ?????? & ??????
        Intent intent = getIntent();
        boolean isIncomingCall = intent.getBooleanExtra("isIncomingCall", false);
        if (isIncomingCall) {
            final int callIndex = intent.getIntExtra("callIndex", -1);
            inviteCallIndex = callIndex;
            String callerName = intent.getStringExtra("callerName");
            String callNumber = intent.getStringExtra("callerNumber");
            toolbarCallNumber.setText(callNumber);
            Log.i(TAG, "showIncomingCallDialog=" + callIndex);
            showCallIncoming(callIndex, callNumber, callerName);
            playMusic(true);
        } else {
            outgoingNumber = intent.getStringExtra("number");
            showCallOutGoing(outgoingNumber);
            L.i(TAG, "outgoing number: " + outgoingNumber);
        }

        mVolumeManager = new VolumeManager(this, volumeView, AudioManager.STREAM_VOICE_CALL);
        mVolumeManager.setMuteCallback(new VolumeManager.MuteCallback() {
            @Override
            public void muteChanged(boolean mute) {
                NemoSDK.getInstance().setSpeakerMute(mute);
            }
        });
        // ??????????????????(??????????????????, ???????????????????????????????????????)
        NemoSDK.getInstance().registerWhiteboardChangeListener(whiteboardChangeListener);

        // ???????????????, ????????????????????????????????? ,  enableScreenOrientation = false
        orientationEventListener = new YourOrientationEventListener(XyCallActivity.this);
        orientationEventListener.enable();
        enableScreenOrientation = true;

        // add for: uvc, ???????????????????????????
        if (isNeedUVC) {
            uvcCameraPresenter = new UVCCameraPresenter(this);
        }
        NemoSDK.getInstance().switchCamera(0);
//        updateMuteStatus(true);
    }

    private void hideOrShowToolbar(boolean show) {
        if (show) {
            hideToolbar();
        } else {
            showToolbar(sDefaultTimeout);
        }
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hideToolbar();
        }
    };

    private void hideToolbar() {
        viewToolbar.setVisibility(GONE);
        llSwitchCamera.setVisibility(GONE);
        isToolbarShowing = false;
        llMoreDialog.setVisibility(GONE);
        feccBar.setVisibility(GONE);
    }

    private void showToolbar(int timeout) {
        if (!isToolbarShowing) { // show toolbar
            viewToolbar.setVisibility(View.VISIBLE);
            llSwitchCamera.setVisibility(View.VISIBLE);
            isToolbarShowing = true;
            // fecc
            feccBar.setVisibility(VISIBLE);
            updateFeccStatus();
        }
        if (timeout != 0) {
            handler.removeCallbacks(mFadeOut);
            handler.postDelayed(mFadeOut, timeout);
        }
    }

    // ????????????
    private void initCallDuration() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
        compositeDisposable.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        tvCallDuration.setText(CommonTime.formatTime(aLong));
                    }
                }));
    }

    private void checkPip() {
        setShowingPip(!isShowingPip());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drop_call:
            case R.id.bt_call_drop:
                NemoSDK.getInstance().hangup();
                NemoSDK.getInstance().releaseLayout();
                finish();
                break;
            case R.id.bt_call_accept:
                L.i(TAG, "inviteCallIndex::: " + inviteCallIndex);
                NemoSDK.getInstance().answerCall(inviteCallIndex, true);
                break;
            case R.id.hold_meeting_more:
                if (layoutMode == LayoutMode.MODE_GALLERY) {
                    tvKeyboared.setVisibility(GONE);
                    tvSwithcLayout.setVisibility(VISIBLE);
                    tvClosePip.setVisibility(GONE);
                    tvWhiteboard.setVisibility(GONE);
                    tvShareScreen.setVisibility(GONE);
                    tvSharePhoto.setVisibility(GONE);
                } else {
                    tvKeyboared.setVisibility(VISIBLE);
                    tvSwithcLayout.setVisibility(VISIBLE);
                    tvClosePip.setVisibility(VISIBLE);
                    tvWhiteboard.setVisibility(VISIBLE);
                    tvShareScreen.setVisibility(VISIBLE);
                    tvSharePhoto.setVisibility(VISIBLE);
                }
                tvWhiteboard.setText(SpeakerVideoGroup.isShowingWhiteboard() ? "????????????" : "????????????");
                boolean isClosePipEnable = mVideoView.isLandscape() && mRemoteVideoInfos != null && mRemoteVideoInfos.size() > 0;
                tvClosePip.setEnabled(isClosePipEnable);
                tvClosePip.setTextColor(isClosePipEnable ? Color.WHITE : Color.GRAY);
                llMoreDialog.setVisibility(llMoreDialog.getVisibility() == View.VISIBLE ? GONE : View.VISIBLE);
                break;
            case R.id.start_record_video:
                L.i(TAG, "is recording: " + isStartRecording);
                if (NemoSDK.getInstance().isAuthorize()) {
                    setRecordVideo(isStartRecording);
                    isStartRecording = !isStartRecording;
                } else {
                    Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.audio_only_btn:
                audioMode = !audioMode;
                setSwitchCallState(audioMode);
                NemoSDK.getInstance().switchCallMode(audioMode);
                break;
            case R.id.mute_mic_btn:
                if (isMuteBtnEnable) {
                    updateMuteStatus(!NemoSDK.getInstance().isMicMuted());
                } else {
                    // ??????/????????????/????????????
                    switch (muteStatus) {
                        case MuteStatus.HAND_UP:
                            NemoSDK.getInstance().handUp();
                            muteStatus = MuteStatus.HAND_DOWN;
                            btMuteMic.setImageResource(R.mipmap.ic_toolbar_handdown);
                            tvMuteMic.setText("????????????");
                            break;
                        case MuteStatus.HAND_DOWN:
                            NemoSDK.getInstance().handDown();
                            muteStatus = MuteStatus.HAND_UP;
                            btMuteMic.setImageResource(R.mipmap.ic_toolbar_hand_up);
                            tvMuteMic.setText("????????????");
                            break;
                        case MuteStatus.END_SPEACH:
                            NemoSDK.getInstance().endSpeech();
                            muteStatus = MuteStatus.HAND_UP;
                            btMuteMic.setImageResource(R.mipmap.ic_toolbar_hand_up);
                            tvMuteMic.setText("????????????");
                            break;
                    }
                }
                break;
            case R.id.close_video:
                isVideoMute = !isVideoMute;
                NemoSDK.getInstance().setVideoMute(isVideoMute);
                setVideoState(isVideoMute);
                break;
            case R.id.keyboard:
                llMoreDialog.setVisibility(GONE);
                dtmfLayout.setVisibility(VISIBLE);
                break;
            case R.id.switch_layout:
                llMoreDialog.setVisibility(GONE);
                layoutMode = layoutMode == LayoutMode.MODE_SPEAKER ? LayoutMode.MODE_GALLERY : LayoutMode.MODE_SPEAKER;
                switchLayout();
                break;
            case R.id.textView2:
                llMoreDialog.setVisibility(GONE);
                tvClosePip.setText(isShowingPip() ? "????????????" : "????????????");
                checkPip();
                break;
            case R.id.tv_whiteboard:
                llMoreDialog.setVisibility(GONE);
                if (SpeakerVideoGroup.isShowingWhiteboard()) {
                    new CustomAlertDialog(XyCallActivity.this).builder()
                            .setTitle(getString(R.string.exit_white_board_title))
                            .setMsg(getString(R.string.exit_white_board_content))
                            .setPositiveButton(getString(R.string.sure), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    NemoSDK.getInstance().stopWhiteboard();
                                    stopWhiteboardView();
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            })
                            .setCancelable(false).show();
                } else {
                    whiteboardLaodingView.setVisibility(VISIBLE);
                    NemoSDK.getInstance().startWhiteboard();
                    L.i("wang ????????????");
                }
                break;
            case R.id.tv_share_screen:
                llMoreDialog.setVisibility(GONE);
                if (screenPresenter != null && screenPresenter.isSharingScreen()) {
                    NemoSDK.getInstance().dualStreamStop(SharingValues.TYPE_SHARE_SCREEN);
                } else {
                    // ????????????presenter
                    screenPresenter = new ScreenPresenter(XyCallActivity.this);
                    screenPresenter.startShare();
                }
                break;
            case R.id.tv_share_photo:
                llMoreDialog.setVisibility(GONE);
                if (isSharePicture) { // ??????????????????, Note: remove pictureHandler
                    NemoSDK.getInstance().dualStreamStop(SharingValues.TYPE_SHARE_PICUTRE);
                } else {
                    Matisse.from(XyCallActivity.this)
                            .choose(MimeType.of(MimeType.PNG, MimeType.GIF, MimeType.JPEG), false)
                            .countable(true)
                            .maxSelectable(9)
                            .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            .thumbnailScale(0.85f)
                            .imageEngine(new Glide4Engine())
                            .forResult(REQUEST_CODE_CHOOSE);
                }
                break;
            case R.id.layout_lock_people:
                llMoreDialog.setVisibility(GONE);
                mVideoView.unlockLayout();
                llLockPeople.setVisibility(GONE);
                break;
            case R.id.switch_camera:
                if (uvcCameraPresenter != null && uvcCameraPresenter.hasUvcCamera()) {
                    uvcCameraPresenter.switchCamera();
                } else {
                    NemoSDK.getInstance().switchCamera(defaultCameraFront ? 0 : 1);  // 0????????? 1?????????
                    defaultCameraFront = !defaultCameraFront;
                }
                break;
            case R.id.bt_invite_accept: // ?????????????????????
                L.i(TAG, "wang invite accept");
                NemoSDK.getInstance().answerCall(inviteCallIndex, true);
                viewInvite.setVisibility(GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateMuteStatus(true);
                    }
                },1000);
                break;
            case R.id.bt_invite_drop: // ?????????????????????
                L.i(TAG, "wang invite drop");
                NemoSDK.getInstance().answerCall(inviteCallIndex, false);
                viewInvite.setVisibility(GONE);
                break;
            case R.id.pager_picture:
                L.i(TAG, "wang pager clicked");
                hideOrShowToolbar(isToolbarShowing);
                break;
        }
    }

    //????????????????????????
    private void setVideoState(boolean videoMute) {
        mVideoView.setMuteLocalVideo(videoMute, getString(R.string.call_video_mute));
        mGalleryVideoView.setMuteLocalVideo(videoMute, getString(R.string.call_video_mute));
        if (videoMute) {
            btCloseVideo.setImageResource(R.mipmap.ic_toolbar_camera);
            tvCloseVideo.setText(getResources().getString(R.string.open_video));
        } else {
            btCloseVideo.setImageResource(R.mipmap.ic_toolbar_camera_muted);
            tvCloseVideo.setText(getResources().getString(R.string.close_video));
        }
    }

    public void setRecordVideo(boolean isStartRecording) {
        if (isStartRecording) {
            NemoSDK.getInstance().startRecord(outgoingNumber, new RecordCallback() {
                @Override
                public void onFailed(final int errorCode) {
                    Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            Toast.makeText(XyCallActivity.this, "Record fail: " + errorCode, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onSuccess() {
                    Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            showRecordStatusNotification(true, NemoSDK.getInstance().getUserName(), true);
                        }
                    });
                }
            });
        } else {
            NemoSDK.getInstance().stopRecord();
            showRecordStatusNotification(false, NemoSDK.getInstance().getUserName(), true);
            Toast.makeText(XyCallActivity.this, getString(R.string.third_conf_record_notice), Toast.LENGTH_LONG).show();
        }
    }

    // ??????
    @Override
    public void showCallOutGoing(String outgoingNumber) {
        viewCallDetail.setVisibility(VISIBLE);
        btCallAccept.setVisibility(GONE);
        L.i(TAG, "showCallOutGoing callNumber: " + outgoingNumber);
        tvCallNumber.setText(outgoingNumber);
        toolbarCallNumber.setText(outgoingNumber);
    }

    // ??????
    @Override
    public void showCallIncoming(int callIndex, String callNumber, String callName) {
        viewCallDetail.setVisibility(VISIBLE);
        tvCallNumber.setText(!TextUtils.isEmpty(callName) ? callName : callNumber);
        btCallAccept.setVisibility(VISIBLE);
    }

    @Override
    public void showCallDisconnected(String reason) {
        if ("CANCEL".equals(reason)) {
            Toast.makeText(this, "call canceled", Toast.LENGTH_SHORT).show();
        }
        if ("BUSY".equals(reason)) {
            Toast.makeText(this, "the side is busy, please call later", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    /**
     * ????????????, ?????? ??????toolbar???
     */
    @Override
    public void showCallConnected() {
        isCallStart = true;
        viewCallDetail.setVisibility(GONE);
        initCallDuration();
        showToolbar(sDefaultTimeout);
    }

    @Override
    public void showVideoDataSourceChange(List<VideoInfo> videoInfos, boolean hasVideoContent) {
        L.i(TAG, "showVideoDataSourceChange: " + videoInfos);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // case-fix: ????????????????????????, ??????content???APP????????????
            if (hasVideoContent && !ActivityUtils.isAppForeground(this)
                    && !(screenPresenter != null && screenPresenter.isSharingScreen())) {
                ActivityUtils.moveTaskToFront(this);
            }
        }
        mRemoteVideoInfos = videoInfos;
        mVideoView.setRemoteVideoInfos(videoInfos);
        mGalleryVideoView.setRemoteVideoInfos(videoInfos);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mVolumeManager.onVolumeDown();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mVolumeManager.onVolumeUp();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * ??????????????????
     * ?????????????????????????????????
     * ?????????????????????????????????????????????????????????
     *
     * @param operation        ?????????mute/unmute
     * @param isMuteIsDisabled ????????????????????? true????????????
     */
    @Override
    public void showConfMgmtStateChanged(String operation, boolean isMuteIsDisabled) {
        isMuteBtnEnable = !isMuteIsDisabled;
        if ("mute".equalsIgnoreCase(operation)) {
            NemoSDK.getInstance().enableMic(true, isMuteIsDisabled);
            if (isMuteIsDisabled) {
                // ????????????
                Toast.makeText(XyCallActivity.this, "?????????????????????, ???????????????", Toast.LENGTH_LONG).show();
                muteStatus = MuteStatus.HAND_UP;
                btMuteMic.setImageResource(R.mipmap.ic_toolbar_hand_up);
                tvMuteMic.setText("????????????");
            } else {
                Toast.makeText(XyCallActivity.this, "???????????????", Toast.LENGTH_LONG).show();
                btMuteMic.setImageResource(R.mipmap.ic_toolbar_mic_muted);
                tvMuteMic.setText("????????????");
            }
            if (mVideoView != null) {
                mVideoView.setMuteLocalAudio(true);
            }
            if (mGalleryVideoView != null) {
                mGalleryVideoView.setMuteLocalAudio(true);
            }
        } else if ("unmute".equalsIgnoreCase(operation)) {
            NemoSDK.getInstance().enableMic(false, false);
            if (isMuteIsDisabled) {
                muteStatus = MuteStatus.END_SPEACH;
                btMuteMic.setImageResource(R.mipmap.ic_toolbar_end_speech);
                tvMuteMic.setText("????????????");
            } else {
                btMuteMic.setImageResource(R.mipmap.ic_toolbar_mic);
                tvMuteMic.setText("??????");
            }
            if (mVideoView != null) {
                mVideoView.setMuteLocalAudio(false);
            }
            if (mGalleryVideoView != null) {
                mGalleryVideoView.setMuteLocalAudio(false);
            }
        }
    }

    @Override
    public void showKickout(int code, final String reason) {
        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                NemoSDK.getInstance().hangup();
                NemoSDK.getInstance().releaseLayout();
                isDown = true;
                finish();
            }
        });
    }

    private void updateMuteStatus(boolean isMute) {
        NemoSDK.getInstance().enableMic(isMute, true);
        if (isMute) {
            btMuteMic.setImageResource(R.mipmap.ic_toolbar_mic_muted);
            tvMuteMic.setText("????????????");
            if (mVideoView != null) {
                mVideoView.setMuteLocalAudio(true);
            }
            if (mGalleryVideoView != null) {
                mGalleryVideoView.setMuteLocalAudio(true);
            }
        } else {
            btMuteMic.setImageResource(R.mipmap.ic_toolbar_mic);
            tvMuteMic.setText("??????");
            if (mVideoView != null) {
                mVideoView.setMuteLocalAudio(false);
            }
            if (mGalleryVideoView != null) {
                mGalleryVideoView.setMuteLocalAudio(false);
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param level 1???2???3???4?????????,???-???-???-???
     */
    @Override
    public void showNetLevel(int level) {
        if (ivNetworkState == null) {
            return;
        }
        switch (level) {
            case 4:
                ivNetworkState.setImageResource(R.drawable.network_state_four);
                break;
            case 3:
                ivNetworkState.setImageResource(R.drawable.network_state_three);
                break;
            case 2:
                ivNetworkState.setImageResource(R.drawable.network_state_two);
                break;
            case 1:
                ivNetworkState.setImageResource(R.drawable.network_state_one);
                break;
        }
    }

    @Override
    public void showVideoStatusChange(int videoStatus) {
        if (videoStatus == VideoStatus.VIDEO_STATUS_NORMAL) {
            Toast.makeText(XyCallActivity.this, "????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_LOW_AS_LOCAL_BW) {
            Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_LOW_AS_LOCAL_HARDWARE) {
            Toast.makeText(XyCallActivity.this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_LOW_AS_REMOTE) {
            Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_NETWORK_ERROR) {
            Toast.makeText(XyCallActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_LOCAL_WIFI_ISSUE) {
            Toast.makeText(XyCallActivity.this, "WiFi???????????????", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showIMNotification(String values) {
        if ("[]".equals(values)) {
            Toast.makeText(XyCallActivity.this, R.string.im_notification_ccs_transfer, Toast.LENGTH_SHORT).show();
        } else {
            String val = values.replace("[", "");
            val = val.replace("]", "");
            val = val.replace('"', ' ');
            val = val.replace('"', ' ');
            String str = String.format("%s%s%s", getResources().getString(R.string.queen_top_part), val, getResources().getString(R.string.queen_bottom_part));
            Toast.makeText(XyCallActivity.this, str, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void showAiFace(final AIParam aiParam, final boolean isLocalFace) {
        L.i(TAG, "aiParam:" + aiParam);
        if (aiParam == null || aiParam.getParticipantId() < 0) {
            return;
        }
        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                L.i(TAG, "fullVideoInfo:: " + fullVideoInfo.toString());
                L.i(TAG, "fullVideoInfo is Local:: " + isLocalFace);
                if (isLocalFace) {
                    callPresenter.dealLocalAiParam(aiParam, fullVideoInfo != null
                            && fullVideoInfo.getParticipantId() == NemoSDK.getInstance().getUserId());
                } else {
                    callPresenter.dealAiParam(aiParam, fullVideoInfo != null
                            && fullVideoInfo.getParticipantId() == aiParam.getParticipantId());
                }
            }
        });
    }

    /**
     * ???????????????laid
     *
     * @param callNumber
     * @param callName
     */
    @Override
    public void showInviteCall(int callIndex, String callNumber, String callName) {
        inviteCallIndex = callIndex;
        viewInvite.setVisibility(VISIBLE);
        toolbarCallNumber.setText(callNumber);
        tvInviteNumber.setText(TextUtils.isEmpty(callName) ? callNumber : callName);
    }

    public void hideInviteCall() {
        viewInvite.setVisibility(GONE);
    }

    @Override
    public void showRecordStatusNotification(boolean isStart, String displayName, boolean canStop) {
        Log.i(TAG, "showRecordStatusNotification: " + isStart);
        if (isStart) {
            Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setFillBefore(true);
            alphaAnimation.setInterpolator(new LinearInterpolator());
            alphaAnimation.setRepeatCount(Animation.INFINITE);
            alphaAnimation.setRepeatMode(Animation.REVERSE);
            llRecording.setVisibility(View.VISIBLE);
            ivRecordStatus.startAnimation(alphaAnimation);
            btStartRecord.setEnabled(canStop);
            tvRecordingDuration.setText(displayName + "????????????");
            btStartRecord.setImageResource(R.mipmap.ic_toolbar_recording_ing);
            tvStartRecord.setText(R.string.button_text_stop);
        } else {
            ivRecordStatus.clearAnimation();
            btStartRecord.setEnabled(true);
            llRecording.setVisibility(GONE);
            tvStartRecord.setText(R.string.button_text_record);
            btStartRecord.setImageResource(R.drawable.ic_toolbar_recording);
        }
    }

    //????????????
    private void setSwitchCallState(boolean audioMode) {
        mVideoView.setAudioOnlyMode(audioMode, isVideoMute);
        mGalleryVideoView.setAudioOnlyMode(audioMode, isVideoMute);
        if (audioMode) {
            btCloseVideo.setEnabled(false);
            btAudioOnly.setImageResource(R.mipmap.ic_toolbar_audio_only_pressed);
            tvAudioOnly.setText(R.string.close_switch_call_module);
        } else {
            btCloseVideo.setEnabled(true);
            tvAudioOnly.setText(R.string.switch_call_module);
            btAudioOnly.setImageResource(R.mipmap.ic_toolbar_audio_only);
        }
    }

    private VideoInfo buildLocalLayoutInfo() {
        VideoInfo li = new VideoInfo();
        li.setLayoutVideoState(Enums.LAYOUT_STATE_RECEIVED);
        li.setDataSourceID(NemoSDK.getLocalVideoStreamID());
        li.setRemoteName(NemoSDK.getInstance().getUserName());
        li.setParticipantId((int) NemoSDK.getInstance().getUserId());
        li.setRemoteID(RemoteUri.generateUri(String.valueOf(NemoSDK.getInstance().getUserId()), Enums.DEVICE_TYPE_SOFT));
        return li;
    }

    private VideoCellLayout.SimpleVideoCellListener galleryVideoCellListener = new VideoCellLayout.SimpleVideoCellListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
            L.i(TAG, "onSingleTapConfirmed, cell.layoutInfo : " + cell.getLayoutInfo());
            hideOrShowToolbar(isToolbarShowing);
            if (dtmfLayout.getVisibility() == VISIBLE) {
                dtmfLayout.setVisibility(GONE);
                dtmf.clearText();
            }
            return true;
        }
    };

    private VideoCellLayout.SimpleVideoCellListener videoCellListener = new VideoCellLayout.SimpleVideoCellListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, WhiteBoardCell cell) {
            L.i("wang whiteboard click");
            hideOrShowToolbar(isToolbarShowing);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
            L.e(TAG, "onSingleTapConfirmed, cell : " + cell.getLayoutInfo().getParticipantId());
            if (!SpeakerVideoGroup.isShowingWhiteboard() && isCallStart) {
                if (cell.isFullScreen() || cell.isLargeScreen()) {
                    hideOrShowToolbar(isToolbarShowing);
                } else if (mVideoView.isLandscape()) {
                    mVideoView.lockLayout(cell.getLayoutInfo().getParticipantId());
                    llLockPeople.setVisibility(VISIBLE);
                }
            }
            if (dtmfLayout.getVisibility() == VISIBLE) {
                dtmfLayout.setVisibility(GONE);
                dtmf.clearText();
            }
            return true;
        }

        @Override
        public void onFullScreenChanged(VideoCell cell) {
            if (cell != null) {
                fullVideoInfo = cell.getLayoutInfo();
            }
            if (uvcCameraPresenter != null) {
                uvcCameraPresenter.setLocalVideoCell(mVideoView.getLocalVideoCell());
            }
        }

        @Override
        public void onWhiteboardMessageSend(String text) {
            // send local draw data to remote client
            NemoSDK.getInstance().sendWhiteboardData(text);
        }

        @Override
        public void onVideoCellGroupClicked(View group) {
            hideOrShowToolbar(isToolbarShowing);
        }
    };

    /**
     * ???????????????????????????
     *
     * @param isShowingPip
     */
    public void setShowingPip(boolean isShowingPip) {
        this.isShowingPip = isShowingPip;
        if (mVideoView != null) {
            mVideoView.setShowingPip(isShowingPip);
        }
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    public boolean isShowingPip() {
        if (mVideoView != null) {
            return mVideoView.isShowingPip();
        }
        return true;
    }

    /**
     * ????????????
     */
    private void switchLayout() {
        L.i(TAG, "onVideoDataSourceChange is switchLayout, layoutMode : " + layoutMode);
        if (layoutMode == LayoutMode.MODE_SPEAKER) {
            mVideoView.setVisibility(VISIBLE);
            mGalleryVideoView.setVisibility(GONE);
            NemoSDK.getInstance().setLayoutBuilder(new SpeakerLayoutBuilder());
        } else {
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mVideoView.setLandscape(true);
                NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
            }
            mVideoView.setVisibility(GONE);
            mGalleryVideoView.setVisibility(VISIBLE);
            NemoSDK.getInstance().setLayoutBuilder(new GalleryLayoutBuilder());
        }
    }

    //=========================================================================================
    // face view
    //=========================================================================================
    @Override
    public void showFaceView(List<FaceView> faceViews) {
        mVideoView.showFaceView(faceViews);
    }

    @Override
    public Activity getCallActivity() {
        return this;
    }

    @Override
    public int[] getMainCellSize() {
        return new int[]{mVideoView.getWidth(), mVideoView.getHeight()};
    }

    //=========================================================================================
    // share picture demo: ????????????
    // NOTE: bitmap only support ARGB_8888
    //=========================================================================================
    private byte[] pictureData;
    private int width;
    private int height;
    private static final int MSG_SHARE_PICTURE = 6002;

    @SuppressLint("HandlerLeak")
    private Handler pictureHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SHARE_PICTURE) {
                String dataSourceId = NemoSDK.getInstance().getDataSourceId();
                if (!TextUtils.isEmpty(dataSourceId) && pictureData != null) {
                    L.i(TAG, "send data to remote: " + pictureData.length + " W. " + width + " h." + height);
                    NativeDataSourceManager.putContentData2(dataSourceId,
                            pictureData, pictureData.length, width, height, 0, 0, 0, true);
                }
                pictureHandler.sendEmptyMessageDelayed(MSG_SHARE_PICTURE, 200);
                // 9711360   wang x. 1080 y. 2029
            }
        }
    };

    private class MyPagerListener extends ViewPager.SimpleOnPageChangeListener {
        boolean first = true;

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            // start share
            L.i(TAG, "wang onPageSelected: " + position);
            if (picturePaths != null && picturePaths.size() > 0) {
                pictureHandler.removeMessages(MSG_SHARE_PICTURE);
                String picturePath = picturePaths.get(position);
                Glide.with(XyCallActivity.this).asBitmap().apply(new RequestOptions().override(1280, 720))
                        .load(picturePath).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Matrix matrix = new Matrix();
                        matrix.setScale(0.5f, 0.5f);
                        Bitmap bitmap = Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), resource.getHeight(), matrix, true);
                        if (bitmap != null) {
                            width = bitmap.getWidth();
                            height = bitmap.getHeight();
                            int byteCount = bitmap.getByteCount();
                            ByteBuffer b = ByteBuffer.allocate(byteCount);
                            bitmap.copyPixelsToBuffer(b);
                            pictureData = b.array();
                            pictureHandler.sendEmptyMessage(MSG_SHARE_PICTURE);
                            bitmap.recycle();
                        }
                    }
                });
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            L.i(TAG, "onPageScrolled:: " + first);
            if (first && positionOffset == 0 && positionOffsetPixels == 0) {
                onPageSelected(0);
                first = false;
            }
            hideToolbar();
        }
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param state
     */
    @Override
    public void updateSharePictures(NemoSDKListener.NemoDualState state) {
        if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STAT_IDLE) {
            pictureHandler.removeMessages(MSG_SHARE_PICTURE);
            pictureData = null;
            pagerPicture.setVisibility(GONE);
            pageIndicator.setVisibility(GONE);
            tvSharePhoto.setText("????????????");
            isSharePicture = false;
            // ?????? ??????enable=true
            tvWhiteboard.setTextColor(Color.WHITE);
            tvWhiteboard.setEnabled(true);
            tvShareScreen.setTextColor(Color.WHITE);
            tvShareScreen.setEnabled(true);
        } else if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STATE_RECEIVING) {
            picturePagerAdapter = new PicturePagerAdapter(getSupportFragmentManager());
            picturePagerAdapter.setOnPagerListener(new PictureFragment.OnPagerClickListener() {
                @Override
                public void onPagerClicked() {
                    hideOrShowToolbar(isToolbarShowing);
                }
            });
            pagerPicture.setAdapter(picturePagerAdapter);
            pageIndicator.setViewPager(pagerPicture);
            pageIndicator.setOnPageChangeListener(new MyPagerListener());
            picturePagerAdapter.setPicturePaths(picturePaths);
            picturePagerAdapter.notifyDataSetChanged();

            pageIndicator.setVisibility(VISIBLE);
            pagerPicture.setVisibility(VISIBLE);
            tvSharePhoto.setText("????????????");
            isSharePicture = true;
            // ?????? ??????enable=false
            tvWhiteboard.setTextColor(Color.GRAY);
            tvWhiteboard.setEnabled(false);
            tvShareScreen.setTextColor(Color.GRAY);
            tvShareScreen.setEnabled(false);
        } else if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STATE_NOBANDWIDTH) {
            Toast.makeText(this, "????????????, ???????????????, ????????????", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "??????, ????????????", Toast.LENGTH_SHORT).show();
        }
    }

    //=========================================================================================
    // share screen demo: ????????????????????????????????????????????????
    //=========================================================================================
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (SharingValues.REQUEST_SHARE_SCREEN == requestCode) {
            if (resultCode == RESULT_OK) {
                if (screenPresenter != null) {
                    screenPresenter.onResult(requestCode, resultCode, intent);
                }
            } else {
                // user did not grant permissions
                Toast.makeText(XyCallActivity.this, "share screen cancel", Toast.LENGTH_LONG).show();
            }
        } else if (SharingValues.REQUEST_FLOAT_PERMISSION == requestCode) {
            // home screen float view
            if (resultCode == RESULT_OK) {
                if (screenPresenter != null) {
                    screenPresenter.gotPermissionStartShare();
                }
            } else {
                Toast.makeText(XyCallActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            picturePaths = Matisse.obtainPathResult(intent);
            L.i(TAG, "wang::: paths: " + picturePaths.size() + " ;; " + picturePaths);
            if (picturePaths.size() > 0) {
                // start share picture
                NemoSDK.getInstance().dualStreamStart(SharingValues.TYPE_SHARE_PICUTRE);
            }
        }
    }

    @Override
    public void updateShareScreen(NemoSDKListener.NemoDualState state) {
        if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STAT_IDLE) {
            if (screenPresenter != null && screenPresenter.isSharingScreen()) {
                L.i(TAG, "updateShareScreen stop");
                screenPresenter.stopShare();
            }
            shareScreenView.setVisibility(GONE);
            mVideoView.getLocalVideoCell().setVisibility(VISIBLE);
            tvShareScreen.setText("????????????");
            // ?????? ??????enable=true
            tvWhiteboard.setEnabled(true);
            tvWhiteboard.setTextColor(Color.WHITE);
            tvSharePhoto.setTextColor(Color.WHITE);
            tvSharePhoto.setEnabled(true);
        } else if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STATE_RECEIVING) {
            // show floating view
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityUtils.goHome(this);
                if (screenPresenter != null) {
                    screenPresenter.showFloatView(); // ???????????????
                }
                shareScreenView.setVisibility(VISIBLE);
                mVideoView.getLocalVideoCell().setVisibility(GONE);
                tvShareScreen.setText("????????????");
                // ?????? ??????enable=false
                tvWhiteboard.setTextColor(Color.GRAY);
                tvWhiteboard.setEnabled(false);
                tvSharePhoto.setTextColor(Color.GRAY);
                tvSharePhoto.setEnabled(false);
            }
        } else {
            Toast.makeText(this, "????????????, ?????????", Toast.LENGTH_SHORT).show();
        }
    }

    //=========================================================================================
    // whiteboard demo
    //=========================================================================================

    /**
     * ????????????
     */
    public void stopWhiteboardView() {
        tvShareScreen.setEnabled(true);
        tvShareScreen.setTextColor(Color.WHITE);
        tvSharePhoto.setEnabled(true);
        tvSharePhoto.setTextColor(Color.WHITE);
        if (mVideoView != null) {
            mVideoView.stopWhiteboard();
            if (whiteboardLaodingView.getVisibility() == VISIBLE) {
                whiteboardLaodingView.setVisibility(GONE);
            }
        }
    }

    /**
     * ????????????
     */
    public void startWhiteboardView() {
        if (mVideoView != null) {
            if (whiteboardLaodingView.getVisibility() == VISIBLE) {
                whiteboardLaodingView.setVisibility(GONE);
            }
            mVideoView.startWhiteboard();
            //?????? ??????enable=false
            tvShareScreen.setEnabled(false);
            tvShareScreen.setTextColor(Color.GRAY);
            tvSharePhoto.setEnabled(false);
            tvSharePhoto.setTextColor(Color.GRAY);
        }
    }

    private WhiteboardChangeListener whiteboardChangeListener = new WhiteboardChangeListener() {

        @SuppressLint("CheckResult")
        @Override
        public void onWhiteboardStart() {
            Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    L.i(TAG, "onWhiteboardStart");
                    // fix: ?????????????????????, ????????????????????????, ?????????????????????
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!ActivityUtils.isAppForeground(XyCallActivity.this)
                                && !(screenPresenter != null && screenPresenter.isSharingScreen())) {
                            ActivityUtils.moveTaskToFront(XyCallActivity.this);
                        }
                    }
                    if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        mVideoView.setLandscape(true);
                        NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
                    }
                    startWhiteboardView();
                }
            });
        }

        @SuppressLint("CheckResult")
        @Override
        public void onWhiteboardStop() {
            Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    stopWhiteboardView();
                }
            });
        }

        /**
         * ??????????????????
         *
         * @param message ????????????
         */
        @SuppressLint("CheckResult")
        @Override
        public void onWhiteboardMessage(final String message) {
            Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    mVideoView.onWhiteBoardMessages(message);
                }
            });
        }

        @SuppressLint("CheckResult")
        @Override
        public void onWhiteboardMessages(final ArrayList<String> messages) {
            Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        mVideoView.setLandscape(true);
                        NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
                    }
                    mVideoView.handleWhiteboardLinesMessage(messages);
                }
            });
        }
    };

    //=========================================================================================
    // ?????????????????????demo
    //=========================================================================================

    /**
     * ????????????????????????
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        L.i("VideoFragment onConfigChanged:: " + newConfig.orientation);
        int orientation = getResources().getConfiguration().orientation;
        L.i("VideoFragment orientation:: " + orientation);
    }

    private static final int MSG_ORIENTATION_CHANGED = 60001;
    private Handler orientationHanler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ORIENTATION_CHANGED) {
                handleOrientationChanged(msg.arg1);
            }
        }
    };

    private void handleOrientationChanged(int rotation) {
        if (rotation > 350 || rotation < 10) {
            // ?????? 0?????????????????????????????????home??????????????????
            // NOTE: ?????????????????????????????? ?????????????????????,  ???????????????????????????(???, ???, ???)
            if (!SpeakerVideoGroup.isShowingWhiteboard()) {
                if (layoutMode == LayoutMode.MODE_GALLERY) {
                    return;
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mVideoView.setLandscape(false);
                NemoSDK.getInstance().setOrientation(Orientation.PORTRAIT);
            }
        } else if (rotation > 80 && rotation < 100) {
            // ???????????? 90???????????????????????????90????????????home???????????????
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            mVideoView.setLandscape(true);
            NemoSDK.getInstance().setOrientation(Orientation.REVERSE_LANDSCAPE);
        } else if (rotation > 260 && rotation < 280) {
            // ?????? 270???????????????????????????270???????????????home???????????????
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mVideoView.setLandscape(true);
            NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
        }
    }

    private class YourOrientationEventListener extends OrientationEventListener {

        public YourOrientationEventListener(Context context) {
            super(context);
        }
        @Override
        public void onOrientationChanged(int orientation) {
            if (enableScreenOrientation) {
                orientationHanler.removeMessages(MSG_ORIENTATION_CHANGED);
                Message msg = handler.obtainMessage(MSG_ORIENTATION_CHANGED, orientation, 0);
                orientationHanler.sendMessageDelayed(msg, 100);
            }
        }
    }

    //=========================================================================================
    // fecc
    //=========================================================================================
    private void updateFeccStatus() {
        if (fullVideoInfo != null) {
            int feccOri = fullVideoInfo.getFeccOri();
            boolean isAudioOnly = Enums.LAYOUT_STATE_RECEIVED_AUDIO_ONLY.equals(fullVideoInfo.getLayoutVideoState());
            // allowControlCamera & feccDisable ???????????????FECC???????????? --> ????????????
            boolean isFeccSupport = feccBar.isSupportHorizontalFECC(feccOri) || feccBar.isSupportVerticalFECC(feccOri);
            L.i(TAG, "isFeccSupport: " + isFeccSupport);
            feccBar.setFECCButtonVisible(!fullVideoInfo.isVideoMute() && !isAudioOnly && isFeccSupport && !isSharePicture);
            feccBar.setZoomInOutVisible(feccBar.isSupportZoomInOut(feccOri));
            feccBar.setFeccTiltControl(feccBar.isSupportHorizontalFECC(feccOri), feccBar.isSupportVerticalFECC(feccOri));
        } else {
            feccBar.setFECCButtonVisible(false);
        }
    }

    private class FeccActionListener implements UserActionListener {
        @Override
        public void onUserAction(int action, Bundle args) {
            switch (action) {
                case UserActionListener.USER_ACTION_FECC_LEFT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_TURN_LEFT, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_RIGHT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_TURN_RIGHT, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STOP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_TURN_STOP, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STEP_LEFT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_STEP_LEFT, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STEP_RIGHT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_STEP_RIGHT, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_UP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_TURN_UP, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_DOWN:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_TURN_DOWN, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STEP_UP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_STEP_UP, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STEP_DOWN:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_STEP_DOWN, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_TURN_STOP, 10);
                    break;
                case UserActionListener.FECC_ZOOM_IN:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_ZOOM_IN, 10);
                    break;
                case UserActionListener.FECC_STEP_ZOOM_IN:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_STEP_ZOOM_IN, 10);
                    break;
                case UserActionListener.FECC_ZOOM_OUT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_ZOOM_OUT, 10);
                    break;
                case UserActionListener.FECC_STEP_ZOOM_OUT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_STEP_ZOOM_OUT, 10);
                    break;
                case UserActionListener.FECC_ZOOM_TURN_STOP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_ZOOM_TURN_STOP, 10);
                    break;
            }
        }
    }



    private void sendTop() {
        Intent intent = new Intent();
        intent.setAction(ACTION_TOPAPP_CHANGE_STATUS);
        intent.putExtra("status",active?"resume":"pause");
        sendBroadcast(intent);
    }

    boolean isDown = false;
    public class XyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Global.HUNG_UP_MEETTING)){
                Log.e(TAG, "HUNG_UP_MEETTING");
                NemoSDK.getInstance().hangup();
                NemoSDK.getInstance().releaseLayout();
                finish();
            }else if (intent.getAction().equals(Global.EXIT_APP)){
                Log.e(TAG, "EXIT_APP");
                NemoSDK.getInstance().hangup();
                NemoSDK.getInstance().releaseLayout();
                finish();
            }else if (intent.getAction().equals(Global.OPEN_VIDEO)){
                Log.e(TAG, "OPEN_VIDEO");
                hideOrShowToolbar(isToolbarShowing);
                NemoSDK.getInstance().setVideoMute(false);
                setVideoState(false);
            }else if (intent.getAction().equals(Global.CLOSE_VIDEO)){
                Log.e(TAG, "CLOSE_VIDEO");
                hideOrShowToolbar(isToolbarShowing);
                isVideoMute = !isVideoMute;
                NemoSDK.getInstance().setVideoMute(true);
                setVideoState(true);
            }else if (intent.getAction().equals(Global.OPEN_MIKE)){
                Log.e(TAG, "OPEN_MIKE");
                hideOrShowToolbar(isToolbarShowing);
                updateMuteStatus(false);
                Toast.makeText(XyCallActivity.this,"????????????",Toast.LENGTH_SHORT).show();
            }else if (intent.getAction().equals(Global.CLOSE_MIKE)){
                Log.e(TAG, "CLOSE_MIKE");
                hideOrShowToolbar(isToolbarShowing);
                updateMuteStatus(true);
                Toast.makeText(XyCallActivity.this,"??????",Toast.LENGTH_SHORT).show();
            }else if (intent.getAction().equals(Global.START_RECORDING)){
                Log.e(TAG, "START_RECORDING");
                hideOrShowToolbar(isToolbarShowing);
                if (NemoSDK.getInstance().isAuthorize()) {
                    setRecordVideo(true);
                } else {
                    Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                }
            }else if (intent.getAction().equals(Global.STOP_RECORDING)){
                Log.e(TAG, "STOP_RECORDING");
                hideOrShowToolbar(isToolbarShowing);
                if (NemoSDK.getInstance().isAuthorize()) {
                    setRecordVideo(false);
                } else {
                    switchLayout();
                    Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                }
            }else if (intent.getAction().equals(Global.SWITCH_LAYOUT)){
                Log.e(TAG, "SWITCH_LAYOUT");
                layoutMode = layoutMode == LayoutMode.MODE_SPEAKER ? LayoutMode.MODE_GALLERY : LayoutMode.MODE_SPEAKER;
                switchLayout();
                Toast.makeText(XyCallActivity.this,"????????????",Toast.LENGTH_SHORT).show();
            }else if (intent.getAction().equals(Global.HOLD_ON)){//??????
                if (mMediaPlayer!=null){
                    mMediaPlayer.release();//????????????
                }
                viewInvite.findViewById(R.id.bt_invite_accept).performClick();
            }else if (intent.getAction().equals(Global.HANG_UP)){//??????
                if (mMediaPlayer!=null){
                    mMediaPlayer.release();//????????????
                }
                viewInvite.findViewById(R.id.bt_invite_drop).performClick();
            }
        }
    }
    MediaPlayer mMediaPlayer;
    private void playMusic(boolean play) {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                AssetFileDescriptor mAssetFD = getAssets().openFd("phonering.mp3");//??????????????????
                mMediaPlayer.setDataSource(mAssetFD.getFileDescriptor(), mAssetFD.getStartOffset(), mAssetFD.getLength());//????????????
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);//?????????????????????
            }
            if (play) {
                mMediaPlayer.prepare();
                mMediaPlayer.setLooping(true);//????????????
                mMediaPlayer.start();//????????????
            } else {
                mMediaPlayer.release();//????????????
            }

        } catch (Exception e) {
            Log.d(TAG, "receiver mMediaPlayer Exception:");
            e.printStackTrace();
        }
    }

}
