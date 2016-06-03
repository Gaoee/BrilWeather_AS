package com.brilweather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.brilweather.DB.WeatherDB;
import com.brilweather.http.HttpCallbackListene;
import com.brilweather.http.HttpUtil;
import com.brilweather.model.City;
import com.brilweather.model.Weather;
import com.brilweather.weathershow.HorizontalScrollViewEx;
import com.brilweather.weathershow.MyUtils;
import com.brilweather.weathershow.ScrollViewCallbackListene;
import com.example.brilweather.R;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts.Data;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class WeatherActivity extends Activity implements OnClickListener{
    private static final String TAG = "LEE";

    private HorizontalScrollViewEx mListContainer;
    private TextView cityNameTextView;
    private Button loactionMagButton;
    private Button refreshButton;
    
    private List<ViewGroup> layoutList;
    
    private WeatherDB weatherDB;
    private List<Weather> weathers = new LinkedList<Weather>();
    private List<City> cities;
    
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_weather);
        
        mListContainer = (HorizontalScrollViewEx) findViewById(R.id.container);
        cityNameTextView = (TextView)findViewById(R.id.city_name);
        loactionMagButton = (Button)findViewById(R.id.city_mag);
        refreshButton = (Button)findViewById(R.id.refresh_weather);
        
        loactionMagButton.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
        
        try {
			weatherDB = WeatherDB.getInstanceDatabase(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
        Log.d(TAG, "onCreate");
        layoutList = new ArrayList<ViewGroup>();
        
        mListContainer.setScrollViewCallbackListene(new ScrollViewCallbackListene() {
		
			@Override
			public void onPageChanged(int pageIndex) {
				cityNameTextView.setText(weathers.get(pageIndex).getCityName());
			}
        });
    }

    private void initView() {
        LayoutInflater inflater = getLayoutInflater();
        final int screenWidth = MyUtils.getScreenMetrics(this).widthPixels;
        final int screenHeight = MyUtils.getScreenMetrics(this).heightPixels;

        weathers = weatherDB.loadWeathers();
        cityNameTextView.setText(weathers.get(0).getCityName());
        layoutList.clear();
        for (int i = 0; i < weathers.size(); i++) {
        	Weather weather = weathers.get(i);
        	
            ViewGroup layout = (ViewGroup) inflater.inflate(
                    R.layout.content_layout, mListContainer, false);
            layout.getLayoutParams().width = screenWidth;
            createList(layout);
            
            layoutList.add(layout);
            updateView(i, layout);
            mListContainer.addView(layout);
        }
        
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mListContainer.removeAllViews();
    	initView();
    }

    private int currentPage(){
    	return mListContainer.getPageIndex();
    }
    
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
    
    private void updateView(int cityId, ViewGroup layout) {
		ViewGroup layoutGroup = layoutList.get(cityId);
		Log.v(TAG, "updateView cityID:" + cityId);
		Weather weather = weathers.get(cityId);
		
		TextView publishTextView = (TextView)layoutGroup.findViewById(R.id.publish_text);
		TextView currentTextView = (TextView)layoutGroup.findViewById(R.id.current_date);
		TextView despTextView = (TextView)layoutGroup.findViewById(R.id.weather_desp);
		TextView minTempTextView = (TextView)layoutGroup.findViewById(R.id.min_temp);
		TextView maxTempTextView = (TextView)layoutGroup.findViewById(R.id.max_temp);
		ListView listView = (ListView)layoutGroup.findViewById(R.id.list);
		
		Log.i(TAG, weather.getCityName() + weather.getCityCode() + weather.getMinTemp() + weather.getMaxTemp()
				+ weather.getDesp() + weather.getTime());
		
		if (weather.getTime() != null) {
			publishTextView.setText(weather.getTime().substring(10));
			currentTextView.setText(weather.getTime().substring(0, 10));
		}
		despTextView.setText(weather.getDesp());
		minTempTextView.setText(weather.getMinTemp());
		maxTempTextView.setText(weather.getMaxTemp());
		
    }
    
    private void updateWeather(String cityCode) {
		Weather weather;
		weather = weatherDB.loadWeather(cityCode);
		if(isOtherDate(weather.getTime())){
			getOnHttpWeather(cityCode, currentPage());
		}
		else {
			Toast.makeText(this, "已是最新天气", Toast.LENGTH_SHORT).show();
		}
	}
    
    
    private void getOnHttpWeather(String cityCode, final int cityId) {
    	String address = "http://www.weather.com.cn/data/cityinfo/"
				+ cityCode + ".html";
    	showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListene() {
			
			@Override
			public void onFinish(String reportString) {
				Weather weather = weathers.get(cityId);
				Weather w = handleWeatherResponse(reportString);
				weather.setDesp(w.getDesp());
				weather.setMaxTemp(w.getMaxTemp());
				weather.setMinTemp(w.getMinTemp());
				weather.setTime(w.getTime());
				weatherDB.updataWeather(weather.getCityCode(), weather.getMinTemp(), 
						weather.getMaxTemp(), weather.getDesp(), weather.getTime());
				Log.i(TAG, weather.getCityName() + weather.getCityCode() + weather.getMinTemp() + weather.getMaxTemp()
						+ weather.getDesp() + weather.getTime());
				Log.v(TAG, "cityID:" + cityId);
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						updateView(cityId, layoutList.get(cityId));
						Toast.makeText(WeatherActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(WeatherActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
					}
				});
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
     * 判断数据更新的时间是否超过了4个小时 
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

            if (Math.abs(dt1.getTime() - today.getTime()) > 4*60*60*1000) {
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
			weather.setMinTemp(jsonWeather.getString("temp1"));
			weather.setMaxTemp(jsonWeather.getString("temp2"));
			weather.setDesp(jsonWeather.getString("weather"));
			weather.setTime(sdf.format(new Date()) + jsonWeather.getString("ptime"));
			Log.i(TAG, weather.getCityName() + weather.getCityCode() + weather.getMinTemp() + weather.getMaxTemp()
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
		
		case R.id.refresh_weather:
			String currentCityCode = weathers.get(currentPage()).getCityCode();
			int currentCityId = currentPage();
			ViewGroup currentGroup = layoutList.get(currentPage());
			updateWeather(currentCityCode);
			break;
			
		default:
			break;
		}
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return super.onMenuOpened(featureId, menu);
	}
	
	class UpdateWeatherAsy extends AsyncTask<Integer, Integer, Integer>{

		@Override
		protected void onPostExecute(Integer pageId) {
			Log.v(TAG, "updateWeather");
			updateView(pageId, layoutList.get(pageId));
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			return params[0];
		}
	}
}
