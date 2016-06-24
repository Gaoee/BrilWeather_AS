package com.brilweather.weathersetting;

import android.content.Context;
import android.content.SharedPreferences;

import com.brilweather.MyApplication;
import com.brilweather.model.SettingData;

/**
 * Created by Administrator on 2016/6/17.
 */
public class MySharedPreferences {

    private static final String TAG = "LEE MySharedPreferences";

    private final String AUTOUDATEKEY = "autoupdate";
    private final String FREQTIMEKEY = "freqtime";
    private final String USELOCATEWEATHER = "uselocateweather";
    private final String SEMDMESSAGE = "sendmesssage";

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
        return mEditor.commit();
    }

//    private String readConfigData(String configName){
//        String value = mSharedPreferences.getString(configName, "123");
//        return value;
//    }

    public boolean readIsAutoUpdate(){
        boolean isAutoUpdate;
        isAutoUpdate = mSharedPreferences.getBoolean(AUTOUDATEKEY, true);
        return isAutoUpdate;
    }

    public String readFreqTime(){
        String freqTime = mSharedPreferences.getString(FREQTIMEKEY, "24小时");
        return freqTime;
    }

    public boolean readIsUseLocateWeather(){
        boolean isUesLocateWeather = mSharedPreferences.getBoolean(USELOCATEWEATHER , true);
        return isUesLocateWeather;
    }

    public boolean readIsSendMessage(){
        boolean isSendMessage = mSharedPreferences.getBoolean(SEMDMESSAGE, true);
        return isSendMessage;
    }

    public boolean saveSettingData(SettingData settingData){
        boolean isAutoUpdate = settingData.getIsAutoUpdate();
        String freqTime = settingData.getFreqTime();
        boolean isUseLocateWeather = settingData.getIsUseLocateWeather();
        boolean isSendMessage = settingData.getISSendMessage();

        mEditor.putBoolean(AUTOUDATEKEY, isAutoUpdate);
        mEditor.putString(FREQTIMEKEY, freqTime);
        mEditor.putBoolean(USELOCATEWEATHER, isUseLocateWeather);
        mEditor.putBoolean(SEMDMESSAGE, isSendMessage);

        return mEditor.commit();
    }

    public SettingData getSettingData(){
        SettingData settingData = new SettingData();

        settingData.setIsAutoUpate(mSharedPreferences.getBoolean(AUTOUDATEKEY, true));
        settingData.setFreqTime(mSharedPreferences.getString(FREQTIMEKEY, "24小时"));
        settingData.setIsUseLocateWeather(mSharedPreferences.getBoolean(USELOCATEWEATHER, true));
        settingData.setIsSendMessage(mSharedPreferences.getBoolean(SEMDMESSAGE, true));

        return settingData;
    }

}
