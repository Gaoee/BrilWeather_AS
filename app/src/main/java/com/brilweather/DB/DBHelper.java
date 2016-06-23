package com.brilweather.DB;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static String TAG = "LEE DB";
	
	public static final String DB_PATH = "/data/data/com.example.brilweather/databases/";  
	public static final String DB_NAME = "db_weather.db";
	public static final String PROVINCE_TABLE_NAME = "provinces";
	public static final String WEATHER_TABLE_NAME = "weather";
	public static final String CITY_TABLE_NAME = "citys";

	private static final String CREATE_WEATHER = "create table if not exists " + WEATHER_TABLE_NAME
			+ "(id integer primary key autoincrement," 
			+ "cityName text," 
			+ "cityCode text not null unique,"
			+ "temp1 text,"
			+ "temp2 text,"
			+ "weatherDesp text,"
			+ "publishTime text)";

	private static final String ALTER_WEATHER_ADDORDERID = "ALTER TABLE " + WEATHER_TABLE_NAME
			+ " ADD OrderId integer";

	private final Context mContext;
	
	public DBHelper(Context context, int version) {
		super(context, DB_NAME, null, version);
		Log.v(TAG, "DBHelper version:" + version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	//只在新建数据库的时候的时候调用
	public void onMyCreate(SQLiteDatabase db) {
		Log.v(TAG, "onCreate");
		try {
			db.execSQL(CREATE_WEATHER);
			Log.v(TAG, "create weather table!");
			db.execSQL(ALTER_WEATHER_ADDORDERID);
			Log.v(TAG, "ALTER_WEATHER_ADDORDERID");
		} catch (SQLException e) {
			Log.v(TAG, e.toString());
		}

	}

	//在version发生变化的时候调用
	public void onMyUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v(TAG, "onUpgrade oldVersion:" + oldVersion);
		switch (oldVersion) {
			case 1:
//				try {
//					db.execSQL(ALTER_WEATHER_ADDORDERID);
//					Log.v(TAG, "ALTER_WEATHER_ADDORDERID");
//				} catch (SQLException e) {
//					Log.v(TAG, e.toString());
//				}

			default:
				break;
		}
	}

	/**
	 * 新建数据库，并进行版本控制
	 * @param newVersion
	 * @throws IOException
     */
	public void createDataBase(int newVersion) throws IOException{
		boolean dbExist = checkDataBase();
		
		if(dbExist){
			
		}else {
			//一定要在这里进行getReadableDatabase,不然copyDataBase会出错的
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (Exception e) {
				throw new Error("Error copying!");
			}
		}
		SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);

		//仿照了SQLiteOpenHelper中的数据库版本控制方法
		final int version = db.getVersion();
		Log.v(TAG, "createDataBase version: " + version);
		if (version != newVersion) {
			if (db.isReadOnly()) {
				throw new SQLiteException("Can't upgrade read-only database from version " +
						db.getVersion() + " to " + newVersion );
			}
			db.beginTransaction();
			try {
				if (version == 0) {
					onMyCreate(db);
				} else {
					if (version > newVersion) {
						onDowngrade(db, version, newVersion);
					} else {
						onMyUpgrade(db, version, newVersion);
					}
				}
				db.setVersion(newVersion);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
		db.close();
	}

	/**
	 * 检测程序数据库是否已经存在
	 * @return
     */
	private boolean checkDataBase() {
		Log.v(TAG, "checkDataBase");
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


	/**
	 * 将Assets中的数据拷入程序文件夹中
	 * @throws IOException
     */
	private void copyDataBase() throws IOException{
		Log.v(TAG, "copyDataBase");
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
