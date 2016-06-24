package com.brilweather.model;

/**
 * Created by Administrator on 2016/6/23.
 */
public class SettingData {

    private boolean isAutoUpdate;
    private String freqTime;
    private boolean isUseLocateWeather;
    private boolean isSendMessage;

    public void setIsAutoUpate(boolean isAutoUpate) {
        this.isAutoUpdate = isAutoUpate;
    }

    public boolean getIsAutoUpdate(){
        return this.isAutoUpdate;
    }

    public void setFreqTime(String freqTime){
        this.freqTime = freqTime;
    }

    public String getFreqTime(){
        return this.freqTime;
    }

    public void setIsUseLocateWeather(boolean isUseLocateWeather){
        this.isUseLocateWeather = isUseLocateWeather;
    }

    public boolean getIsUseLocateWeather(){
        return this.isUseLocateWeather;
    }

    public void setIsSendMessage(boolean isSendMessage){
        this.isSendMessage = isSendMessage;
    }
    public boolean getISSendMessage(){
        return this.isSendMessage;
    }
}
