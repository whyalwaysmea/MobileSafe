package com.ithaha.mobilesafe.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ithaha.mobilesafe.R;
import com.ithaha.mobilesafe.receiver.MyWidget;
import com.ithaha.mobilesafe.utils.SystemInfoUtils;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViews.RemoteView;

public class UpdateWidgetService extends Service {

	private ScreenOffReceiver receiver;
	private ActivityManager am;
	private Timer timer;
	private TimerTask task;
	private String TAG = "UpdateWidgetService";
	private AppWidgetManager awm;
	
	
	private ScreenOffReceiver offReceiver;
	private ScreenOnReceiver onReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class ScreenOffReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 屏幕锁屏了
			Log.i(TAG, "屏幕锁屏了");
			stopTimer();
		}
	}
	
	private class ScreenOnReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 屏幕解锁了
			Log.i(TAG, "屏幕解锁了");
			startTimer();
		}
		
	}
	
	@Override
	public void onCreate() {
		onReceiver = new ScreenOnReceiver();
		offReceiver = new ScreenOffReceiver();
		registerReceiver(onReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(offReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		awm = AppWidgetManager.getInstance(this);
		startTimer();
		
		super.onCreate();
	}

	private void startTimer() {
		if(timer == null && task == null) {
			timer = new Timer();
			task = new TimerTask() {
				
				@Override
				public void run() {
					Log.i(TAG, "更新Widget");
					// 设置更新的组件
					ComponentName provider = new ComponentName(UpdateWidgetService.this, MyWidget.class);
					RemoteViews view = new RemoteViews(getPackageName(), R.layout.process_widget);
					view.setTextViewText(R.id.process_count, "正在运行的进程:" + SystemInfoUtils.getRunningProcessCount(getApplicationContext()) + "个");
					view.setTextViewText(R.id.process_memory, "可用内存:" + Formatter.formatFileSize(getApplicationContext(), SystemInfoUtils.getAvailMem(getApplicationContext())));
					
					// 描述一个动作，这个动作是由另外一个程序执行的
					// 自定义一个广播，来发送清理进程的要求
					Intent intent = new Intent();
					intent.setAction("com.ithaha.mobilesafe.killall");
					
					PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					view.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);
					
					awm.updateAppWidget(provider, view);
					
				}
			};
			timer.schedule(task, 0, 3000);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(offReceiver);
		unregisterReceiver(onReceiver);
		offReceiver = null;
		onReceiver = null;
		if(timer != null && task != null) {
			stopTimer();
		}
		
	}

	private void stopTimer() {
		timer.cancel();
		task.cancel();
		timer = null;
		task = null;
	}
}
