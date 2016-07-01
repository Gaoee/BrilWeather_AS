package com.brilweather.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.brilweather.model.City;
import com.brilweather.model.Province;
import com.brilweather.model.Weather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WeatherDB {
	private final static String TAG = "lee";
	
	private static final int DB_VERSION = 1;
	private static WeatherDB weatherDB;
	private SQLiteDatabase db;
	
	private WeatherDB(Context context){
		DBHelper myDbHelper = new DBHelper(context, DB_VERSION);
        try {
        	myDbHelper.createDataBase(DB_VERSION);
	 	} catch (IOException ioe) {
	 		ioe.printStackTrace();
	 		throw new Error("Unable to create database");
	 	}
	 	try {
	 		openDataBase();
	 	}catch(SQLException sqle){
	 		throw sqle;
	 	}
	}
	
	public void openDataBase() throws SQLException {
		String myPath = DBHelper.DB_PATH + DBHelper.DB_NAME;
		db = SQLiteDatabase.openDatabase(myPath, null, 
				SQLiteDatabase.OPEN_READWRITE);
		Log.v(TAG, "openDataBase db getVersion:" + db.getVersion());
	}
	
	public synchronized void closeDataBase(){
		if(db != null){
			db.close();
		}
		try {
			super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static WeatherDB getInstanceDatabase(Context context) throws Exception{
		if(weatherDB == null){
			Log.v(TAG, "weatherDB == null");
			weatherDB = new WeatherDB(context);
		}
		return weatherDB;
	}

	public List<Province> loadProvinces(){
		List<Province> provinceList = new ArrayList<>();
		Cursor cursor = db.query(DBHelper.PROVINCE_TABLE_NAME, null, null, null, null, null, null);

		if (cursor.moveToFirst()){
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("_id")));
				province.setProName(cursor.getString(cursor.getColumnIndex("name")));
				provinceList.add(province);
			}while(cursor.moveToNext());
		}

		if (cursor != null){
			cursor.close();
		}
		return provinceList;
	}

	/**
	 * 查询所有的城市
	 * */
	public List<City> loadCitys(int province_id) {
		List<City> cityList = new ArrayList<City>();
		Cursor cursor = db.query(DBHelper.CITY_TABLE_NAME, null,  "province_id = ?",
				new String[]{Integer.toString(province_id - 1)}, null, null, null);
		
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("_id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_num")));
				cityList.add(city);
			} while (cursor.moveToNext());
		}
		
		if (cursor != null) {
			cursor.close();
		}
		
		return cityList;
	}

	
	/**
	 * 查询所有选择的城市
	 * */
	public List<City> loadSelectedCity() {
		List<City> selectedCityList = new ArrayList<City>();
		Log.v(TAG, "loadSelectedCity");
		Cursor cursor = db.query(DBHelper.WEATHER_TABLE_NAME, new String[]{"id", "cityName", "cityCode"},
				null, null, null, null, "OrderId");
		
		Log.v(TAG, "cursor:" + cursor.getCount());
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("cityName")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("cityCode")));
				selectedCityList.add(city);
			} while (cursor.moveToNext());
		}
		
		if (cursor != null) {
			cursor.close();
		}
		
		return selectedCityList;
	}
	
	/**
	 *删除某个城市 
	 * */
	public int deleteCity(int cityId) {
		return db.delete(DBHelper.WEATHER_TABLE_NAME, "id = ?",new String[]{String.valueOf(cityId)});
	}
	
	/**
	 *删除某个城市 
	 * */
	public int deleteCity(String cityCode) {
		return db.delete(DBHelper.WEATHER_TABLE_NAME, "cityCode = ?",new String[]{String.valueOf(cityCode)});
	}
	
	/**
	 * 添加一个城市
	 * */
	public long addSecletCity(String cityName, String cityCode) {
		ContentValues values = new ContentValues();
		values.put("cityName", cityName);
		values.put("cityCode", cityCode);
		int orderId = getLastSeletCity();
		values.put("OrderId", orderId + 1);
		return db.insert(DBHelper.WEATHER_TABLE_NAME, null, values);
	}

	/**
	 * 获得当前最大OrderId
	 * @return
     */
	private int getLastSeletCity(){
		int lastCityOrderId = 0;
		Cursor cursor = db.rawQuery("SELECT max(OrderId) AS maxOrderId FROM " + DBHelper.WEATHER_TABLE_NAME
		 + " GROUP BY OrderId ", null);
		if (cursor.moveToFirst()){
			lastCityOrderId = cursor.getInt(cursor.getColumnIndex("maxOrderId"));
		}
		if (cursor != null){
			cursor.close();
		}
		Log.v(TAG, "lastCityOrderId:" + lastCityOrderId);
		return lastCityOrderId;
	}

	/**
	 * 重新排列城市顺序，
	 * @param cities 通过城市名来查找是不严谨的，以后要修改成cityCode来查找
	 * @return
     */
	public boolean updateWeatherOrder(List<City> cities){
		if(cities.size() == 0){
			return true;
		}

		for(int i = 0; i < cities.size(); i++){
			String cityCode = cities.get(i).getCityCode();
			ContentValues values = new ContentValues();
			values.put("OrderId", i+1);
			try{
				db.update(DBHelper.WEATHER_TABLE_NAME, values, "cityCode = ?", new String[]{cityCode});
			}catch (SQLException e){
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	/**
	 *更新天气
	 *
	 * */
	public int updateWeather(String cityCode, String observe, String forecast, String index, String updateTime){
		ContentValues values = new ContentValues();
		values.put("observe", observe);
		values.put("forecast", forecast);
		values.put("windex", index);
		values.put("updateTime", updateTime);
		
		return db.update(DBHelper.WEATHER_TABLE_NAME, values, "cityCode = ?", new String[]{cityCode});
	}

	/**
	 * 获得weatherTable中的所有天气数据   需要更改
	 * @return
     */
	public List<Weather> loadWeathers() {
		List<Weather> weathers = new ArrayList<Weather>();
		Cursor cursor = db.query(DBHelper.WEATHER_TABLE_NAME, null, null, null, null, null, "OrderId");
		if(cursor.moveToFirst()){
			do{
				Weather weather = new Weather();
				weather.setCityCode(cursor.getString(cursor.getColumnIndex("cityCode")));
				weather.setCityName(cursor.getString(cursor.getColumnIndex("cityName")));
				weather.setIndex(cursor.getString(cursor.getColumnIndex("windex")));
				weather.setId(cursor.getInt(cursor.getColumnIndex("id")));
				weather.setForecast(cursor.getString(cursor.getColumnIndex("forecast")));
				weather.setObserve(cursor.getString(cursor.getColumnIndex("observe")));
				weather.setUpdateTime(cursor.getString(cursor.getColumnIndex("updateTime")));
				weathers.add(weather);
			}while(cursor.moveToNext());
		}
		
		if(cursor != null){
			cursor.close();
		}
		return weathers;
	}
	
	public Weather loadWeather(String cityCode) {
		Weather weather = new Weather();
		Cursor cursor = db.query(DBHelper.WEATHER_TABLE_NAME, null, "cityCode = ?", new String[]{cityCode}, null, null, null);
		
		if(cursor.moveToFirst()){
			weather.setCityCode(cursor.getString(cursor.getColumnIndex("cityCode")));
			weather.setCityName(cursor.getString(cursor.getColumnIndex("cityName")));
			weather.setIndex(cursor.getString(cursor.getColumnIndex("windex")));
			weather.setId(cursor.getInt(cursor.getColumnIndex("id")));
			weather.setForecast(cursor.getString(cursor.getColumnIndex("forecast")));
			weather.setObserve(cursor.getString(cursor.getColumnIndex("observe")));
			weather.setUpdateTime(cursor.getString(cursor.getColumnIndex("updateTime")));
		}
		
		if (cursor != null) {
			cursor.close();
		}
		
		return weather;
	}
	
	public void beginTransaction() {
		db.beginTransaction();
	}
	
	public void commitTransaction() {
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public void cancelTransaction() {
		db.endTransaction();
	}
	
}
