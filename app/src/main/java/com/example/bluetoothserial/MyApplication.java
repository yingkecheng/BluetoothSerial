package com.example.bluetoothserial;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

public class MyApplication extends Application {
    private static final Handler mHandler = new Handler();
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();
    }

    /** 获取系统上下文：用于ToastUtil类 **/
    public static Context getAppContext() {
        return mAppContext;
    }


    /** 有疑问 **/
    public static void runUi(Runnable runnable) {
        mHandler.post(runnable);
    }
}
