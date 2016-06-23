package com.brilweather.weathersetting;

import android.content.Context;
import android.content.SharedPreferences;

import com.brilweather.MyApplication;

/**
 * Created by Administrator on 2016/6/17.
 */
public class MySharedPreferences {

    private static MySharedPreferences mySharedPreferences;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private MySharedPreferences(){
        Context appContext = null;
        try {
            appContext = MyApplication.getAppContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert appContext != null;
        mSharedPreferences = appContext.getSharedPreferences("Setting_Config", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }
    
    public static MySharedPreferences getMySharePrefereces(){
        if (mySharedPreferences == null)
        {
            mySharedPreferences = new MySharedPreferences();
        }
        return mySharedPreferences;
    }

    private boolean saveConfigData(String configName, String value){
        mEditor.putString(configName, value);
        mEditor.commit();
        return true;
    }

    private String readConfigData(String configName){
        String value = mSharedPreferences.getString(configName, "123");
        return value;
    }

}
