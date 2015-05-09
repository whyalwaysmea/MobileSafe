package com.ithaha.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 病毒数据库查询的业务类
 * @author hello
 *
 */
public class AntivirusDao {
	/**
	 * 查询一个md5是否在病毒数据库中存在
	 * @param md5
	 * @return
	 */
	public static boolean isVirus(String md5) {
		boolean result = false;
		String path = "/data/data/com.ithaha.mobilesafe/files/antivirus.db";
		// 打开病毒数据库
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select * from datable where md5=?", new String[]{md5});
		if(cursor.moveToNext()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
}
