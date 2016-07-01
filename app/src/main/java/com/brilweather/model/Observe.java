package com.brilweather.model;

/**
 * Created by Administrator on 2016/6/30.
 */
public class Observe {
    private String currentTemp;
    private String publishTime;
    private String weatherDes;

    public void setCurrentTemp(String name) {
        this.currentTemp = name;
    }
    public String getCurrentTemp() {
        return this.currentTemp;
    }

    public void setPublishTime(String code){
        this.publishTime = code;
    }
    public String getPublishTime() {
        return this.publishTime;
    }

    public void setWeatherDes(String temp) {
        this.weatherDes = temp;
    }
    public String getWeatherDes(){
        return this.weatherDes;
    }
}
