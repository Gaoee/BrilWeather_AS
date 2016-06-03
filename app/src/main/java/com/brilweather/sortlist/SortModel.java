package com.brilweather.sortlist;

public class SortModel {

	private String name;   //显示的数�??
	private String sortLetters;  //显示数据拼音的首字母
	private String code;	//显示城市码
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
}
