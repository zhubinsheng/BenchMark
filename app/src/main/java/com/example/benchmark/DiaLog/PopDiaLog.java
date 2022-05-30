package com.example.benchmark.DiaLog;

import android.annotation.SuppressLint;
<<<<<<< HEAD
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
=======
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
>>>>>>> 211fdf0 ([feat]华为云手机、云手游，红手指、移动云手机稳定性测评)
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
<<<<<<< HEAD
import androidx.annotation.RequiresApi;

import com.example.benchmark.Activity.CePingActivity;
import com.example.benchmark.Activity.MainActivity;
import com.example.benchmark.R;
import com.example.benchmark.Service.AblService;
import com.example.benchmark.utils.AccessUtils;
=======

import com.example.benchmark.BaseApp;
import com.example.benchmark.R;
import com.example.benchmark.utils.AccessUtils;
import com.example.benchmark.utils.AutoStartUtil;
>>>>>>> 211fdf0 ([feat]华为云手机、云手游，红手指、移动云手机稳定性测评)

public class PopDiaLog extends Dialog  implements View.OnClickListener {
    private RelativeLayout ibility,zidong,houtai;
    private CheckBox ibility_cheak,zidong_cheak,houtai_cheak;
    private Button queren,quxiao;
    private Context context;
    private  AccessUtils accessUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.access_popwindow);
        initview();
        boolean accessibilityServiceOpen = accessUtils.isAccessibilityServiceOpen();
        if (accessibilityServiceOpen){
            ibility_cheak.setChecked(true);
        }else{
            ibility_cheak.setChecked(false);

        }

        if (accessUtils.isIgnoringBatteryOptimizations()){
            houtai_cheak.setChecked(true);
        }else {
            houtai_cheak.setChecked(false);
        }

        ibility.setOnClickListener(this::onClick);
        zidong.setOnClickListener(this::onClick);
        houtai.setOnClickListener(this::onClick);
        quxiao.setOnClickListener(this::onClick);
        queren.setOnClickListener(this::onClick);

<<<<<<< HEAD


=======
        setCanceledOnTouchOutside(false);
>>>>>>> 211fdf0 ([feat]华为云手机、云手游，红手指、移动云手机稳定性测评)

    }

    public PopDiaLog(@NonNull Context context) {
        super(context);
        this.context=context;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.access_ibility:{
                accessUtils.openAccessibilityService();
<<<<<<< HEAD
=======
                dismiss();
                break;
            }
            case R.id.access_ziqidong: {
                AutoStartUtil.openStart(BaseApp.context);
                dismiss();
>>>>>>> 211fdf0 ([feat]华为云手机、云手游，红手指、移动云手机稳定性测评)
                break;
            }
            case R.id.access_houtai:{
                accessUtils.requestIgnoreBatteryOptimizations();
<<<<<<< HEAD
                break;
            }
            case R.id.dialog_queren:{
                break;
            }
            case R.id.dialog_quxiao:{
                Toast.makeText(context,"权限开启失败",Toast.LENGTH_LONG).show();
                context.startActivity(new Intent(context, MainActivity.class));
=======
                dismiss();
                break;
            }
            case R.id.dialog_queren:{
//                Toast.makeText(context,"权限开启失败",Toast.LENGTH_LONG).show();
//                context.startActivity(new Intent(context, MainActivity.class));
                dismiss();
                break;
            }
            case R.id.dialog_quxiao:{
                dismiss();
>>>>>>> 211fdf0 ([feat]华为云手机、云手游，红手指、移动云手机稳定性测评)
            }

        }
    }


    void  initview(){
        ibility=findViewById(R.id.access_ibility);
        zidong = findViewById(R.id.access_ziqidong);
        houtai = findViewById(R.id.access_houtai);

        ibility_cheak=findViewById(R.id.access_ibility_cheak);
        zidong_cheak=findViewById(R.id.access_ziqidong_cheak);
        houtai_cheak = findViewById(R.id.access_houtai_cheak);

        accessUtils=new AccessUtils(context);

        queren=findViewById(R.id.dialog_queren);
        quxiao=findViewById(R.id.dialog_quxiao);

    }

}
