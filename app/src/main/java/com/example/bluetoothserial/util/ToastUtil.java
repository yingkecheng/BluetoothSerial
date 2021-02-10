package com.example.bluetoothserial.util;

import android.widget.Toast;

import com.example.bluetoothserial.MyApplication;

/**
 * 简单的Toast封装类
 */
public class ToastUtil {
    private static Toast toast;

    public static void showMessage(String msg) {
        if(MyApplication.getAppContext() != null){
            if (toast == null) {
                toast = Toast.makeText(MyApplication.getAppContext(), msg, Toast.LENGTH_SHORT);
            } else {
                toast.setText(msg);
            }
            toast.show();
        }
    }
}
