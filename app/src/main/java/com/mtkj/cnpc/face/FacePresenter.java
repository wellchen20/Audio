package com.mtkj.cnpc.face;

import android.content.Context;
import android.log.L;

import com.ainemo.sdk.model.AIParam;
import com.ainemo.sdk.model.FaceInfo;
import com.ainemo.sdk.model.FacePosition;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.util.JsonUtil;
import com.mtkj.cnpc.net.DefaultHttpObserver;
import com.mtkj.cnpc.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * 人脸识别的业务处理
 *
 * @author zhangyazhou
 * @deprecated
 */
public class FacePresenter implements FaceContract.Presenter {

    private static final String TAG = "FacePresenter";

    private FaceContract.View faceContractView;
    private FaceInfoCache faceInfoCache;
    private FaceViewCache faceViewCache;

    public FacePresenter(Context context, FaceContract.View view) {
        this.faceContractView = view;
        faceInfoCache = new FaceInfoCache();
        faceViewCache = new FaceViewCache();
        faceContractView.setPresenter(this);
    }

    @Override
    public void dealAiParam(AIParam aiParam, boolean isMainCellInfo) {
        if (isMainCellInfo) {
            checkFaceInfoCache(aiParam);
            checkFaceViewCache(false, aiParam);
            showFaceView(aiParam);
        }
    }

    @Override
    public void dealLocalAiParam(AIParam aiParam, boolean isMainCell) {
        if (isMainCell) {
            checkFaceInfoCache(aiParam);
            checkFaceViewCache(true, aiParam);
            showFaceView(aiParam);
        }
    }

    public void clear() {
        faceInfoCache.clear();
    }

    private void getFaceInfoFromServer(long participantId, List<FacePosition> positionList) {
        L.i(TAG, "getFaceInfoFromServer");
        if (CollectionUtils.isEmpty(positionList)) {
            L.w(TAG, "人脸位置信息为null!!!");
            return;
        }
        long[] faceIds = new long[positionList.size()];
        for (int i = 0; i < positionList.size(); i++) {
            faceIds[i] = positionList.get(i).getFaceId();
        }
        getMultiFaceInfo(participantId, faceIds);
    }

    private void getMultiFaceInfo(final long participantId, final long[] faceIds) {
        L.i(TAG, "getMultiFaceInfo:" + participantId + ",faceIds:" + (faceIds));
        NemoSDK.getInstance().getMultiFaceInfo(faceIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultHttpObserver<List<FaceInfo>>("getMultiFaceInfo") {
                    @Override
                    public void onNext(List<FaceInfo> list, boolean isJSON) {
                        L.i(TAG, "resp-facelist:" + JsonUtil.toJson(list));
                        faceInfoCache.putFaceInfoList(participantId, list);
                    }

                    @Override
                    public void onHttpError(HttpException exception, String errorData, boolean isJSON) {
                        super.onHttpError(exception, errorData, isJSON);
                        L.i(TAG, exception.message());
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        super.onException(throwable);
                        L.i(TAG, throwable.getCause());
                    }
                });

    }

    private void checkFaceInfoCache(AIParam aiParam) {
        L.i(TAG, "checkFaceInfoCache");
        List<FacePosition> noCacheList = new ArrayList<>();
        for (int i = 0; i < aiParam.getPositionVec().size(); i++) {
            FacePosition position = aiParam.getPositionVec().get(i);
            if (position.getFaceId() > 0) {
                if (!faceInfoCache.isCacheFace(aiParam.getParticipantId(), position.getFaceId())) {
                    noCacheList.add(position);
                }
            } else {
                FaceInfo faceInfo = new FaceInfo();
                faceInfo.setPosition("");
                faceInfo.setName("");
                faceInfo.setFaceId(position.getFaceId());
                faceInfoCache.putFaceInfo(aiParam.getParticipantId(), faceInfo);
                L.w(TAG, "face id 无效!");
            }
        }
        if (CollectionUtils.isNotEmpty(noCacheList)) {
            getFaceInfoFromServer(aiParam.getParticipantId(), noCacheList);
        }
    }

    private void checkFaceViewCache(boolean isLocalFace, AIParam aiParam) {
        L.i(TAG, "checkFaceViewCache, isLocalFace:" + isLocalFace + ", aiParam:" + aiParam);
        for (int i = 0; i < aiParam.getPositionVec().size(); i++) {
            FacePosition position = aiParam.getPositionVec().get(i);
//            if (position.getFaceId() <= 0) {
//                L.w(TAG, "face is :" + position.getFaceId());
//                continue;
//            }
            FaceView faceView = faceViewCache.getFaceInfoView(aiParam.getParticipantId(), position.getFaceId());
            if (faceView == null) {
                L.i(TAG, "get face info, faceId:" + position.getFaceId() + ", cellId:" + aiParam.getParticipantId());
                FaceInfo faceInfo = faceInfoCache.getFaceInfo(aiParam.getParticipantId(), position.getFaceId());
                if (faceInfo != null) {
                    faceView = new FaceView(this.faceContractView.getCallActivity());
                    faceView.setPosition(faceInfo.getPosition());
                    faceView.setName(faceInfo.getName());
                    faceView.setFaceId(faceInfo.getFaceId());
                    faceView.setParticipantId(aiParam.getParticipantId());
                    faceInfoCache.putFaceInfo(aiParam.getParticipantId(), faceInfo);
                    faceViewCache.putFaceInfoView(aiParam.getParticipantId(), faceView);
                    calculatePosition(isLocalFace, faceView, position);
                } else {
                    L.w(TAG, " face info is null!!!");
                }
            } else {
                calculatePosition(isLocalFace, faceView, position);
            }
        }
    }

    private void showFaceView(AIParam aiParam) {
        L.i(TAG, "showFaceView");
        List<FaceView> showViews = new ArrayList<>();
        for (FacePosition position : aiParam.getPositionVec()) {
            FaceView faceView = faceViewCache.getFaceInfoView(aiParam.getParticipantId(), position.getFaceId());
            if (faceView != null) {
                showViews.add(faceView);
            }
        }
        faceContractView.showFaceView(showViews);
    }

    private void calculatePosition(boolean isLocalFace, FaceView faceView, FacePosition position) {
        int[] cellSize = faceContractView.getMainCellSize();
        float left = cellSize[0] * position.getLeft() / 10000.0F;
        float top = cellSize[1] * position.getTop() / 10000.0F;
        float right = cellSize[0] * position.getRight() / 10000.0F;
        float bottom = cellSize[1] * position.getBottom() / 10000.0F;
        L.i(TAG, "计算后的位置,left:" + left + ",top:" + top + ", right:" + right + ",bottom:" + bottom);
        faceView.setLayoutPosition(isLocalFace, ((int) left), ((int) top), ((int) right), ((int) bottom));
    }

    @Override
    public void start() {

    }
}
