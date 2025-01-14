package com.example.benchmark.Service;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK;

import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.benchmark.utils.AccessibilityCallback;
import com.example.benchmark.utils.AccessibilityUtil;
import com.example.benchmark.utils.CacheConst;
import com.example.benchmark.utils.CacheUtil;

public class RedFingerStabilityService implements IStabilityService {

    private final int screenHeight = CacheUtil.getInt(CacheConst.KEY_SCREEN_HEIGHT);
    private final int screenWidth = CacheUtil.getInt(CacheConst.KEY_SCREEN_WIDTH);
    private final String NODE_ID_CLICK_VIEW = "com.redfinger.app:id/click_view";
    private final String NODE_ID_START_CONTROL = "com.redfinger.app:id/btnConfirm";
    private final String NODE_TEXT_START_CONTROL = "开始控制";
    private final String NODE_ID_NO_NOTICE = "com.redfinger.app:id/check_box";
    private final String NODE_ID_CONTINUE_CONTROL = "com.redfinger.app:id/tv_ok";
    private final String NODE_TEXT_CONTINUE_CONTROL = "继续控制";
    private final String NODE_ID_QUIT_PHONE = "com.redfinger.app:id/tv_ok";
    private final String NODE_TEXT_QUIT_PHONE = "确定";

    private final MyAccessibilityService service;

    private int mCurrentMonitorNum = 0;
    private long mStartTime = 0L;
    private long mQuitTime = 0L;
    private long mLastTapTime = 0L;

    private boolean isClickStartControl = false;
    private boolean isClickContinueControl = false;
    private boolean isClickQuitNotice = false;
    private boolean isConnectSuccess = false;
    private boolean isTapSuccess = false;

    public RedFingerStabilityService(MyAccessibilityService service) {
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
            if (mLastTapTime != 0L && System.currentTimeMillis() - mLastTapTime < 1500L) return;
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
        mStartTime = System.currentTimeMillis();
        closeDialogIfExistWhenStart();
        AccessibilityNodeInfo clickNode = AccessibilityUtil.findNodeInfo(
                service, NODE_ID_CLICK_VIEW, "");
        AccessibilityNodeInfo startControlNode = AccessibilityUtil.findNodeInfo(service,
                NODE_ID_START_CONTROL, NODE_TEXT_START_CONTROL);
        AccessibilityNodeInfo continueControlNode = AccessibilityUtil.findNodeInfo(service,
                NODE_ID_CONTINUE_CONTROL, NODE_TEXT_CONTINUE_CONTROL);
        while (clickNode != null || startControlNode != null || continueControlNode != null) {
            clickNode = AccessibilityUtil.findNodeInfo(
                    service, NODE_ID_CLICK_VIEW, "");
            startControlNode = AccessibilityUtil.findNodeInfo(service,
                    NODE_ID_START_CONTROL, NODE_TEXT_START_CONTROL);
            continueControlNode = AccessibilityUtil.findNodeInfo(service,
                    NODE_ID_CONTINUE_CONTROL, NODE_TEXT_CONTINUE_CONTROL);
        }
        service.mOpenTime.add(System.currentTimeMillis() - mStartTime);
        try {
            // wait cloud phone loading
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
        AccessibilityNodeInfo nodeClickView = AccessibilityUtil.findNodeInfo(
                service, NODE_ID_CLICK_VIEW, "");
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

    private void closeDialogIfExistWhenStart() {
        if (isClickStartControl || isClickContinueControl) return;
        new Thread(() -> {
            try {
                if (!isClickStartControl) {
                    Thread.sleep(1000L);
                    AccessibilityNodeInfo startControlNode = AccessibilityUtil.findNodeInfo(service,
                            NODE_ID_START_CONTROL, NODE_TEXT_START_CONTROL);
                    if (startControlNode != null) {
                        AccessibilityNodeInfo noNoticeNode = AccessibilityUtil.findNodeInfo(service,
                                NODE_ID_NO_NOTICE, "");
                        if (noNoticeNode != null) AccessibilityUtil.performClick(noNoticeNode);
                        AccessibilityUtil.performClick(startControlNode);
                        mStartTime = System.currentTimeMillis();
                    }
                    isClickStartControl = true;
                }
                if (!isClickContinueControl) {
                    Thread.sleep(1000L);
                    AccessibilityNodeInfo continueControlNode = AccessibilityUtil.findNodeInfo(service,
                            NODE_ID_CONTINUE_CONTROL, NODE_TEXT_CONTINUE_CONTROL);
                    if (continueControlNode != null) {
                        AccessibilityNodeInfo noNoticeNode = AccessibilityUtil.findNodeInfo(service,
                                NODE_ID_NO_NOTICE, "");
                        if (noNoticeNode != null) AccessibilityUtil.performClick(noNoticeNode);
                        AccessibilityUtil.performClick(continueControlNode);
                        mStartTime = System.currentTimeMillis();
                    }
                    isClickContinueControl = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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
