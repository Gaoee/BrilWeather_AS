package com.brilweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.brilweather.model.SettingData;
import com.brilweather.weathersetting.MySharedPreferences;
import com.example.brilweather.R;

public class SettingActivity extends Activity implements View.OnClickListener{

	private final static String TAG = "LEE SettingActivity";

	private LinearLayout mGoBackLayout;
	private Switch mAutoUpdateBut;
	private Spinner mUpdateFreqSpin;
	private View mUpdateFreqHider;
	private Switch mUseLocateWeatherSwitch;
	private Switch mSendMessageSwitch;

	String[] mFreqString;

	private MySharedPreferences mySharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);

		mySharedPreferences = MySharedPreferences.getMySharePrefereces();
		mFreqString = getResources().getStringArray(R.array.spingarr);

		initView();
		initData();
	}

	private void initView(){
		mGoBackLayout = (LinearLayout)findViewById(R.id.goback_layout);
		mAutoUpdateBut = (Switch) findViewById(R.id.auto_update_switch);
		mUpdateFreqSpin = (Spinner)findViewById(R.id.update_freq);
		mUpdateFreqHider = (View)findViewById(R.id.update_freq_hider);
		mUseLocateWeatherSwitch = (Switch)findViewById(R.id.locateweather_switch);
		mSendMessageSwitch = (Switch)findViewById(R.id.send_message_switch);

		SettingData settingData = mySharedPreferences.getSettingData();
		mAutoUpdateBut.setChecked(settingData.getIsAutoUpdate());
		if(settingData.getIsAutoUpdate()) {
			mUpdateFreqHider.setVisibility(View.GONE);
		}else{
			mUpdateFreqHider.setVisibility(View.VISIBLE);
		}
		Log.v(TAG, "settingData.getFreqTime():" + settingData.getFreqTime());
		int freqTimePosition = 0;
		while(!mFreqString[freqTimePosition++].equals(settingData.getFreqTime()) && mFreqString.length > freqTimePosition);
		mUpdateFreqSpin.setSelection(--freqTimePosition);
		mUseLocateWeatherSwitch.setChecked(settingData.getIsUseLocateWeather());
		mSendMessageSwitch.setChecked(settingData.getISSendMessage());
	}

	private void initData(){
		mGoBackLayout.setOnClickListener(this);

		mAutoUpdateBut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mUpdateFreqHider.setVisibility(View.GONE);
					Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
					startService(intent);
				}else{
					mUpdateFreqHider.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.goback_layout:{
				finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		SettingData settingData = new SettingData();
		Log.v(TAG, "mAutoUpdateBut.isChecked()" + mAutoUpdateBut.isChecked());
		settingData.setIsAutoUpate(mAutoUpdateBut.isChecked());
		Log.v(TAG, "mFreqString[mUpdateFreqSpin.getSelectedItemPosition()]:" + mFreqString[mUpdateFreqSpin.getSelectedItemPosition()]);
		settingData.setFreqTime(mFreqString[mUpdateFreqSpin.getSelectedItemPosition()]);
		settingData.setIsUseLocateWeather(mUseLocateWeatherSwitch.isChecked());
		settingData.setIsSendMessage(mSendMessageSwitch.isChecked());

		mySharedPreferences.saveSettingData(settingData);
	}
}
