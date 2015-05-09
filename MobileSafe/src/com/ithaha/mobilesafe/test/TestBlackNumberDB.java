package com.ithaha.mobilesafe.test;

import java.util.Random;

import com.ithaha.mobilesafe.db.BlackNumberDBOpenHelper;
import com.ithaha.mobilesafe.db.dao.BlackNumberDao;

import android.test.AndroidTestCase;

public class TestBlackNumberDB extends AndroidTestCase {

	public void testCreateDB() throws Exception {
		BlackNumberDBOpenHelper helper = new BlackNumberDBOpenHelper(getContext());
		helper.getWritableDatabase();
	}
	
	public void testAdd() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		long basenumber = 13500000000l;
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			dao.add(String.valueOf(basenumber+i), String.valueOf(random.nextInt(3)+1));
		}
	}
}
