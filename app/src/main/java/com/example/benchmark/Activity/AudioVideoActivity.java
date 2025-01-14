package com.example.benchmark.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONException;
import com.example.benchmark.R;
//import com.example.myapplication15.data.JsonData;
import com.example.benchmark.thread.AudioDecodeThread;
import com.example.benchmark.thread.VideoDecodeThread;
import com.example.benchmark.utils.ScoreUtil;

import java.util.Timer;
import java.util.TimerTask;

public class AudioVideoActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean isTestOver = false;
    private SurfaceView mSurfaceView;
    public static String mMp4FilePath;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mHolder;
    private TextView yinhuaxinxi;
    private long videocurtime, audiocurtime;
    private long lastVideoCurtime = -1, lastAudioCurtime = -1;
    private int heightPixels, widthPixels;
    private long maxDifferenceValue;
    private boolean isCompleted = false;
    private VideoDecodeThread videoDecodeThread;
    private AudioDecodeThread audioDecodeThread;

    public static void start(Context context) {
        Intent intent = new Intent(context, AudioVideoActivity.class);
        context.startActivity(intent);
    }

    Handler handler = new Handler() {
        @SuppressLint({"HandlerLeak", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            videocurtime = videoDecodeThread.getcurTime() / 10000;
            audiocurtime = audioDecodeThread.getcurTime() / 10000;

            if (isCompleted) {
                yinhuaxinxi.setText("测试结束！\n" +
                        "当前云手机像素为" + heightPixels + "X" + widthPixels + "像素" + "\n当前视频帧" + videocurtime + "\n"
                        + "当前音频帧" + audiocurtime + "\n" +
                        "\n" + "最大音画同步差" + maxDifferenceValue);
                ScoreUtil.calcAndSaveSoundFrameScores("0X0", maxDifferenceValue);
                isTestOver = true;
                Intent intent = new Intent(AudioVideoActivity.this, CePingActivity.class);
                startActivity(intent);
                return;
            }

            //当视频帧和音频帧不再增加判断播放结束
            if (lastAudioCurtime == audiocurtime && lastVideoCurtime == videocurtime && !isCompleted) {
                //Toast.makeText(AudioVideoActivity.this,"视频播放结束",Toast.LENGTH_LONG).show();
                isCompleted = true;
            }


            if (Math.abs(videocurtime - audiocurtime) > maxDifferenceValue) {
                maxDifferenceValue = Math.abs(videocurtime - audiocurtime);
            }
            yinhuaxinxi.setText("手机像素为" + heightPixels + "X" + widthPixels + "像素" + "\n当前视频帧" + videocurtime + "\n"
                    + "当前音频帧" + audiocurtime + "\n" + "当前音画同步差" + Math.abs((videocurtime - audiocurtime))
            );
            lastAudioCurtime = audiocurtime;
            lastVideoCurtime = videocurtime;

        }
    };


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_video);
        init();

        Log.d("资源文件", "资源文件的路径" + mMp4FilePath);

        //截取视频一帧图像 分析分辨率
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(mMp4FilePath);
//        Bitmap bitmap = retriever.getFrameAtIndex(10);
//        Log.e("TWT", "height: "+bitmap.getHeight() + "width:" +bitmap.getWidth());


    }

    void init() {
        mMediaPlayer = new MediaPlayer();
        mSurfaceView = findViewById(R.id.surface_view);
        mHolder = mSurfaceView.getHolder();
        yinhuaxinxi = findViewById(R.id.yinhua_item);
        //mMp4FilePath="android.resource://"+AudioVideoActivity.this.getPackageName()+"/"+R.raw.minions;
        //mMp4FilePath="/sdcard/ScreenRecorder/1660217722579.mp4";
        mMp4FilePath = getIntent().getStringExtra("path");
        WindowManager wm = (WindowManager) AudioVideoActivity.this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        //分辨率
        heightPixels = outMetrics.heightPixels;
        widthPixels = outMetrics.widthPixels;
        //最大同步差
        maxDifferenceValue = 0;

        //直接开始测试崩溃 等待1S后开始测试
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startTest();
            }
        };
        timer.schedule(task, 1000);
        //startTest();

    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    @Override
    public void onClick(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }
    }

    private void startTest() {
        videoDecodeThread = new VideoDecodeThread(mMp4FilePath, AudioVideoActivity.this);
        videoDecodeThread.setSurfaceView(mSurfaceView);
        //开启音频解码线程
        audioDecodeThread = new AudioDecodeThread(mMp4FilePath, AudioVideoActivity.this);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int sessionId = audioManager.generateAudioSessionId();
        audioDecodeThread.setSessionId(sessionId);
        videoDecodeThread.start();
        audioDecodeThread.start();
        if (videoDecodeThread.isAlive() && audioDecodeThread.isAlive()) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    handler.sendMessage(message);
                    if (isTestOver) {
                        timer.cancel();
                    }
                }
            }, 0, 1000);
        } else {
            Toast.makeText(AudioVideoActivity.this, "视频播放结束", Toast.LENGTH_LONG).show();
        }
    }

}