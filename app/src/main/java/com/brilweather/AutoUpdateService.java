package com.brilweather;

import java.util.ArrayList;
import java.util.List;

import com.brilweather.DB.WeatherDB;
import com.brilweather.http.HttpCallbackListene;
import com.brilweather.http.HttpUtil;
import com.brilweather.http.JsonUtility;
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
		int anHour = updateFrequency*60*1000;			//设定后台更新时间为
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

	
	 private void getOnHttpWeather(final String cityCode, final int cityId) {
			HttpUtil.sendHttpRequest(cityCode, new HttpCallbackListene() {
				
				@Override
				public void onFinish(String reportString) {
					Weather weather = weathers.get(cityId);
					Weather w = JsonUtility.getJsonUtility(getApplication()).handleWeatherResponse(reportString, cityCode);
					weather.setIndex(w.getIndex());
					weather.setForecast(w.getForecast());
					weather.setObserve(w.getObserve());
					weather.setUpdateTime(w.getUpdateTime());
					weatherDB.updateWeather(weather.getCityCode(), weather.getObserve(),
							weather.getForecast(), weather.getIndex(), weather.getUpdateTime());
					Log.i(TAG,"AutoUpdateService" + weather.getCityName() + weather.getCityCode() + weather.getObserve() + weather.getForecast()
							+ weather.getIndex() + weather.getUpdateTime());
					Log.v(TAG, "AutoUpdateService cityID:" + cityId);
				}
				
				@Override
				public void onError(Exception e) {
				}
			});
		}

}
