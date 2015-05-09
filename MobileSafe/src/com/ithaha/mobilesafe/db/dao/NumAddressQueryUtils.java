package com.ithaha.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NumAddressQueryUtils {

	private static String path = "data/data/com.ithaha.mobilesafe/files/address.db";

	/**
	 * 传一个号码进来，返回一归属地回去
	 * 
	 * @param number
	 * @return
	 */
	public static String queryNumber(String number) {
		String address = number;
		// path 把address.db这个数据库拷贝到data/data/《包名》/files/address.db
		SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		// 手机号码 13 14 15 16 18
		// 手机号码的正则表达式
		if (number.matches("^1[34568]\\d{9}$")) {
			// 手机号码

			Cursor cursor = database
					.rawQuery(
							"select location from data2 where id = (select outkey from data1 where id = ?)",
							new String[] { number.substring(0, 7) });

			while (cursor.moveToNext()) {

				String location = cursor.getString(0);
				address = location;
			}
			cursor.close();

		} else {
			// 其他的电话号码
			switch (number.length()) {
			case 3:
				// 110
				address = "匪警号码";
				break;
			case 4:
				// 5554
				address = "模拟器";
				break;
			case 5:
				// 10086
				address = "客服电话";
				break;
			case 7:
				//
				address = "本地号码";
				break;

			case 8:
				address = "本地号码";
				break;

			default:
				// /处理长途电话 10
				if (number.length() > 10 && number.startsWith("0")) {
					// 010-59790386
					Cursor cursor = database.rawQuery(
							"select location from data2 where area = ?",
							new String[] { number.substring(1, 3) });

					while (cursor.moveToNext()) {
						String location = cursor.getString(0);
						address = location.substring(0, location.length() - 2);
					}
					cursor.close();

					// 0855-59790386
					cursor = database.rawQuery(
							"select location from data2 where area = ?",
							new String[] { number.substring(1, 4) });
					while (cursor.moveToNext()) {
						String location = cursor.getString(0);
						address = location.substring(0, location.length() - 2);

					}
				}

				break;
			}

		}

		return address;
	}

}
