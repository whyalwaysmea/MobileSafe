package com.ithaha.mobilesafe;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class Setup4Activity extends BaseSetupActivity {

	/**
	 * 设备策略服务
	 */
	private DevicePolicyManager dpm;

	private SharedPreferences sp;
	private Button cb_setup4_status;
	private boolean lockstatus;

	private Editor edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup4);

		sp = getSharedPreferences("config", MODE_PRIVATE);
		edit = sp.edit();
		
		cb_setup4_status = (Button) findViewById(R.id.cb_setup4_status);
		
		lockstatus = sp.getBoolean("lockstatus", false);
		if(lockstatus) {
			// 已经激活
			cb_setup4_status.setText("远程锁屏已经开启");
		} else {
			// 还未激活
			cb_setup4_status.setText("远程锁屏还未激活");
		}
	}

	public void lock(View view) {
		lockstatus = sp.getBoolean("lockstatus", false);
		if(!lockstatus) {
			// 已经激活
			
			// 创建一个Intent
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			// 我要激活谁
			ComponentName mDeviceAdminSample = new ComponentName(this,MyAdmin.class);

			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					mDeviceAdminSample);
			// 劝说用户开启管理员权限
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					"哥们开启我可以一键锁屏，你的按钮就不会经常失灵");
			startActivity(intent);

			cb_setup4_status.setText("远程锁屏已经开启");
			edit.putBoolean("lockstatus", true);
			edit.commit();
		} else {
			// 还未激活
			edit.putBoolean("lockstatus", false);
			edit.commit();
			
			dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
			ComponentName mDeviceAdminSample = new ComponentName(Setup4Activity.this,MyAdmin.class);
			dpm.removeActiveAdmin(mDeviceAdminSample);
			
			cb_setup4_status.setText("远程锁屏还未激活");
			Toast.makeText(getApplicationContext(), "已经取消一键锁屏", 0).show();		
		}
		
	}

	@Override
	public void showNext() {

		Intent intent = new Intent(Setup4Activity.this, Setup5Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
	}

	@Override
	public void showPre() {
		Intent intent = new Intent(Setup4Activity.this, Setup3Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);

	}

}
