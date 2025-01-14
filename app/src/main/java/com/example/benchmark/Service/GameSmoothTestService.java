package com.example.benchmark.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.benchmark.Activity.TestSMActivity;
import com.example.benchmark.R;
import com.example.benchmark.utils.CacheConst;
import com.example.benchmark.utils.CacheUtil;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class GameSmoothTestService extends Service {
    //
//    private final int screenHeight = CacheUtil.getInt(CacheConst.KEY_SCREEN_HEIGHT);
//    private final int screenWidth = CacheUtil.getInt(CacheConst.KEY_SCREEN_WIDTH);
    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;

    private int screenWidth;
    private int screenHeight;
    private int screenDensity;

    private boolean running;
    private int width = 720;
    private int height = 1080;
    private int dpi;

    private String path = "";
    //
    private boolean isAble=true;
    //定义浮动窗口布局
    LinearLayout mFloatLayout;
    LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;
    private Context mContext;
    TextView mFloatView;
    private long startTime;
    private long endTime;
    //private boolean isColor=true;
    private boolean isRecording=false;
    private int statusBarHeight;

    private int timeCount = 15; //录屏时间
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: 123123");
            mFloatView.setText(String.valueOf(timeCount));
        }
    };

    private Messenger mMessenger;

    private static final String TAG = "TWT";

    @Override
    public void onCreate()
    {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        running = false;
        mediaRecorder = new MediaRecorder();

        mContext=GameSmoothTestService.this;
        createFloatView();

    }
//    @Override
//    public IBinder onBind(Intent intent)
//    {
//        return null;
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return new GameSmoothBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void createFloatView()
    {
        Log.d("TWT", "createFloatView:gamesmoothTest ");
        wmParams = new LayoutParams();
        //获取WindowManagerImpl.CompatModeWrapper
        mWindowManager =  (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //设置window type
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            wmParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            wmParams.type= LayoutParams.TYPE_TOAST;
        }
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags =
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
                LayoutParams.FLAG_NOT_FOCUSABLE
//          LayoutParams.FLAG_NOT_TOUCHABLE
        ;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.START | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值(设置最大直接显示在右下角)
        wmParams.x = width - 50;
        wmParams.y = height / 2;
        //设置悬浮窗口长宽数据
        wmParams.width = LayoutParams.WRAP_CONTENT;
        wmParams.height = LayoutParams.WRAP_CONTENT;
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.game_smooth_float, null);
        mFloatView = (TextView)mFloatLayout.findViewById(R.id.textinfo);
        mWindowManager.addView(mFloatLayout, wmParams);

        //获取状态栏的高度
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        statusBarHeight = getResources().getDimensionPixelSize(resourceId);

        // handler.sendEmptyMessage(1);
        //浮动窗口按钮
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isclick=false;
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startTime=System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                        wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth()/2;
                        wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight()/2-statusBarHeight;
                        //Log.d("TWT", "onTouch: "+MainActivity.);
                        //刷新
                        mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        endTime=System.currentTimeMillis();
                        //小于0.2秒被判断为点击
                        if ((endTime - startTime) > 200) {
                            isclick = false;
                        } else {
                            isclick = true;
                        }
                        break;
                }
                //响应点击事件
                if (isclick&&isAble) {
                    isAble = false;
                    //Toast.makeText(mContext, "准备录屏进行测试", Toast.LENGTH_SHORT).show();
                    //count = 10;
                    startRecord();
                    //mFloatView.setText(String.valueOf(count));
                    Timer timer=new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            Message a = new Message();
                            handler.sendMessage(a);
                            if(timeCount==0){
                                timer.cancel();
                                Log.d(TAG, "run: stop");
                                stopRecord();
                            }
                            timeCount--;

                        }
                    };
                    timer.schedule(task,0,1000);
                }
                return true;
            }
        });

        mFloatView.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //Toast.makeText(FxService.this, "onClick", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
//        if(mFloatLayout != null)
//
//        {
//            mWindowManager.removeView(mFloatLayout);
//        }
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        if(mMessenger==null){
//            mMessenger = (Messenger) intent.getExtras().get("messenger");
//        }
//        return super.onStartCommand(intent, flags, startId);
//    }


    //
    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }

    public boolean isRunning() {
        return running;
    }

    public void setConfig(int width, int height, int dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    public boolean startRecord() {
        if (mediaProjection == null || running) {
            return false;
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        screenDensity = metrics.densityDpi;
        initRecorder();
        createVirtualDisplay();
        mediaRecorder.start();
        running = true;
        Log.d(TAG, "begin:开始录制 ");
        return true;
    }

    public boolean stopRecord() {
        if (!running) {
            return false;
        }
        running = false;
        mediaRecorder.stop();
        mediaRecorder.reset();
        virtualDisplay.release();
        //mediaProjection.stop();
        Log.d(TAG, "begin:结束录制 ");

        //录制结束对录制视频进行测试
        TestSMActivity.start(this,path);
        stopSelf();
        if(mFloatLayout != null)
        {
            mWindowManager.removeView(mFloatLayout);
        }
        return true;
    }

    private void createVirtualDisplay() {
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int screenWidth = metrics.widthPixels;
//        int screenHeight = metrics.heightPixels;
//        int screenDensity = metrics.densityDpi;
        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
    }

    private void initRecorder() {
        try{
            //mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            path = getsaveDirectory() + System.currentTimeMillis() + ".mp4";
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(path);
            mediaRecorder.setVideoSize(screenWidth, screenHeight);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
            mediaRecorder.setVideoFrameRate(30);

        }catch (Exception e){
            Log.e("TWT", "initRecorder: "+e.toString() );
        }
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getsaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecorder" + "/";
            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }

            //Toast.makeText(getApplicationContext(), rootDir, Toast.LENGTH_SHORT).show();

            return rootDir;
        } else {
            return null;
        }
    }

    public class GameSmoothBinder extends Binder {
        public GameSmoothTestService getGameSmoothService() {
            return GameSmoothTestService.this;
        }
    }

}


