package com.brilweather.model;

public class City {
	private int id;
	private String city_name;
	private String city_code;
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setCityName(String name) {
		this.city_name = name;
	}
	
	public String getCityName() {
		return this.city_name;
	}
	
	
	public void setCityCode(String code){
		this.city_code = code;
	}
	
	public String getCityCode() {
		return this.city_code;
	}
}
