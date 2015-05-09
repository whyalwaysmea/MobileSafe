package com.ithaha.mobilesafe.service;

import java.util.List;

import com.ithaha.mobilesafe.EnterPwdActivity;
import com.ithaha.mobilesafe.db.dao.AppLockDao;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * 监视系统程序的运行状态
 * @author hello
 *
 */
public class WatchDogService extends Service {

	private ActivityManager am;
	private boolean flag;
	private AppLockDao dao;
	private InnerReceiver innerReceiver;
	private String tempStopPackname;
	private ScreenOffReceiver receiver;
	private ScreenOffReceiver offReceiver;
	
	private List<String> protectPacknames;
	private Intent intent;
	private DataChangeReceiver dataChangeReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	
	private class InnerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("接收到了临时停止的广播");
			tempStopPackname = intent.getStringExtra("packname");
		}
	}

	private class ScreenOffReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 屏幕锁屏了
			tempStopPackname = null;
		}
	}
	
	private class DataChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			protectPacknames = dao.findAll();
		}
	}
	
	@Override
	public void onCreate() {
		
		// 动态注册广播
		dataChangeReceiver = new DataChangeReceiver();
		registerReceiver(dataChangeReceiver, new IntentFilter("com.ithaha.mobilesafe.applockchange"));
		
		offReceiver = new ScreenOffReceiver();
		registerReceiver(offReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		innerReceiver = new InnerReceiver();
		registerReceiver(innerReceiver, new IntentFilter("com.ithaha.mobilesafe.tempstop"));
		
		dao = new AppLockDao(this);
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		protectPacknames = dao.findAll();
		flag = true;
		
		new Thread(){
			public void run() {
				while(flag){
					List<RunningTaskInfo> infos = am.getRunningTasks(1);
					String packageName = infos.get(0).topActivity.getPackageName();
//					System.out.println("当前用户操作的程序:" + packageName);
					if(dao.find(packageName)) {		// 查询数据库太慢了，消耗内存,改成查询内存  findAll()
//					if(protectPacknames.contains(packageName)) {
						// 判断这个应用程序是否需要临时保护
						if(packageName.equals(tempStopPackname)) {
							
						} else {
							intent = new Intent(getApplicationContext(),EnterPwdActivity.class);
							// 服务是没有任务栈信息的，在服务开启activity，要指定这个activity运行的任务栈
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							// 设置要保护程序的包名
							intent.putExtra("packname", packageName);
							startActivity(intent);
						}
					}
					
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
		
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(innerReceiver);
		innerReceiver = null;
		
		unregisterReceiver(offReceiver);
		offReceiver = null;
		
		unregisterReceiver(dataChangeReceiver);
		dataChangeReceiver = null;
		
		flag = false;
		super.onDestroy();
	}
	
}
