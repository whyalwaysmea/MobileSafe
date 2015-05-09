package com.ithaha.mobilesafe;

import java.util.Timer;

import com.ithaha.mobilesafe.service.AutoClearService;
import com.ithaha.mobilesafe.utils.ServiceUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TaskSettingActivity extends Activity {

	private CheckBox cb_show_system;
	private CheckBox cb_auto_clear;
	private SharedPreferences sp;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_setting);
		
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		cb_show_system = (CheckBox) findViewById(R.id.cb_show_system);
		cb_auto_clear = (CheckBox) findViewById(R.id.cb_auto_clear);
		cb_show_system.setChecked(sp.getBoolean("showsystem", false));
		cb_show_system.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor edit = sp.edit();
				edit.putBoolean("showsystem", isChecked);
				edit.commit();
			}
		});
		
		cb_auto_clear.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// 锁屏的广播事件，是一个特殊的广播事件，在清单文件里面配置广播接收者是不会起效的
				// 只能在代码里面注册才会生效
				Intent intent = new Intent(TaskSettingActivity.this,AutoClearService.class);
				if(isChecked) {
					// 开启服务
					startService(intent);
				} else {
					stopService(intent);
				}
			}
		});
	}
	
	@Override
	protected void onStart() {
		boolean running = ServiceUtils.isServiceRunning(TaskSettingActivity.this, "com.ithaha.mobilesafe.service.AutoClearService");
		cb_auto_clear.setChecked(running);
		super.onStart();
	}
}
