package com.brilweather;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2016/6/16.
 */
public class MyApplication extends Application {
    private final static String TAG = "LEE MyApplication";

    private static Context myApplicationContext;
    @Override
    public void onCreate() {
        super.onCreate();
        myApplicationContext = this;
        Log.v(TAG, "onCreate");
    }

    public static Context getAppContext() throws Exception{
        if(myApplicationContext != null){
            return myApplicationContext;
        }else{
            throw new Exception("AppContext is Null!");
        }
    }
}
