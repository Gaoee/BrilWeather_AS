package com.brilweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.brilweather.weathersetting.MySharedPreferences;

public class AutoUpdateReceiver extends BroadcastReceiver {

	private final static String TAG = "LEE AutoUpdateReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "MySharedPreferences.getMySharePrefereces().readIsAutoUpdate():" + MySharedPreferences.getMySharePrefereces().readIsAutoUpdate());
		if(MySharedPreferences.getMySharePrefereces().readIsAutoUpdate()) {
			Intent i = new Intent(context, AutoUpdateService.class);
			context.startService(i);
		}
	}

}
