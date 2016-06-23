package com.brilweather;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.brilweather.DB.WeatherDB;
import com.brilweather.citymanage.CityAdapter;
import com.brilweather.citymanage.DeleteAdapter;
import com.brilweather.model.City;
import com.example.brilweather.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CitymanageActivity extends Activity implements OnClickListener ,AdapterView.OnItemClickListener{
	private static final String TAG = "LEE CitymanageActivity";
	
	private ListView mListView;
	private Button eidButton;
	private Button commitButton;
	private Button canelButton;
	private ImageButton addButton;
	private Button gobackButton;
	private View lineView;
	
	private float mDensity;
	private int mHiddenTopBarHeight;
	private int mAddButtonHeight;
	private int mWindowsWidth;
	
	private RelativeLayout relaLayout1;
	private RelativeLayout relaLayout2;
	
	private ArrayAdapter<City> mAdapter1;
	private ArrayAdapter<City> mAdapter2;
	
	private List<City> list;
	private List<City> citiesList;
	//private List<String> citiesCopyList;
	
	private Context mContext;
	private WeatherDB weatherDB;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_city_manage);
		
		mContext = this;
		try {
			weatherDB = WeatherDB.getInstanceDatabase(mContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		eidButton = (Button)findViewById(R.id.eidte_but);
		commitButton = (Button)findViewById(R.id.commit_but);
		canelButton = (Button)findViewById(R.id.cancel_but);
		addButton = (ImageButton)findViewById(R.id.add_but);
		gobackButton = (Button)findViewById(R.id.backtoweather_but);
		mListView = (ListView)findViewById(R.id.city_listview);
		lineView = (View)findViewById(R.id.line_view);
		
		relaLayout1 = (RelativeLayout)findViewById(R.id.top_bar1);
		relaLayout2 = (RelativeLayout)findViewById(R.id.top_bar2);
		
		eidButton.setOnClickListener(this);
		commitButton.setOnClickListener(this);
		canelButton.setOnClickListener(this);
		addButton.setOnClickListener(this);
		gobackButton.setOnClickListener(this);
	
		Log.v(TAG, "getCities");
		citiesList = new ArrayList<City>();
		//citiesCopyList = new ArrayList<String>();
		
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		mAdapter1 = new CityAdapter(this, R.layout.cities_mag_item, R.id.city_tex,
				citiesList);
		mListView.setAdapter(mAdapter1);
		mListView.setOnItemClickListener(this);

		mAdapter2 = new DeleteAdapter(this, R.layout.cities_magd_item,
				R.id.city_tex, citiesList);
		
		mDensity = getResources().getDisplayMetrics().density;
		mHiddenTopBarHeight = (int)(mDensity * 50 + 0.5);
		mAddButtonHeight = (int)(mDensity * 30);
		DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    mWindowsWidth = dm.widthPixels;    //得到宽度


	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		citiesList.clear();
		list = getCities();
		for (City city : list) {
			citiesList.add(city);
		}
		mAdapter1.notifyDataSetChanged();
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.eidte_but:
			animateClose(addButton);
			animateHorClose(lineView);
			animateClose(relaLayout1);
			animateOpen(relaLayout2);
			
			mListView.setAdapter(mAdapter2);
			//开始事务
			weatherDB.beginTransaction();
			break;
		
		case R.id.commit_but:
			animateOpen(addButton);
			animateHorOpen(lineView);
			animateClose(relaLayout2);
			animateOpen(relaLayout1);
			
			//提交事务
			weatherDB.commitTransaction();

			//重新排列城市顺序，假设citiesList是排序后的城市列表
			weatherDB.updateWeatherOrder(citiesList);
			//重新从数据库中加载城市数据
			citiesList.clear();
			list = getCities();
			for (City city : list) {
				citiesList.add(city);
			}
			mListView.setAdapter(mAdapter1);
			addButton.setVisibility(View.VISIBLE);
			break;
		
		case R.id.cancel_but:
			animateOpen(addButton);
			animateHorOpen(lineView);
			animateClose(relaLayout2);
			animateOpen(relaLayout1);
			
			//终止事务
			weatherDB.cancelTransaction();
			citiesList.clear();
			list = getCities();
			for (City city : list) {
				citiesList.add(city);
			}
			mListView.setAdapter(mAdapter1);
			addButton.setVisibility(View.VISIBLE);
			break;
			
		case R.id.add_but:
			Intent intent = new Intent(CitymanageActivity.this, CityshowAcivity.class);
			//假如没有城市数据时将intent的Action设置为“NoSelect”
			if (getCities().size() == 0){
				intent.setAction(CityshowAcivity.INTENT_ACTION_NOCITY);
			}
			startActivity(intent);
			finish();
			break;
			
		case R.id.backtoweather_but:
			finish();
			break;
			
		default:
			break;
		}
	}

	/**
	 * 点击后跳指定城市的天气信息页面，暂时还没有用
	 * @param parent
	 * @param view
	 * @param position
     * @param id
     */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListView listView = (ListView) parent;
		if(relaLayout1.getVisibility() == View.VISIBLE) {
//			City city = (City)listView.getItemAtPosition(position);
//			Intent intent = new Intent(CitymanageActivity.this, WeatherActivity.class);
//			intent.setAction(WeatherActivity.SCROLL_ACTION);
//			intent.putExtra("cityId", position);
//			Log.v(TAG, "position" + position);
//			intent.putExtra("cityName", city.getCityName());
//			startActivity(intent);
//			finish();
		}
	}
	
	private List<City> getCities() {
		List<City> citiesList = new ArrayList<City>();
		citiesList = weatherDB.loadSelectedCity();
		return citiesList;
	}
	
	
	private void animateOpen(final View view) {
		view.setVisibility(View.VISIBLE);
		ValueAnimator animator;
		if(view.getId() == R.id.add_but){
			animator = creatDropAnimator(view, 0, mAddButtonHeight);
		}else {
			animator = creatDropAnimator(view, 0, mHiddenTopBarHeight);
		}
		animator.start();
	}
	
	private void animateClose(final View view) {
		int origHeight = view.getHeight();
		ValueAnimator animator;
		animator = creatDropAnimator(view, origHeight, 0);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				view.setVisibility(View.GONE);
			}
		});
		animator.start();
	}
	
	
	
	private ValueAnimator creatDropAnimator(final View view, int start, int end) {
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (Integer)animation.getAnimatedValue();
				ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
				layoutParams.height = value;
				view.setLayoutParams(layoutParams);
			}
		});
		
		return animator;
	}
	
	
	private void animateHorOpen(final View view) {
		view.setVisibility(View.VISIBLE);
		ValueAnimator animator;
		animator = creatDropHorAnimator(view, 0, mWindowsWidth);
		animator.start();
	}
	
	private void animateHorClose(final View view) {
		int origWidth = view.getWidth();
		ValueAnimator animator;
		animator = creatDropHorAnimator(view, origWidth, 0);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				view.setVisibility(View.GONE);
			}
		});
		animator.start();
	}
	
	private ValueAnimator creatDropHorAnimator(final View view, int start, int end) {
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (Integer)animation.getAnimatedValue();
				ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
				layoutParams.width = value;
				view.setLayoutParams(layoutParams);
			}
		});
		
		return animator;
	}

	@Override
	public void onBackPressed() {
		if(relaLayout2.getVisibility() != View.GONE){
			//终止事务
			weatherDB.cancelTransaction();
		}
		super.onBackPressed();
	}


}
