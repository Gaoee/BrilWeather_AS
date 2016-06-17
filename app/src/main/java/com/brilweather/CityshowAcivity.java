package com.brilweather;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brilweather.DB.WeatherDB;
import com.brilweather.model.City;
import com.brilweather.model.Province;
import com.brilweather.model.Weather;
import com.brilweather.sortlist.CharacterParser;
import com.brilweather.sortlist.ClearEditText;
import com.brilweather.sortlist.PinyinComparator;
import com.brilweather.sortlist.SideBar;
import com.brilweather.sortlist.SideBar.OnTouchingLetterChangedListener;
import com.brilweather.sortlist.SortAdapter;
import com.brilweather.sortlist.SortModel;
import com.example.brilweather.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CityshowAcivity extends Activity {
	private static  final String TAG = "CityshowActivity";

	public static final String INTENT_ACTION_NOCITY = "NoCity";

	private static final int PROVINCE_LEVEL = 1;
	private static final int CITY_LEVEL = 2;
	private int ChoiceLevel;

	private View mBaseView;
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;
	private ClearEditText mClearEditText;
	private Map<String, String> callRecords;
	private List<? extends Object> contentList;
	private WeatherDB weatherDB;

	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;

	private PinyinComparator pinyinComparator;

	private ProgressDialog progressDialog;

	private int mTochSlop;
	private float mFristY;
	private float mCurrentY;
	private int dirction;
	private static final int SCROLL_UP = 0;
	private static final int SCROLL_DOWM = 1;
	private ObjectAnimator mAnimator;
	private int cleanTextViewVisitState;
	private final int VISITABLE = 0;
	private final int DISVISTABLE = 1;

	InputMethodManager imm;


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
		ChoiceLevel = PROVINCE_LEVEL;
		cleanTextViewVisitState = VISITABLE;
		mTochSlop = ViewConfiguration.get(this).getScaledTouchSlop();

		initView();
		initData();
	}

	private void initView() {
		sideBar = (SideBar) this.findViewById(R.id.sidrbar);
		dialog = (TextView) this.findViewById(R.id.dialog);
		mClearEditText = (ClearEditText) this.findViewById(R.id.filter_edit);
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
					//有问题在分析吧
					sortListView.setSelection(position + 1);
				}
			}
		});

		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				if(ChoiceLevel == PROVINCE_LEVEL){
					//根据选择的省，加载相应的城市
					SortModel province = (SortModel) adapter.getItem(position - 1);
					ChoiceLevel = CITY_LEVEL;		//一定要在ConstentAsyncTask().execute之前，因为里面用的了ChoiceLevel
					new ConstentAsyncTask().execute(Integer.valueOf(province.getCode()));
				}else if(ChoiceLevel == CITY_LEVEL) {

					SortModel city = (SortModel) adapter.getItem(position - 1);
					if (weatherDB.addSecletCity(city.getName(), city.getCode()) != -1) {
						Toast.makeText(CityshowAcivity.this, "添加成功！", Toast.LENGTH_SHORT).show();
						Intent i = getIntent();
						Log.v(TAG," i.getAction():" + i.getAction());
						if (i.getAction() == INTENT_ACTION_NOCITY){
							Intent intent = new Intent(CityshowAcivity.this, WeatherActivity.class);
							startActivity(intent);
						}
						finish();
					} else {
						Toast.makeText(CityshowAcivity.this, "添加失败！", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		View header = new View(this);
		header.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
				85));
		Log.v(TAG, "mClearEditText.getMeasuredHeight():" + mClearEditText.getMeasuredHeight());
		sortListView.addHeaderView(header, null, false);
		//设置自动显示、隐藏ClearTextView
		sortListView.setOnTouchListener(new View.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()){
					case MotionEvent.ACTION_DOWN:
						mFristY = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:
						mCurrentY = event.getY();
						if(mCurrentY - mFristY > mTochSlop){
							dirction = SCROLL_UP;
						}else if (mFristY - mCurrentY > mTochSlop){
							dirction = SCROLL_DOWM;
						}

						if (dirction == SCROLL_UP){
							if (cleanTextViewVisitState == DISVISTABLE){
								clearTextVeiwAnim(cleanTextViewVisitState);//show
								Log.v(TAG, "show clearText by scroll");
								cleanTextViewVisitState = VISITABLE;
							}
						}else if (dirction == SCROLL_DOWM && sortListView.getFirstVisiblePosition() > 0){	//因为这边刷新数据是用新建adapter来做的，每次刷新数据都会
							Log.v(TAG, "cleanTextViewVisitState:" + cleanTextViewVisitState);				//进入这个判断，导致有时候会ClearTextView消失
							Log.v(TAG, "sortListView.getSelectedItemPosition():" + sortListView.getFirstVisiblePosition());
							if (cleanTextViewVisitState == VISITABLE){
								clearTextVeiwAnim(cleanTextViewVisitState);//hide
								Log.v(TAG, "hide clearText by scroll");
								cleanTextViewVisitState = DISVISTABLE;
							}
						}
						break;
					case MotionEvent.ACTION_UP:
						break;
				}
				return false;
			}
		});

		new ConstentAsyncTask().execute(0);

	}

	/**
	 * clearTextVeiwAnim显示、隐藏动画
	 * @param isShowFlag
     */
	private void clearTextVeiwAnim(int isShowFlag){
		if (mAnimator != null && mAnimator.isRunning()){
			mAnimator.cancel();
		}
		if(isShowFlag == VISITABLE){
			mAnimator = ObjectAnimator.ofFloat(mClearEditText, "translationY",
					mClearEditText.getTranslationY(), -mClearEditText.getHeight());
			mAnimator.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							HideKeyboard(mClearEditText);
						}
					}).run();

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					mClearEditText.setVisibility(View.GONE);
					sideBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});
		}else{
			mAnimator = ObjectAnimator.ofFloat(mClearEditText, "translationY",
					mClearEditText.getTranslationY(), 0);
			mAnimator.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
					sideBar.setVisibility(View.GONE);
					mClearEditText.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationEnd(Animator animation) {

				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});
		}
		mAnimator.start();
	}

	private class ConstentAsyncTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... arg0) {
			int result = -1;
			if(ChoiceLevel == PROVINCE_LEVEL) {
				contentList = weatherDB.loadProvinces();
			}else if (ChoiceLevel == CITY_LEVEL){
				contentList = weatherDB.loadCitys(arg0[0]);
			}
			result = 1;
			return result;
		}
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == 1) {
				SourceDateList = filledData(contentList);
				// 根据a-z进行排序源数据
				Collections.sort(SourceDateList, pinyinComparator);

				adapter = new SortAdapter(CityshowAcivity.this, SourceDateList);
				sortListView.setAdapter(adapter);
				if (mClearEditText.getVisibility() == View.GONE){
					clearTextVeiwAnim(cleanTextViewVisitState);//show
					cleanTextViewVisitState = VISITABLE;
					Log.v(TAG, "clearTextVeiwAnim show");
				}
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
	 * @param contents
	 * @return
	 */
	private List<SortModel> filledData(List<? extends Object> contents) {
		List<SortModel> mSortList = new ArrayList<SortModel>();

		for (int i = 0; i < contents.size(); i++) {
			String name = new String();
			String code = new String();
			if(ChoiceLevel == PROVINCE_LEVEL){
				Province province = (Province)(contents.get(i));
				name = province.getProName();
				code = Integer.toString(province.getId());
			}else if (ChoiceLevel == CITY_LEVEL) {
				City city = (City) (contents.get(i));
				name = city.getCityName();
				code = city.getCityCode();
			}
			SortModel sortModel = new SortModel();
			sortModel.setName(name);
			sortModel.setCode(code);

			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(name);
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

	/**
	 *  隐藏虚拟键盘
	 */
	public void HideKeyboard(View v) {
		if (imm == null){
			imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		}
		if ( imm.isActive( ) ) {
			imm.hideSoftInputFromWindow( v.getApplicationWindowToken( ) , 0 );
		}
	}

	@Override
	public void onBackPressed() {
		if(ChoiceLevel == CITY_LEVEL) {
			ChoiceLevel = PROVINCE_LEVEL;
			new ConstentAsyncTask().execute(0);
		}else{
			super.onBackPressed();
		}
	}
}
