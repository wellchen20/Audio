package com.mtkj.cnpc.face;

import android.app.Activity;

import com.ainemo.sdk.model.AIParam;
import com.mtkj.cnpc.BasePresenter;
import com.mtkj.cnpc.BaseView;

import java.util.List;

/**
 * 人脸检测的Presenter和View的契约类
 *
 * @author zhangyazhou
 * @deprecated
 */
public interface FaceContract {

    interface Presenter extends BasePresenter {
        /**
         * 处理Ai回调数据
         *
         * @param aiParam        ai数据
         * @param isMainCellInfo 是否为主屏数据信息
         */
        void dealAiParam(AIParam aiParam, boolean isMainCellInfo);

        void dealLocalAiParam(AIParam aiParam, boolean isMainCell);
    }

    interface View extends BaseView<Presenter> {

        /**
         * 显示人脸信息
         *
         * @param faceViews 要显示的View
         */
        void showFaceView(List<FaceView> faceViews);

        /**
         * 获取Activity的Context
         *
         * @return Activity本身, 用于缓存FaceView时使用
         */
        Activity getCallActivity();

        /**
         * 获取主Cell的尺寸
         *
         * @return 尺寸结果, [0]=width, [1]=height
         */
        int[] getMainCellSize();
    }
}
