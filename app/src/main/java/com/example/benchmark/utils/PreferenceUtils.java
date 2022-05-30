package com.example.benchmark.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;


//用来判断用户是不是第一次登录，如果是第一次登录，则跳转到引导
//页面，如果不是，则跳转到主界面
public class PreferenceUtils {
<<<<<<< HEAD
    private  static final String FILE_NAME="BenchMark";
    private static final String MODE_NAME="isFirst";

=======
    private  static final String FILE_NAME="CloudHealth";
    private static final String MODE_NAME="isFirst";
    //利用用户偏好对象，对app登录进行判断
>>>>>>> 211fdf0 ([feat]华为云手机、云手游，红手指、移动云手机稳定性测评)

    //获取是否市第一次进入app的Boolean值
    public static  boolean get_isFirst_come(Context context){
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getBoolean(MODE_NAME,false);
    }

    //写入Boolean的值
    public static void put_isFirst_boolean(Context context, boolean isFirst){
        //获取用户偏好写入对象
        @SuppressLint("WrongConstant")
        SharedPreferences.Editor editor= context.getSharedPreferences(FILE_NAME, Context.MODE_APPEND).edit();
        //写入boolean值
        editor.putBoolean(MODE_NAME,isFirst);
        editor.apply();
    }

}
