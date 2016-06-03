package com.brilweather.DB;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.runner.Version;

import android.R.anim;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static String TAG = "LEE";
	
	public static final String DB_PATH = "/data/data/com.example.brilweather/databases/";  
	public static final String DB_NAME = "db_weather.db";
	public static final String PROVINCE_TABLE_NAME = "provinces";
	public static final String WEATHER_TABLE_NAME = "weather";
	public static final String CITY_TABLE_NAME = "citys";
	
	private static final String CREATE_Weather= "create table if not exists " + WEATHER_TABLE_NAME 
			+ "(id integer primary key autoincrement," 
			+ "cityName text," 
			+ "cityCode text,"
			+ "temp1 text,"
			+ "temp2 text,"
			+ "weatherDesp text,"
			+ "publishTime text)";
	
	private final Context mContext;
	
	public DBHelper(Context context, int version) {
		super(context, DB_NAME, null, version);
		mContext = context;
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_Weather);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
		case 1:
			db.execSQL(CREATE_Weather);

		default:
			break;
		}
	}
	
	public void createDataBase(int version) throws IOException{
		boolean dbExist = checkDataBase();
		
		if(dbExist){
			
		}else {
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (Exception e) {
				throw new Error("Error copying!");
			}
		}
		Log.v(TAG, "create weather.db!" + version);
		SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
		switch (version) {
		case 2:
			try {
				db.execSQL(CREATE_Weather);
			} catch (SQLException e) {
				Log.v(TAG, e.toString());
			}
			Log.v(TAG, "create weather tabble!");
		default:
			break;
		}
		
		db.close();
	}
	
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH +DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, 
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		if(checkDB != null){
			checkDB.close();
		}
		
		return checkDB != null ? true : false;
	}
	
	private void copyDataBase() throws IOException{
		InputStream myInput = mContext.getAssets().open(DB_NAME);
		String outFileNameString = DB_PATH +DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFileNameString);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		
		myOutput.flush();
		myOutput.close();
		myInput.close();
		
	}
}
