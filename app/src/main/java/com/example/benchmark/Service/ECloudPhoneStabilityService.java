package com.example.benchmark.Service;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;

import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.benchmark.utils.AccessibilityCallback;
import com.example.benchmark.utils.AccessibilityUtil;
import com.example.benchmark.utils.CacheConst;
import com.example.benchmark.utils.CacheUtil;

public class ECloudPhoneStabilityService implements IStabilityService{

    private final int screenHeight = CacheUtil.getInt(CacheConst.KEY_SCREEN_HEIGHT);
    private final int screenWidth = CacheUtil.getInt(CacheConst.KEY_SCREEN_WIDTH);
    private final String NODE_ID_CLICK_VIEW = "com.chinamobile.cmss.saas.cloundphone:id/index_img";
    private final String NODE_ID_QUIT_PHONE = "com.chinamobile.cmss.saas.cloundphone:id/netwrok_ok";
    private final String NODE_TEXT_QUIT_PHONE = "确认";
    private final String NODE_ID_NO_NOTICE = "com.chinamobile.cmss.saas.cloundphone:id/netwrok_check";

    private final MyAccessibilityService service;

    private int mCurrentMonitorNum = 0;
    private long mQuitTime = 0L;
    private long mLastTapTime = 0L;

    private boolean isClickQuitNotice = false;
    private boolean isConnectSuccess = false;
    private boolean isTapSuccess = false;

    public ECloudPhoneStabilityService(MyAccessibilityService service) {
        this.service = service;
    }

    @Override
    public void onMonitor() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || isFinished()) return;
        if (isTapSuccess) {
            isConnectSuccess = true;
            startControlCloudPhone();
            startQuitCloudPhone();
            mCurrentMonitorNum++;
            isTapSuccess = false;
            isConnectSuccess = false;
            return;
        }
        if (!isConnectSuccess) {
            AccessibilityNodeInfo clickNode = AccessibilityUtil.findNodeInfo(
                    service, NODE_ID_CLICK_VIEW, "");
            if (clickNode == null) return;
            if (mLastTapTime != 0L && System.currentTimeMillis() - mLastTapTime < 1000L) return;
            mLastTapTime = System.currentTimeMillis();
            service.startCaptureScreen();
            AccessibilityUtil.tap(service, screenWidth / 2, screenHeight / 2,
                    new AccessibilityCallback() {
                        @Override
                        public void onSuccess() {
                            isTapSuccess = true;
                        }
                        @Override
                        public void onFailure() {
                            isTapSuccess = true;
                        }
                    });
        }
    }

    @Override
    public void startControlCloudPhone() {
        long mStartTime = System.currentTimeMillis();
        AccessibilityNodeInfo clickNode = AccessibilityUtil.findNodeInfo(
                service, NODE_ID_CLICK_VIEW, "");
        while (clickNode != null || AccessibilityUtil.findIsExistClass(
                service, "android.widget.ProgressBar")) {
            clickNode = AccessibilityUtil.findNodeInfo(
                    service, NODE_ID_CLICK_VIEW, "");
        }
        Log.e("QT", "openTime:"+(System.currentTimeMillis() - mStartTime));
        service.mOpenTime.add(System.currentTimeMillis() - mStartTime);
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.mStartTimes.add(mStartTime);
    }

    @Override
    public void startQuitCloudPhone() {
        // 双击返回键退出云手机
        service.performGlobalAction(GLOBAL_ACTION_BACK);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.performGlobalAction(GLOBAL_ACTION_BACK);
        mQuitTime = System.currentTimeMillis();
        closeDialogIfExistWhenQuit();
        AccessibilityNodeInfo nodeClickView = null;
        while (nodeClickView == null) {
            nodeClickView = AccessibilityUtil.findNodeInfo(
                    service, NODE_ID_CLICK_VIEW, "");
        }
        service.mQuitTimes.add(System.currentTimeMillis() - mQuitTime);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeDialogIfExistWhenQuit() {
        if (isClickQuitNotice) return;
        new Thread(() -> {
            try {
                Thread.sleep(1000L);
                AccessibilityNodeInfo nodeBtnQuit = AccessibilityUtil.findNodeInfo(service,
                        NODE_ID_QUIT_PHONE, NODE_TEXT_QUIT_PHONE);
                if (nodeBtnQuit != null) {
                    AccessibilityNodeInfo noNotionNode = AccessibilityUtil.findNodeInfo(service,
                            NODE_ID_NO_NOTICE, "");
                    if (noNotionNode != null) AccessibilityUtil.performClick(noNotionNode);
                    AccessibilityUtil.performClick(nodeBtnQuit);
                    mQuitTime = System.currentTimeMillis();
                }
                isClickQuitNotice = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public int getCurrentMonitorNum() {
        return mCurrentMonitorNum;
    }
}
