package com.brilweather.http;

import android.content.Context;
import android.util.Log;

import com.brilweather.model.Forecast;
import com.brilweather.model.Index;
import com.brilweather.model.Observe;
import com.brilweather.model.Weather;
import com.example.brilweather.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JsonUtility {
	private static final String TAG = "Lee JsonUtility";

	private static Context context;
	private static JsonUtility jsonUtility;
	private static String[] Weatheres;
	private static String[] Winds;

	private JsonUtility(){
		Weatheres = context.getResources().getStringArray(R.array.weather_phenomena);
		Winds = context.getResources().getStringArray(R.array.wind_power);
	}

	public static JsonUtility getJsonUtility(Context context){
		JsonUtility.context = context;
		if (jsonUtility == null){
			jsonUtility = new JsonUtility();
		}
		return jsonUtility;
	}
	/**
	 *  处理http请求回来的数据
	 * @param weatherString
	 * @return
	 */
	public Weather handleWeatherResponse(String weatherString, String areaCode) {
		Weather weather = new Weather();

		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss",Locale.CANADA);
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String updateTime = formatter.format(curDate);
		Log.v(TAG, "weatherString:" + weatherString);//{"errNum":300207,"errMsg":"Your service is overdue, please pay in time"}
		try {
			JSONObject jsonObject = new JSONObject(weatherString);
			JSONObject jsonAir =jsonObject.getJSONObject("air");
			JSONObject jsonAlarm = jsonObject.getJSONObject("alarm").getJSONObject(areaCode);
			JSONArray jsonForecast = jsonObject.getJSONObject("forecast").getJSONObject("24h")
					.getJSONObject(areaCode).getJSONArray("1001001");
			JSONObject jsonObserve = jsonObject.getJSONObject("observe").getJSONObject(areaCode).getJSONObject("1001002");
			JSONArray jsonIndex = jsonObject.getJSONObject("index").getJSONObject("24h")
					.getJSONObject(areaCode).getJSONArray("1001004");

			Log.v(TAG, "jsonAir:" + jsonAir.toString());
			Log.v(TAG, "jsonAlarm:" + jsonAlarm.toString());
			Log.v(TAG, "jsonForecast:" + jsonForecast.toString());
			Log.v(TAG, "jsonObserve: "+ jsonObserve.toString());
			Log.v(TAG, "jsonIndex: "+ jsonIndex.toString());

			weather.setForecast(jsonForecast.toString());
			weather.setIndex(jsonIndex.toString());
			weather.setObserve(jsonObserve.toString());
			weather.setUpdateTime(updateTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return weather;
	}

	public Index handleIndex(String indexString){
		Index index  = new Index();
		if (indexString == null){
			return index;
		}

		try {
			JSONArray jsonIndex = new JSONArray(indexString);
			//index解析
			JSONObject jsonTodayIndex = jsonIndex.getJSONObject(0);
			JSONObject jsonCloth = jsonTodayIndex.getJSONObject("002");
			String clothIndex = jsonCloth.getString("002002");
			String clothSug = jsonCloth.getString("002003");
			Log.v(TAG, "clothIndex:" + clothIndex + " clothSug:" + clothSug );
			JSONObject jsonCold = jsonTodayIndex.getJSONObject("004");
			String coldIndex = jsonCold.getString("004002");
			String coldSug = jsonCold.getString("004003");
			Log.v(TAG, "coldIndex:" + coldIndex + " coldSug:" + coldSug);
			JSONObject jsonTraffic = jsonTodayIndex.getJSONObject("005");
			String traffIndex = jsonTraffic.getString("005002");
			String traffSug = jsonTraffic.getString("005003");
			Log.v(TAG, "traffIndex:" + traffIndex + " traffSug:" + traffSug);

			index.setClothIndex(clothIndex);
			index.setClothSug(clothSug);
			index.setColdIndex(coldIndex);
			index.setColdSug(coldSug);
			index.setTraffIndex(traffIndex);
			index.setTraffSug(traffSug);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return index;
	}

	public Observe handObserve(String observeString){
		Observe observe = new Observe();
		if(observeString == null){
			return observe;
		}
		JSONObject jsonObserve = null;
		try {
			jsonObserve = new JSONObject(observeString);
			//observe解析
			String currentTemp = jsonObserve.getString("002");
			String publishTime = jsonObserve.getString("000");
			String weatherCode = jsonObserve.getString("001");
			String weatherDes = Weatheres[Integer.valueOf(weatherCode)];
			Log.v(TAG, "publishTime：" + publishTime + " currentTemp：" + currentTemp + " weatherCode：" + weatherCode
					+ " weatherDes:" + weatherDes);

			observe.setCurrentTemp(currentTemp);
			observe.setPublishTime(publishTime);
			observe.setWeatherDes(weatherDes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return observe;
	}

	public Forecast handForeCast(String forecastString){
		Forecast forecast = new Forecast();
		if (forecastString == null){
			return forecast;
		}
		JSONArray jsonForecast = null;
		try {
			jsonForecast = new JSONArray(forecastString);
			//forecast解析
			JSONObject jsonTodayForecast = jsonForecast.getJSONObject(0);
			JSONObject jsonTomorrowForecast = jsonForecast.getJSONObject(1);
			JSONObject jsonThirdForecast = jsonForecast.getJSONObject(2);
			//Today
			String temp_d1;
			String weatherCode_d1;
			String weather_d1;
			String windCode_d1;
			String wind_d1;
			Log.v(TAG, "jsonTodayForecast.getString(\"003\").length():" + jsonTodayForecast.getString("003").length());
			if(jsonTodayForecast.getString("003").length() != 0){
				temp_d1 = jsonTodayForecast.getString("003");
				weatherCode_d1 = jsonTodayForecast.getString("001");
				weather_d1 = Weatheres[Integer.valueOf(weatherCode_d1)];
				windCode_d1 = jsonTodayForecast.getString("005");
				wind_d1 = Winds[Integer.valueOf(windCode_d1)];
			}
			else {
				temp_d1 = "";
				weather_d1 = "";
				wind_d1 = "";
			}

			String temp_n1 = jsonTodayForecast.getString("004");
			String weatherCode_n1 = jsonTodayForecast.getString("002");
			String weather_n1 = Weatheres[Integer.valueOf(weatherCode_n1)];
			String windCode_n1 = jsonTodayForecast.getString("006");
			String wind_n1 = Winds[Integer.valueOf(windCode_n1)];
			Log.v(TAG, "temp_n1:" + temp_n1 + " temp_d1:"+ temp_d1 + " weather_d1:" + weather_d1
					+ " weather_n1:" + weather_n1 + " wind_d1:" + wind_d1 + " wind_n1:" + wind_n1);
			//Tomorrow
			String temp_d2 = jsonTomorrowForecast.getString("003");
			String weatherCode_d2 = jsonTomorrowForecast.getString("001");
			String weather_d2 = Weatheres[Integer.valueOf(weatherCode_d2)];
			String windCode_d2 = jsonTomorrowForecast.getString("005");
			String wind_d2 = Winds[Integer.valueOf(windCode_d2)];
			String temp_n2 = jsonTomorrowForecast.getString("004");
			String weatherCode_n2 = jsonTomorrowForecast.getString("002");
			String weather_n2 = Weatheres[Integer.valueOf(weatherCode_n2)];
			String windCode_n2 = jsonTomorrowForecast.getString("006");
			String wind_n2 = Winds[Integer.valueOf(windCode_n2)];
			//Third Day
			String temp_d3 = jsonThirdForecast.getString("003");
			String weatherCode_d3 = jsonThirdForecast.getString("001");
			String weather_d3 = Weatheres[Integer.valueOf(weatherCode_d3)];
			String windCode_d3 = jsonThirdForecast.getString("005");
			String wind_d3 = Winds[Integer.valueOf(windCode_d3)];
			String temp_n3 = jsonThirdForecast.getString("004");
			String weatherCode_n3 = jsonThirdForecast.getString("002");
			String weather_n3 = Weatheres[Integer.valueOf(weatherCode_n3)];
			String windCode_n3 = jsonThirdForecast.getString("006");
			String wind_n3 = Winds[Integer.valueOf(windCode_n3)];

			forecast.setTemp_d1(temp_d1);
			forecast.setTemp_d2(temp_d2);
			forecast.setTemp_d3(temp_d3);
			forecast.setTemp_n1(temp_n1);
			forecast.setTemp_n2(temp_n2);
			forecast.setTemp_n3(temp_n3);
			forecast.setWeather_d1(weather_d1);
			forecast.setWeather_d2(weather_d2);
			forecast.setWeather_d3(weather_d3);
			forecast.setWeather_n1(weather_n1);
			forecast.setWeather_n2(weather_n2);
			forecast.setWeather_n3(weather_n3);
			forecast.setWind_d1(wind_d1);
			forecast.setWind_d2(wind_d2);
			forecast.setWind_d3(wind_d3);
			forecast.setWind_n1(wind_n1);
			forecast.setWind_n2(wind_n2);
			forecast.setWind_n3(wind_n3);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return forecast;
	}
}
