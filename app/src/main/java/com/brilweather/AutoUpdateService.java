package com.brilweather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.brilweather.DB.WeatherDB;
import com.brilweather.http.HttpCallbackListene;
import com.brilweather.http.HttpUtil;
import com.brilweather.model.Weather;
import com.brilweather.weathersetting.MySharedPreferences;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class AutoUpdateService extends Service {

	private static final String TAG = "LEE AutoUpdateService";
	WeatherDB weatherDB = null;
	List<Weather> weathers = new ArrayList<>();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					weatherDB = WeatherDB.getInstanceDatabase(getApplicationContext());
				} catch (Exception e) {
					e.printStackTrace();
				}
				weathers.clear();
				weathers = weatherDB.loadWeathers();
				updateWeather();
				
			}
		}).start();
		AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
		String updateFrequencyString = MySharedPreferences.getMySharePrefereces().readFreqTime();
		int updateFrequency = Integer.valueOf(updateFrequencyString.substring(0,updateFrequencyString.length()-2));
		Log.v(TAG, "updateFrequency:" + updateFrequency);
		int anHour = 60*1000;			//设定后台更新时间为4h updateFrequency*
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent i = new Intent(this, AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void updateWeather() {
		for (int i = 0; i < weathers.size(); i++) {
			Weather weather = weathers.get(i);
			String cityCode = weather.getCityCode();
			getOnHttpWeather(cityCode, i);
		}
	}

	
	 private void getOnHttpWeather(String cityCode, final int cityId) {
	    	String address = "http://www.weather.com.cn/data/cityinfo/"
					+ cityCode + ".html";
			HttpUtil.sendHttpRequest(address, new HttpCallbackListene() {
				
				@Override
				public void onFinish(String reportString) {
					Weather weather = weathers.get(cityId);
					Weather w = handleWeatherResponse(reportString);
					weather.setDesp(w.getDesp());
					weather.setMaxTemp(w.getMaxTemp());
					weather.setMinTemp(w.getMinTemp());
					weather.setTime(w.getTime());
					weatherDB.updateWeather(weather.getCityCode(), weather.getMinTemp(),
							weather.getMaxTemp(), weather.getDesp(), weather.getTime());
					Log.i(TAG,"AutoUpdateService" + weather.getCityName() + weather.getCityCode() + weather.getMinTemp() + weather.getMaxTemp()
							+ weather.getDesp() + weather.getTime());
					Log.v(TAG, "AutoUpdateService cityID:" + cityId);
					
				}
				
				@Override
				public void onError(Exception e) {
				}
			});
		}
	 
	 	/*
	     * 处理http请求回来的数据
	     * */
	    private Weather handleWeatherResponse(String weatherString) {
	    	Weather weather = new Weather();
	    	
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ", Locale.CHINA);
	    	
			try {
				JSONObject jsonObject = new JSONObject(weatherString);
				JSONObject jsonWeather = jsonObject.getJSONObject("weatherinfo");
				weather.setMinTemp(jsonWeather.getString("temp2"));
				weather.setMaxTemp(jsonWeather.getString("temp1"));
				weather.setDesp(jsonWeather.getString("weather"));
				weather.setTime(sdf.format(new Date()) + jsonWeather.getString("ptime"));
				Log.i(TAG,"AutoUpdateService" + weather.getCityName() + weather.getCityCode() + weather.getMinTemp() + weather.getMaxTemp()
						+ weather.getDesp() + weather.getTime());
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return weather;
		}
}
