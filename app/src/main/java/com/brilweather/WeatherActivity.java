package com.brilweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.brilweather.DB.WeatherDB;
import com.brilweather.http.HttpCallbackListene;
import com.brilweather.http.HttpUtil;
import com.brilweather.http.JsonUtility;
import com.brilweather.model.City;
import com.brilweather.model.Forecast;
import com.brilweather.model.Index;
import com.brilweather.model.Observe;
import com.brilweather.model.Weather;
import com.brilweather.weathersetting.MySharedPreferences;
import com.brilweather.weathershow.HorizontalScrollViewEx;
import com.brilweather.weathershow.MySwipeRefreshLayout;
import com.brilweather.weathershow.MyUtils;
import com.example.brilweather.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class WeatherActivity extends Activity implements OnClickListener{
    private static final String TAG = "LEE WeatherActivity";

	public static final String SCROLL_ACTION = "Scroll_Action";

	private boolean isInitView = false;
	private boolean isOnPageChanged = false;
	private boolean isNoDialogShow = false;

    private HorizontalScrollViewEx mListContainer;
    private TextView cityNameTextView;
    private Button loactionMagButton;
    private Button weatherMenu;
	private MySwipeRefreshLayout swipeRefreshLayout;

    private List<ViewGroup> layoutList;

    private WeatherDB weatherDB;
    private List<Weather> weathers = new LinkedList<Weather>();
    private List<City> cities;

    private ProgressDialog progressDialog;

	private boolean isSwipeRefershEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_weather);

        mListContainer = (HorizontalScrollViewEx) findViewById(R.id.container);
        cityNameTextView = (TextView)findViewById(R.id.city_name);
        loactionMagButton = (Button)findViewById(R.id.city_mag);
        weatherMenu = (Button)findViewById(R.id.weather_menu);
		swipeRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.swiperefresh);

        loactionMagButton.setOnClickListener(this);
        weatherMenu.setOnClickListener(this);

        try {
			weatherDB = WeatherDB.getInstanceDatabase(this);
			Log.v(TAG, "getInstanceDatabase");
		} catch (Exception e) {
			e.printStackTrace();
		}
        Log.d(TAG, "onCreate");
        layoutList = new ArrayList<ViewGroup>();

		//开启定时更新服务
		if(MySharedPreferences.getMySharePrefereces().readIsAutoUpdate()) {
			Intent intent = new Intent(this, AutoUpdateService.class);
			startService(intent);
		}
    }

	@Override
	protected void onStart() {
		isNoDialogShow = true;
		super.onStart();
		initView();
		initData();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		//activity已经绘制完成
		if(hasFocus && isNoDialogShow){
			Log.v(TAG, "onWindowFocusChanged hasFocus");
			scrollToSelectedPage();
			isNoDialogShow = false;
		}
	}

	private void initData(){
		mListContainer.setScrollViewCallbackListener(new HorizontalScrollViewEx.onScrollViewCallbackListener() {

			@Override
			public void onPageChanged(int pageIndex) {
				isOnPageChanged = true;
				swipeRefreshLayout.setRefreshing(false);
				cityNameTextView.setText(weathers.get(pageIndex).getCityName());
				//初始时，通过http加载最新数据，形成了一个递归
				Log.v(TAG, "pageIndex:" + pageIndex + "  getCurrentPage():" + getCurrentPage());
				updateWeather(pageIndex, 60);
				isOnPageChanged = false;
			}

			//下面的代码解决的是SwipeRefersh与HorizontalScroll之间的滑动冲突问题
			@Override
			public void onPageScrolled(int position, float positionOffset) {
				Log.v(TAG, "position:" + position);
				Log.v(TAG, "positionOffset" + positionOffset);
				if (isSwipeRefershEnable && positionOffset != 0) {
					swipeRefreshLayout.setEnabled(false);
					isSwipeRefershEnable = false;
				}
			}
			@Override
			public void onPageScrollStateChanged(int state) {
				switch (state){
					case HorizontalScrollViewEx.SCROLL_STATE_IDLE:
						Log.v(TAG, "HorizontalScrollViewEx.SCROLL_STATE_IDLE");
						swipeRefreshLayout.setEnabled(true);
						isSwipeRefershEnable = true;
						break;
					default:
						break;
				}
			}
		});

		swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.RED, Color.YELLOW);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				int currentCityId = getCurrentPage();
				updateWeather(currentCityId, 16);
			}
		});

		swipeRefreshLayout.setOnGiveUpTouchEventListener(new MySwipeRefreshLayout.GiveUpTouchEventListener(){
			@Override
			public boolean giveUpTouchEven(MotionEvent ev) {
				ScrollView scrollView = (ScrollView) layoutList.get(getCurrentPage()).findViewById(R.id.scrollView);
				if(scrollView.getScrollY() == 0){
					return true;
				}
				return false;
			}
		});

	}


	private void initView() {
		mListContainer.removeAllViews();

		isInitView = true;
		Log.v(TAG, "initView()");
        LayoutInflater inflater = getLayoutInflater();
        final int screenWidth = MyUtils.getScreenMetrics(this).widthPixels;
        final int screenHeight = MyUtils.getScreenMetrics(this).heightPixels;

		//只在数据库中取了一次weather的数据
        getWeathers();

		//假如没有选择的城市，则直接进入城市选择列表
		if (weathers.size() == 0){
			Intent intent = new Intent(WeatherActivity.this,CityshowAcivity.class);
			intent.setAction(CityshowAcivity.INTENT_ACTION_NOCITY);
			startActivity(intent);
			finish();
			return;
		}
		Log.v(TAG, "weahters:" + weathers.size());
        cityNameTextView.setText(weathers.get(0).getCityName());
        layoutList.clear();
        for (int i = 0; i < weathers.size(); i++) {
            ViewGroup layout = (ViewGroup) inflater.inflate(
                    R.layout.content_layout, mListContainer, false);
            layout.getLayoutParams().width = screenWidth;

            layoutList.add(layout);
			updateWeatherAndView(i);
            mListContainer.addView(layout);
        }

		isInitView = false;
    }

	/**
	 * 只对页面进行更新，不更新天气数据   需要更改
	 * @param cityId
     */
    private void updateView(int cityId) {
		Log.i(TAG, "updateView!");
		ViewGroup layoutGroup = layoutList.get(cityId);
		Log.v(TAG, "updateView cityID:" + cityId);
		Weather weather = weathers.get(cityId);

		TextView publishTimeTextView = (TextView)layoutGroup.findViewById(R.id.publishtime_text);
		TextView currentTempTextView = (TextView)layoutGroup.findViewById(R.id.current_temp);
		TextView currentWeatherTextView = (TextView)layoutGroup.findViewById(R.id.current_weather);
		TextView airQualityTextView = (TextView)layoutGroup.findViewById(R.id.air_quality);
		TextView weather1TextView = (TextView)layoutGroup.findViewById(R.id.weather1);
		TextView weather2TextView = (TextView)layoutGroup.findViewById(R.id.weather2);
		TextView weather3TextView = (TextView)layoutGroup.findViewById(R.id.weather3);
		TextView temp1HigTextView = (TextView)layoutGroup.findViewById(R.id.temp1_hig);
		TextView temp2HigTextView = (TextView)layoutGroup.findViewById(R.id.temp2_hig);
		TextView temp3HigTextView = (TextView)layoutGroup.findViewById(R.id.temp3_hig);
		TextView temp1LowTextView = (TextView)layoutGroup.findViewById(R.id.temp1_low);
		TextView temp2LowTextView = (TextView)layoutGroup.findViewById(R.id.temp2_low);
		TextView temp3LowTextView = (TextView)layoutGroup.findViewById(R.id.temp3_low);
		TextView wind1TextView = (TextView)layoutGroup.findViewById(R.id.wind1);
		TextView wind2TextView = (TextView)layoutGroup.findViewById(R.id.wind2);
		TextView wind3TextView = (TextView)layoutGroup.findViewById(R.id.wind3);
		RelativeLayout trafficLayout = (RelativeLayout)layoutGroup.findViewById(R.id.traffic);
		TextView trafficIndexTextView = (TextView)layoutGroup.findViewById(R.id.traffic_index);
		RelativeLayout coldLayout = (RelativeLayout)layoutGroup.findViewById(R.id.cold);
		TextView coldIndexTextView = (TextView)layoutGroup.findViewById(R.id.cold_index);
		RelativeLayout clothLayout = (RelativeLayout)layoutGroup.findViewById(R.id.cloth);
		TextView clothIndexTextView = (TextView)layoutGroup.findViewById(R.id.cloth_index);

		JsonUtility jsonUtility = JsonUtility.getJsonUtility(getApplication());
		String observeString = weather.getObserve();
		String forecastString = weather.getForecast();
		String indexString = weather.getIndex();
		Log.v(TAG, "observeString:" + observeString);
		Log.v(TAG, "forecastString:" + forecastString);
		Log.v(TAG, "indexString:" + indexString);
		Observe observe = jsonUtility.handObserve(observeString);
		Forecast forecast = jsonUtility.handForeCast(forecastString);
		final Index index = jsonUtility.handleIndex(indexString);

		publishTimeTextView.setText(observe.getPublishTime());
		currentTempTextView.setText(observe.getCurrentTemp());
		currentWeatherTextView.setText(observe.getWeatherDes());

		Log.v(TAG, " forecast.getWeather_d1()" + forecast.getWeather_d1());
		if(forecast.getWeather_d1().length() == 0){
			weather1TextView.setText(forecast.getWeather_n1());
			temp1LowTextView.setText(forecast.getTemp_n1());
			temp1HigTextView.setText(observe.getCurrentTemp());
			wind1TextView.setText(forecast.getWind_n1());

			weather2TextView.setText(forecast.getWeather_n2());
			temp2LowTextView.setText(forecast.getTemp_n2());
			temp2HigTextView.setText(forecast.getTemp_d2());
			wind2TextView.setText(forecast.getWind_n2());

			weather3TextView.setText(forecast.getWeather_n3());
			temp3LowTextView.setText(forecast.getTemp_n3());
			temp3HigTextView.setText(forecast.getTemp_d3());
			wind3TextView.setText(forecast.getWind_n3());
		}else {
			weather1TextView.setText(forecast.getWeather_d1());
			temp1LowTextView.setText(forecast.getTemp_n1());
			temp1HigTextView.setText(forecast.getTemp_d1());
			wind1TextView.setText(forecast.getWind_d1());

			weather2TextView.setText(forecast.getWeather_d2());
			temp2LowTextView.setText(forecast.getTemp_n2());
			temp2HigTextView.setText(forecast.getTemp_d2());
			wind2TextView.setText(forecast.getWind_d2());

			weather3TextView.setText(forecast.getWeather_d3());
			temp3LowTextView.setText(forecast.getTemp_n3());
			temp3HigTextView.setText(forecast.getTemp_d3());
			wind3TextView.setText(forecast.getWind_d3());
		}
		Log.v(TAG, "index.getTraffIndex()" + index.getTraffIndex());
		trafficIndexTextView.setText(index.getTraffIndex());
		coldIndexTextView.setText(index.getColdIndex());
		clothIndexTextView.setText(index.getClothIndex());

		Log.i(TAG,"updateView" + weather.getCityName() + weather.getCityCode() + weather.getObserve() + weather.getForecast()
				+ weather.getIndex() + weather.getUpdateTime());

		airQualityTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		trafficLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						WeatherActivity.this);
				builder.setTitle("交通建议");
				builder.setMessage(index.getTraffSug());
				builder.setCancelable(true);
				builder.create().show();
			}
		});

		coldLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						WeatherActivity.this);
				builder.setTitle("健康建议");
				builder.setMessage(index.getColdSug());
				builder.setCancelable(true);
				builder.create().show();
			}
		});

		clothLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						WeatherActivity.this);
				builder.setTitle("穿衣建议");
				builder.setMessage(index.getClothSug());
				builder.setCancelable(true);
				builder.create().show();
			}
		});
		Log.i(TAG, "updateView finish!");
    }

	private void updateWeatherAndView(int cityId){
		Weather weather = weathers.get(cityId);
		if(isOtherDate(weather.getUpdateTime(), 16*60)){	//主动更新的时间
			getOnHttpWeather(cityId);
		}
		else {
			updateView(cityId);
		}
	}

	/**
	 * 更新cityCode指定的城市的天气数据,并最终调用updateView更新页面     手动更新时必须更新
	 * @param cityId
     */
    private void updateWeather(int cityId, int updateTime) {
		Weather weather = weathers.get(cityId);
		if(isOtherDate(weather.getUpdateTime(), updateTime)){
			getOnHttpWeather(cityId);
		}
		else {
			if (!isInitView && !isOnPageChanged) {
				Toast.makeText(this, "已是最新天气", Toast.LENGTH_SHORT).show();
				swipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	/**
	 * 加载http数据，对数据库中的数据进行更新，并直接对页面进行了更新
	 * @param cityId
     */
	private void getOnHttpWeather(final int cityId) {
		final Weather weather = weathers.get(cityId);
		final String cityCode = weather.getCityCode();
		//showProgressDialog();
		HttpUtil.sendHttpRequest(cityCode, new HttpCallbackListene() {

			@Override
			public void onFinish(String reportString) {
				Weather w = JsonUtility.getJsonUtility(getApplication()).handleWeatherResponse(reportString, cityCode);
				weather.setIndex(w.getIndex());
				weather.setForecast(w.getForecast());
				weather.setObserve(w.getObserve());
				weather.setUpdateTime(w.getUpdateTime());
				weatherDB.updateWeather(weather.getCityCode(), weather.getObserve(),
						weather.getForecast(), weather.getIndex(), weather.getUpdateTime());
				Log.i(TAG, "getOnHttpWeather:" + weather.getCityName() + weather.getCityCode() + weather.getObserve() + weather.getForecast()
						+ weather.getIndex() + weather.getUpdateTime());
				Log.v(TAG, "cityID:" + cityId);
				runOnUiThread(new Runnable() {
					public void run() {
						//closeProgressDialog();
						//因为这里是一个线程，为了保证数据的有效更新，必须重新回调updateView
						updateView(cityId);
						if (!isInitView && !isOnPageChanged) {
							Toast.makeText(WeatherActivity.this, "天气更新成功", Toast.LENGTH_SHORT).show();
							swipeRefreshLayout.setRefreshing(false);
						}
					}
				});
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						//closeProgressDialog();
						if (!isInitView && !isOnPageChanged){
							Toast.makeText(WeatherActivity.this, "天气更新失败", Toast.LENGTH_SHORT).show();
							swipeRefreshLayout.setRefreshing(false);
						}
					}
				});
			}
		});
	}

	/**
	 * 获得当前所在的page
	 * @return
     */
	private int getCurrentPage(){
		return mListContainer.getPageIndex();
	}

    ///////////////////////////////////////////////////

    private void showProgressDialog() {
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载中...");
			progressDialog.setCanceledOnTouchOutside(false);
		}

		progressDialog.show();
	}

    private void closeProgressDialog() {
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}


    private void getWeathers() {
		weathers = weatherDB.loadWeathers();
	}

    private void getCities() {
		cities = weatherDB.loadSelectedCity();
		for (City city : cities) {
			Weather weather = new Weather();
			weather.setCityCode(city.getCityCode());
			weather.setCityName(city.getCityName());
			weathers.add(weather);
		}
	}


    /**
     * 判断数据更新的时间是否超过了updateTime分钟
     * @param date
	 * @param updateTime 单位分钟
     * @return
     */
    private Boolean isOtherDate(String date, int updateTime) {
		Log.v(TAG, "date: " + date );
    	if(date == null){
    		return true;
    	}
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CANADA);
        try {
            Date dt1 = df.parse(date);
            Date today = new Date();
			Log.v(TAG, "dt1:" + dt1 + " today:" + today);
			//超过updateTime分钟更新
            if (Math.abs(dt1.getTime() - today.getTime()) > updateTime*60*1000) {
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }




	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.city_mag:
			Intent intent = new Intent(WeatherActivity.this, CitymanageActivity.class);
			startActivity(intent);
			break;

		case R.id.weather_menu:
			Intent intent2 = new Intent(WeatherActivity.this, SettingActivity.class);
			startActivity(intent2);
			break;

		default:
			break;
		}
	}

	/**
	 * 滑动到指定页面
	 */
	private void scrollToSelectedPage(){
		Intent intent = getIntent();
		if(intent.getAction().equals(SCROLL_ACTION)){
			int pageId = intent.getIntExtra("cityId", layoutList.size() -1);
			Log.v(TAG, "pageId" + pageId);
			String cityName = intent.getStringExtra("cityName");
			cityNameTextView.setText(cityName);
			mListContainer.scrollToSelectedPage(pageId);
			//改变action值，否则下次过来还是会进入这段代码
			intent.setAction(" ");
		}
		else {
			//一般的返回，返回到
			mListContainer.scrollToSelectedPage(0);
		}
	}

}
