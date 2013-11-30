package com.mogu.wifilocation.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbHandler {

	private String packageName;

	private InputStream inputStream;

	private SQLiteDatabase sqliteDB;

	public DbHandler(String packageName, InputStream inputStream) {
		this.packageName = packageName;
		this.inputStream = inputStream;
		init();
	}

	public void init() {
		if (!new File("/data/data/" + packageName + "/database.sqlite").exists()) {
			try {
				FileOutputStream out = new FileOutputStream("data/data/" + packageName + "/database.sqlite");

				byte[] buffer = new byte[1024];
				int readBytes = 0;

				while ((readBytes = inputStream.read(buffer)) != -1)
					out.write(buffer, 0, readBytes);

				inputStream.close();
				out.close();
			} catch (IOException e) {
			}
		}

		sqliteDB = SQLiteDatabase.openOrCreateDatabase("/data/data/" + packageName + "/database.sqlite", null);
	}
	
	
	/**
	 * 根据x和y坐标查询位置信息
	 * @param x
	 * @param y
	 * @return
	 */
	public List<Map<String, Object>> queryLocation(double x, double y){
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		Cursor cursor = sqliteDB.rawQuery("select * from location where loc_x=? and loc_y=?", new String[]{String.valueOf(x), String.valueOf(y)});
		while (cursor.moveToNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("location_id", cursor.getInt(cursor.getColumnIndex("location_id")));
			resultList.add(map);
		}
		return resultList;
	}
	
	/**
	 * 根据location_id查询x和y坐标
	 * @param locationId
	 * @return
	 */
	public Map<String, Object> queryLocationById(Integer locationId){
		Map<String, Object> map = new HashMap<String, Object>();
		Cursor cursor = sqliteDB.rawQuery("select * from location where location_id=?", new String[]{String.valueOf(locationId)});
		while (cursor.moveToNext()) {
			map.put("x", cursor.getInt(cursor.getColumnIndex("loc_x")));
			map.put("y", cursor.getInt(cursor.getColumnIndex("loc_y")));
		}
		return map;
	}
	
	
	// 插入位置信息
	public void insertLoc(double x, double y){
		ContentValues contenValues = new ContentValues();
		contenValues.put("loc_x", x);
		contenValues.put("loc_y", y);
		sqliteDB.insert("location", null, contenValues);
	}
	
	// 插入wifi信息
	public void insertWifi(String ssid, double level, int locationId) {
		ContentValues contenValues = new ContentValues();
		contenValues.put("ssid", ssid);
		contenValues.put("wifi_level", level);
		contenValues.put("location_id", locationId);
		sqliteDB.insert("wifi", null, contenValues);
	}
	
	public List<Map<String, Object>> queryWifiBySsid(String ssid) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		Cursor cursor = sqliteDB.rawQuery("select * from wifi where ssid=?", new String[]{ssid});
		while (cursor.moveToNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("wifi_level", cursor.getInt(cursor.getColumnIndex("wifi_level")));
			map.put("location_id", cursor.getInt(cursor.getColumnIndex("location_id")));
			resultList.add(map);
		}
		return resultList;
	}
}
