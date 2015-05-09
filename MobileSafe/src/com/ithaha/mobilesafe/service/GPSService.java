package com.ithaha.mobilesafe.service;

import java.io.IOException;
import java.io.InputStream;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

public class GPSService extends Service {

	// 用到位置服务
	private LocationManager lm;
	private MyLocationListener listener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		lm = (LocationManager) getSystemService(LOCATION_SERVICE);

		listener = new MyLocationListener();
		// 给位置提供者设置条件
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = lm.getBestProvider(criteria, true);
		lm.requestLocationUpdates(provider, 0, 0, listener);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 取消监听位置服务
		lm.removeUpdates(listener);
		listener = null;
	}

	class MyLocationListener implements LocationListener {

		/**
		 * 当位置改变的时候回调
		 */
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub

			String longitude = "j:" + location.getLongitude() + "\n";
			String latitude = "w：" + location.getLatitude() + "\n";
			String accuracy = "a:" + location.getAccuracy() + "\n";

//			try {
//				// 把标准的GPS转换成火星坐标
//				InputStream is = getAssets().open("axisoffset.dat");
//				ModifyOffset offset = ModifyOffset.getInstance(is);
//				
//				PointDouble newPoint = offset.s2c(new PointDouble(location.getLongitude(), location.getLatitude()));
//				longitude = "j" + offset.X + "\n";
//				latitude = "w" + offset.Y + "\n";
//				
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			// 发短信给安全号码
			SharedPreferences sp = getSharedPreferences("confgi", MODE_PRIVATE);
			Editor edit = sp.edit();
			edit.putString("lastlocation", longitude + latitude + accuracy);
			edit.commit();
		}

		/**
		 * 当状态发生改变回调， 开启->关闭 or 关闭 -> 开启
		 */
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		/**
		 * 某一个位置提供者可使用了，就回调
		 */
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		/**
		 * 某一个位置提供者不可使用了，就回调
		 */
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

	}
}
