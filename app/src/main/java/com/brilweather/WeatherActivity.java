package com.brilweather;

import android.app.Activity;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brilweather.DB.WeatherDB;
import com.brilweather.http.HttpCallbackListene;
import com.brilweather.http.HttpUtil;
import com.brilweather.model.City;
import com.brilweather.model.Weather;
import com.brilweather.weathersetting.MySharedPreferences;
import com.brilweather.weathershow.HorizontalScrollViewEx;
import com.brilweather.weathershow.MySwipeRefreshLayout;
import com.brilweather.weathershow.MyUtils;
import com.example.brilweather.R;

import org.json.JSONException;
import org.json.JSONObject;

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
		super.onStart();
		initView();
		initData();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		//activity已经绘制完成
		if(hasFocus){
			Log.v(TAG, "onWindowFocusChanged hasFocus");
			scrollToSelectedPage();
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
				updateWeather(getCurrentPage());

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
				updateWeather(currentCityId);
			}
		});

		swipeRefreshLayout.setOnGiveUpTouchEventListener(new MySwipeRefreshLayout.GiveUpTouchEventListener(){
			@Override
			public boolean giveUpTouchEven(MotionEvent ev) {
				ListView listView = (ListView) layoutList.get(getCurrentPage()).findViewById(R.id.list);
				if(listView.getFirstVisiblePosition() == 0){
					View view = listView.getChildAt(0);
					Log.v(TAG, "view.getTop():" + view.getTop());
					if(view != null && view.getTop() >= 0){
						return true;
					}
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

            createList(layout);
            layoutList.add(layout);
            updateView(i);
            mListContainer.addView(layout);
        }

		isInitView = false;
    }

	/**
	 * 只对页面进行更新，不更新天气数据
	 * @param cityId
     */
    private void updateView(int cityId) {
		Log.i(TAG, "updateView!");
		ViewGroup layoutGroup = layoutList.get(cityId);
		Log.v(TAG, "updateView cityID:" + cityId);
		Weather weather = weathers.get(cityId);

		TextView publishTextView = (TextView)layoutGroup.findViewById(R.id.publish_text);
		TextView currentTextView = (TextView)layoutGroup.findViewById(R.id.current_date);
		TextView despTextView = (TextView)layoutGroup.findViewById(R.id.weather_desp);
		TextView minTempTextView = (TextView)layoutGroup.findViewById(R.id.min_temp);
		TextView maxTempTextView = (TextView)layoutGroup.findViewById(R.id.max_temp);
		ListView listView = (ListView)layoutGroup.findViewById(R.id.list);

		Log.i(TAG,"updateView" + weather.getCityName() + weather.getCityCode() + weather.getMinTemp() + weather.getMaxTemp()
				+ weather.getDesp() + weather.getTime());

		if (weather.getTime() != null) {
			publishTextView.setText(weather.getTime().substring(10));
			currentTextView.setText(weather.getTime().substring(0, 10));
		}
		despTextView.setText(weather.getDesp());
		minTempTextView.setText(weather.getMinTemp());
		maxTempTextView.setText(weather.getMaxTemp());

		Log.i(TAG, "updateView finish!");
    }

	/**
	 * 更新cityCode指定的城市的天气数据,并最终调用updateView更新页面
	 * @param cityId
     */
    private void updateWeather(int cityId) {
		Weather weather = weathers.get(cityId);
		if(isOtherDate(weather.getTime())){
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
		String cityCode = weather.getCityCode();
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ cityCode + ".html";
		//showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListene() {

			@Override
			public void onFinish(String reportString) {
				Weather w = handleWeatherResponse(reportString);
				weather.setDesp(w.getDesp());
				weather.setMaxTemp(w.getMaxTemp());
				weather.setMinTemp(w.getMinTemp());
				weather.setTime(w.getTime());
				weatherDB.updateWeather(weather.getCityCode(), weather.getMinTemp(),
						weather.getMaxTemp(), weather.getDesp(), weather.getTime());
				Log.i(TAG, "getOnHttpWeather:" + weather.getCityName() + weather.getCityCode() + weather.getMinTemp() + weather.getMaxTemp()
						+ weather.getDesp() + weather.getTime());
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

	private void createList(ViewGroup layout) {
		ListView listView = (ListView) layout.findViewById(R.id.list);
		ArrayList<String> datas = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			datas.add("name " + i);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.content_list_item, R.id.name, datas);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				Toast.makeText(WeatherActivity.this, "click item",
						Toast.LENGTH_SHORT).show();

			}
		});
	}

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
     * 判断数据更新的时间是否超过了16个小时
     * @param date
     * @return
     */
    private Boolean isOtherDate(String date) {
    	if(date == null){
    		return true;
    	}

    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        try {
            Date dt1 = df.parse(date);
            Date today = new Date();

			//超过16个小时更新
            if (Math.abs(dt1.getTime() - today.getTime()) > 16*60*60*1000) {
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }


    /**
     *  处理http请求回来的数据
     * @param weatherString
     * @return
     */
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
			Log.i(TAG,"handleWeatherResponse" + weather.getCityName() + weather.getCityCode() + weather.getMinTemp() + weather.getMaxTemp()
					+ weather.getDesp() + weather.getTime());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return weather;
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
