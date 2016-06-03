package com.brilweather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brilweather.DB.WeatherDB;
import com.brilweather.model.City;
import com.brilweather.sortlist.CharacterParser;
import com.brilweather.sortlist.ClearEditText;
import com.brilweather.sortlist.ConstactUtil;
import com.brilweather.sortlist.PinyinComparator;
import com.brilweather.sortlist.SideBar;
import com.brilweather.sortlist.SideBar.OnTouchingLetterChangedListener;
import com.brilweather.sortlist.SortAdapter;
import com.brilweather.sortlist.SortModel;
import com.example.brilweather.R;

public class CityshowAcivity extends Activity {
	private View mBaseView;
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;
	private ClearEditText mClearEditText;
	private Map<String, String> callRecords;
	private List<City> cityList;
	private WeatherDB weatherDB;

	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;

	private PinyinComparator pinyinComparator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_citys_show);
		try {
			weatherDB = WeatherDB.getInstanceDatabase(getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		initView();
		initData();
	}

	private void initView() {
		sideBar = (SideBar) this.findViewById(R.id.sidrbar);
		dialog = (TextView) this.findViewById(R.id.dialog);

		sortListView = (ListView) this.findViewById(R.id.sortlist);

	}

	private void initData() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		sideBar.setTextView(dialog);

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@SuppressLint("NewApi")
			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}
			}
		});

		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				SortModel city = (SortModel)adapter.getItem(position);
				if(weatherDB.addSecletCity(city.getName(), city.getCode()) != -1){
					Toast.makeText(CityshowAcivity.this, "添加成功！", 0).show();
					finish();
				}else {
					Toast.makeText(CityshowAcivity.this, "添加失败！", 0).show();
				}
			}
		});

		new ConstactAsyncTask().execute(0);

	}

	private class ConstactAsyncTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... arg0) {
			int result = -1;
			cityList = weatherDB.loadCitys();
			
			result = 1;
			return result;
		}
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == 1) {
				SourceDateList = filledData(cityList);
				// 根据a-z进行排序源数据
				Collections.sort(SourceDateList, pinyinComparator);
				adapter = new SortAdapter(CityshowAcivity.this, SourceDateList);
				sortListView.setAdapter(adapter);

				mClearEditText = (ClearEditText) CityshowAcivity.this
						.findViewById(R.id.filter_edit);
				mClearEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View arg0, boolean arg1) {
						mClearEditText.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
						
					}
				});
				// 根据输入框输入值的改变来过滤搜索
				mClearEditText.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
						filterData(s.toString());
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {

					}

					@Override
					public void afterTextChanged(Editable s) {
					}
				});
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

	/**
	 * 
	 * 为ListView填充数据
	 * @param cities
	 * @return
	 */
	private List<SortModel> filledData(List<City> cities) {
		List<SortModel> mSortList = new ArrayList<SortModel>();

		for (int i = 0; i < cities.size(); i++) {
			SortModel sortModel = new SortModel();
			sortModel.setName(cities.get(i).getCityName());
			sortModel.setCode(cities.get(i).getCityCode());
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(cities.get(i).getCityName());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<SortModel> filterDateList = new ArrayList<SortModel>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (SortModel sortModel : SourceDateList) {
				String name = sortModel.getName();
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}

		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}
}
