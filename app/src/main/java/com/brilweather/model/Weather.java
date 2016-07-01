package com.brilweather.model;

public class Weather {

	private int id;
	private String cityName;
	private String cityCode;
	private String observe;
	private String forecast;
	private String index;
	private String updateTime;

	public void setId(int id) {
		this.id = id;
	}
	public int getId(){
		return this.id;
	}
	
	public void setCityName(String name) {
		this.cityName = name;
	}
	
	public String getCityName() {
		return this.cityName;
	}
	
	public void setCityCode(String code){
		this.cityCode = code;
	}
	
	public String getCityCode() {
		return this.cityCode;
	}
	
	public void setObserve(String observe) {
		this.observe = observe;
	}
	public String getObserve(){
		return this.observe;
	}
	
	public void setForecast(String forecast) {
		this.forecast = forecast;
	}
	public String getForecast(){
		return this.forecast;
	}
	
	public void setIndex(String index) {
		this.index = index;
	}
	public String getIndex(){
		return this.index;
	}
	
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getUpdateTime(){
		return this.updateTime;
	}
}
