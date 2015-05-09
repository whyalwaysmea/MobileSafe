package com.ithaha.mobilesafe.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import com.ithaha.mobilesafe.domain.BackUpSms;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;

/**
 * 短信工具类
 * @author hello
 *
 */
public class SmsUtils {
	

	/**
	 * 备份短信的回调接口
	 * @author hello
	 *
	 */
	public interface BackUpCallBack {
		/**
		 * 设置进度条的最大值
		 * @param max	总进度
		 */
		public void beforeBackup(int max);
		
		/**
		 * 备份的进度
		 * @param progress	当前进度
		 */
		public void onSmsBackup(int progress);
	}
	
	
	/**
	 * 备份用户的短信
	 * @param context
	 * @param BackUpCallBack	备份短信的接口
	 * @throws Exception 
	 */
	public static void backupSms(Context context, BackUpCallBack callBack) throws Exception {
		
		ContentResolver resolver = context.getContentResolver();
		
		
		File file = new File(Environment.getExternalStorageDirectory(),"backup.xml");
		FileOutputStream fos = new FileOutputStream(file);
		
		// 把用户的短信一条条的读出来，按照一定的格式写入到.xml文件里
		// 获取XML文件的序列化器
		XmlSerializer serializer = Xml.newSerializer();
		// 初始化生成器
		serializer.setOutput(fos, "utf-8");
		serializer.startDocument("utf-8", true);
		
		serializer.startTag(null, "smss");
		
		Uri uri = Uri.parse("content://sms/");
		Cursor cursor = resolver.query(uri, new String[]{"body","address","type","date"}, null, null, null);
		// 设置进度条的最大值
		int max = cursor.getCount();
//		pd.setMax(max);
		
		callBack.beforeBackup(max);
		int process = 0;

		serializer.attribute(null, "max", max+"");
		while(cursor.moveToNext()) {
			String body = cursor.getString(0);
			String address = cursor.getString(1);
			String type = cursor.getString(2);
			String date = cursor.getString(3);
			
			serializer.startTag(null, "sms");
			
			serializer.startTag(null, "body");
			serializer.text(body);
			serializer.endTag(null, "body");

			serializer.startTag(null, "address");
			serializer.text(address);
			serializer.endTag(null, "address");
			
			serializer.startTag(null, "type");
			serializer.text(type);
			serializer.endTag(null, "type");
			
			serializer.startTag(null, "date");
			serializer.text(date);
			serializer.endTag(null, "date");
			
			serializer.endTag(null, "sms");
			
			// 备份的进度
			process ++;
//			pd.setProgress(process);
			callBack.onSmsBackup(process);
		}
		cursor.close();
		serializer.endTag(null, "smss");
		serializer.endDocument();
		fos.close();
	}
	
	/**
	 * 还原短信
	 * @param context
	 * @param flag	是否清理原有的短信
	 */
	public static void restoreSms(Context context, boolean flag) {
		
		List<BackUpSms> backUpSmsList = null;
		BackUpSms sms = null;
		
		Uri uri = Uri.parse("content://sms/");
		if(flag) {
			context.getContentResolver().delete(uri, null, null);
		} 
		
		try {
			// 1.读取SD卡上的xml文件
			File path = new File(Environment.getExternalStorageDirectory(),"backup.xml");
			FileInputStream fis = new FileInputStream(path);
			
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(fis, "utf-8");
			
			int eventType = parser.getEventType();
			// 2.读取max
			
			// 3.读取每一条的短信信息,body,date,type,address
			String max = "";
			String body = "";
			String date = "";
			String type = "";
			String address = "";
		
			while(eventType != XmlPullParser.END_DOCUMENT) {
				String tagName = parser.getName();
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if("smss".equals(tagName)) {
						backUpSmsList = new ArrayList<BackUpSms>();
						max = parser.getAttributeValue(null, "max");
					} else if("sms".equals(tagName)) {
						sms = new BackUpSms();
						sms.setMax(Integer.valueOf(max));
					} else if("body".equals(tagName)) {
						sms.setBody(parser.nextText());
					} else if("address".equals(tagName)) {
						sms.setAddress(parser.nextText());
					} else if("type".equals(tagName)) {
						sms.setType(parser.nextText());
					} else if("date".equals(tagName)) {
						sms.setDate(parser.nextText());
					}
					
					break;
				case XmlPullParser.END_TAG:
					if("sms".equals(tagName)) {
						backUpSmsList.add(sms);
					}
					break;
				default:
					break;
				}
				eventType = parser.next();				// 获得下一个事件类型
			}
			
			// 4.把短信插入到系统短信应用
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ContentValues values = new ContentValues();
		for(BackUpSms backSms : backUpSmsList) {
			values.put("body", backSms.getBody());
			values.put("date", backSms.getDate());
			values.put("type", backSms.getType());
			values.put("address", backSms.getAddress());
			context.getContentResolver().insert(uri, values);
		}
		
		
		
		
	}
}
