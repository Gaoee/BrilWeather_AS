package com.brilweather.http;

import org.json.JSONObject;

import android.content.Context;

public class Utility {
	
	/**
	 * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
	 * 
	 */
	public static void handleWeatherResponse(Context context, String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			//save to DB
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
