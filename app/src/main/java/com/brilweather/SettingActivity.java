package com.brilweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.brilweather.R;

public class SettingActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting);
		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString("a", "45");
	}
}
