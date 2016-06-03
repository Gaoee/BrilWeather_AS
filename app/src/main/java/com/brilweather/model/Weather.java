package com.brilweather.model;

import java.sql.Time;


public class Weather {

	private int id;
	private String cityName;
	private String cityCode;
	private String temp1;
	private String temp2;
	private String desp;
	private String time;

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
	
	public void setMinTemp(String temp) {
		this.temp1 = temp;
	}
	public String getMinTemp(){
		return this.temp1;
	}
	
	public void setMaxTemp(String temp) {
		this.temp2 = temp;
	}
	public String getMaxTemp(){
		return this.temp2;
	}
	
	public void setDesp(String desp) {
		this.desp = desp;
	}
	public String getDesp(){
		return this.desp;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	public String getTime(){
		return this.time;
	}
	
}
