package com.ithaha.mobilesafe;

import com.ithaha.mobilesafe.service.AddressService;
import com.ithaha.mobilesafe.service.CallSmsSafeService;
import com.ithaha.mobilesafe.service.WatchDogService;
import com.ithaha.mobilesafe.ui.SettingClickView;
import com.ithaha.mobilesafe.ui.SettingItemView;
import com.ithaha.mobilesafe.utils.ServiceUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SettingActivity extends Activity {
	
	/**
	 * 设置是否自动更新
	 */
	private SettingItemView siv_update;
	private SharedPreferences sp;
	/**
	 * 设置是否来电归属地显示
	 */
	private SettingItemView siv_show_address;
	private Intent showAddressIntent;
	
	// 设置归属地显示框的背景
	private SettingClickView scv_changebg;
	
	// 黑名单拦截
	private SettingItemView siv_callsms_safe;
	private Intent callSmsSafeIntent;
	
	// 程序锁
	private SettingItemView siv_watchdog;
	private Intent watchdogIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		// 设置自动升级
		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean update = sp.getBoolean("update", false);
		
		if(update) {
			// 自动升级已经开启
			siv_update.setChecked(true);
		} else {
			// 自动升级已经关闭
			siv_update.setChecked(false);
		}
		
		siv_update.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Editor edit = sp.edit();
				// 判断是否有选中
				if(siv_update.isChecked()) {
					// 关闭自动升级了
					siv_update.setChecked(false);
					edit.putBoolean("update", false);
					
				} else {
					// 开启了自动升级
					siv_update.setChecked(true);
					edit.putBoolean("update", true);
				}
				edit.commit();
			}
		});
		
		// 设置来电归属地显示
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		showAddressIntent = new Intent(this, AddressService.class);
		boolean serviceRunning = ServiceUtils.isServiceRunning(this, "com.ithaha.mobilesafe.service.AddressService");
		
		if(serviceRunning) {
			// 来电归属地服务正在运行
			siv_show_address.setChecked(true);
		} else {
			// 来电归属地服务没有运行了
			siv_show_address.setChecked(false);
		}
		
		siv_show_address.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 判断是否有选中
				if(siv_show_address.isChecked()) {
					// 关闭归属地显示了
					siv_show_address.setChecked(false);
					stopService(showAddressIntent);
				} else {
					// 开启归属地显示了
					siv_show_address.setChecked(true);
					startService(showAddressIntent);
				}
			}
		});
		
		// 设置号码归属地的背景
		final String[] items = {"半透明","活力橙","卫士蓝","金属灰","苹果绿"};
		scv_changebg = (SettingClickView) findViewById(R.id.scv_changebg);
		scv_changebg.setTitle("归属地提示框风格");
		int which = sp.getInt("which", 0);
		scv_changebg.setDesc(items[which]);
		scv_changebg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				int dd = sp.getInt("which", 0);
				
				// 弹出一个单选框
				AlertDialog.Builder builder = new Builder(SettingActivity.this);
				builder.setTitle("归属地提示框风格");
				builder.setSingleChoiceItems(items, dd, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 保存选择参数
						Editor edit = sp.edit();
						edit.putInt("which", which);
						edit.commit();
						scv_changebg.setDesc(items[which]);
						// 取消对话框
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", null);
				builder.show();
			}
		});
		
		// 黑名单拦截设置
		siv_callsms_safe = (SettingItemView) findViewById(R.id.siv_callsms_safe);
		callSmsSafeIntent = new Intent(this, CallSmsSafeService.class);
				
		siv_callsms_safe.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 判断是否有选中
				if(siv_callsms_safe.isChecked()) {
					// 关闭归属地显示了
					siv_callsms_safe.setChecked(false);
					stopService(callSmsSafeIntent);
				} else {
					// 开启归属地显示了
					siv_callsms_safe.setChecked(true);
					startService(callSmsSafeIntent);
				}
			}
		});
		
		// 程序锁设置
		siv_watchdog = (SettingItemView) findViewById(R.id.siv_watchdog);
		watchdogIntent = new Intent(this,WatchDogService.class);
		
		siv_watchdog.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String password = sp.getString("password", null);
				// 判断是否有选中
				if(siv_watchdog.isChecked()) {
					// 关闭程序锁了
					siv_watchdog.setChecked(false);
					stopService(watchdogIntent);
				} else {
					if(TextUtils.isEmpty(password)) {
						Toast.makeText(getApplicationContext(), "先进入手机防盗设置密码", 0).show();
						return ;
					}
					// 开启程序锁了
					siv_watchdog.setChecked(true);
					startService(watchdogIntent);
				}
				
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showAddressIntent = new Intent(this, AddressService.class);
		boolean serviceRunning = ServiceUtils.isServiceRunning(this, "com.ithaha.mobilesafe.service.AddressService");
		
		if(serviceRunning) {
			// 来电归属地服务正在运行
			siv_show_address.setChecked(true);
		} else {
			// 来电归属地服务没有运行了
			siv_show_address.setChecked(false);
		}
		
		boolean iscallSmsServiceRunning = ServiceUtils.isServiceRunning(this, "com.ithaha.mobilesafe.service.CallSmsSafeService");
		if(iscallSmsServiceRunning) {
			// 黑名单拦截服务正在运行
			siv_callsms_safe.setChecked(true);
		} else {
			// 黑名单拦截服务没有运行了
			siv_callsms_safe.setChecked(false);
		}
		
		boolean iswatchDogServiceRunning = ServiceUtils.isServiceRunning(this, "com.ithaha.mobilesafe.service.WatchDogService");
		if(iswatchDogServiceRunning) {
			// 开启程序锁了
			siv_watchdog.setChecked(true);
		} else {
			// 关闭程序锁了
			siv_watchdog.setChecked(false);
		}
	}
}
