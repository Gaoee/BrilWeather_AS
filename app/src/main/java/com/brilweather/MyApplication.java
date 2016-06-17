package com.brilweather;

import android.app.Application;
import android.util.Log;

/**
 * Created by Administrator on 2016/6/16.
 */
public class MyApplication extends Application {
    private final static String TAG = "LEE MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
    }
}
