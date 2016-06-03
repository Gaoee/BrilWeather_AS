package com.brilweather.DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.brilweather.model.City;
import com.brilweather.model.Weather;

import android.R.id;
import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WeatherDB {
	private final static String TAG = "lee";
	
	private static final int DB_VERSION = 2;
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
			weatherDB = new WeatherDB(context);
		}
		return weatherDB;
	}

	/*
	 * 查询所有的城市
	 * */
	public List<City> loadCitys() {
		List<City> cityList = new ArrayList<City>();
		Cursor cursor = db.query(DBHelper.CITY_TABLE_NAME, null, null, null, null, null, null);
		
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

	
	/*
	 * 查询所有选择的城市
	 * */
	public List<City> loadSelectedCity() {
		List<City> selectedCityList = new ArrayList<City>();
		Log.v(TAG, "loadSelectedCity");
		Cursor cursor = db.query(DBHelper.WEATHER_TABLE_NAME, new String[]{"id", "cityName", "cityCode"}, null, null, null, null, null);
		
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
	
	/*
	 *删除某个城市 
	 * */
	public int deleteCity(int cityId) {
		return db.delete(DBHelper.WEATHER_TABLE_NAME, "id = ?",new String[]{String.valueOf(cityId)});
	}
	
	/*
	 *删除某个城市 
	 * */
	public int deleteCity(String cityName) {
		return db.delete(DBHelper.WEATHER_TABLE_NAME, "cityName = ?",new String[]{String.valueOf(cityName)});
	}
	
	/*
	 * 添加一个城市
	 * */
	public long addSecletCity(String cityName, String cityCode) {
		ContentValues values = new ContentValues();
		values.put("cityName", cityName);
		values.put("cityCode", cityCode);
		return db.insert(DBHelper.WEATHER_TABLE_NAME, null, values);
	}
	
	/*
	 *更新天气 
	 * */
	public int updataWeather(String cityCode, String temp1, String temp2, String weatherDesp, String publicTime){
		ContentValues values = new ContentValues();
		values.put("temp1", temp1);
		values.put("temp2", temp2);
		values.put("weatherDesp", weatherDesp);
		values.put("publishTime", publicTime);
		
		return db.update(DBHelper.WEATHER_TABLE_NAME, values, "cityCode = ?", new String[]{cityCode});
	}
	
	public List<Weather> loadWeathers() {
		List<Weather> weathers = new ArrayList<Weather>();
		Cursor cursor = db.query(DBHelper.WEATHER_TABLE_NAME, null, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				Weather weather = new Weather();
				weather.setCityCode(cursor.getString(cursor.getColumnIndex("cityCode")));
				weather.setCityName(cursor.getString(cursor.getColumnIndex("cityName")));
				weather.setDesp(cursor.getString(cursor.getColumnIndex("weatherDesp")));
				weather.setId(cursor.getInt(cursor.getColumnIndex("id")));
				weather.setMaxTemp(cursor.getString(cursor.getColumnIndex("temp2")));
				weather.setMinTemp(cursor.getString(cursor.getColumnIndex("temp1")));
				weather.setTime(cursor.getString(cursor.getColumnIndex("publishTime")));
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
			weather.setDesp(cursor.getString(cursor.getColumnIndex("weatherDesp")));
			weather.setId(cursor.getInt(cursor.getColumnIndex("id")));
			weather.setMaxTemp(cursor.getString(cursor.getColumnIndex("temp2")));
			weather.setMinTemp(cursor.getString(cursor.getColumnIndex("temp1")));
			weather.setTime(cursor.getString(cursor.getColumnIndex("publishTime")));
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
