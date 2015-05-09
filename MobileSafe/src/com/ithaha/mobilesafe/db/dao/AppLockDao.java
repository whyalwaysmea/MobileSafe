package com.ithaha.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.ithaha.mobilesafe.db.AppLockDBOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 程序所的dao
 * @author hello
 *
 */
public class AppLockDao {
	private AppLockDBOpenHelper helper;
	private Context context;
	/**
	 * 构造方法
	 * @param context
	 */
	public AppLockDao(Context context) {
		helper = new AppLockDBOpenHelper(context);
		this.context = context;
	}
	
	/**
	 * 添加一个要锁定的应用程序的包名
	 */
	public void add(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("packname", packname);
		db.insert("applock", null, values);
		db.close();
		Intent intent = new Intent();
		intent.setAction("com.ithaha.mobilesafe.applockchange");
		context.sendBroadcast(intent);
		
	}
	
	/**
	 * 删除锁定的应用程序的包名
	 */
	public void delete(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("applock", "packname = ?", new String[]{packname});
		db.close();
		Intent intent = new Intent();
		intent.setAction("com.ithaha.mobilesafe.applockchange");
		context.sendBroadcast(intent);
	}
	
	/**
	 * 查询应用程序是否上锁了
	 * @param packname
	 * @return
	 */
	public boolean find(String packname) {
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("applock", null, "packname = ?", new String[]{packname}, null, null, null);
		if(cursor.moveToNext()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
	
	/**
	 * 查询全部的包名
	 * @param packname
	 * @return
	 */
	public List<String> findAll() {
		List<String> protectPacknames = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("applock", new String[]{"packname"}, null, null, null, null, null);
		if(cursor.moveToNext()) {
			protectPacknames.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return protectPacknames;
	}
}
