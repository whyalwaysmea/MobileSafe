package com.ithaha.mobilesafe.utils;

import com.ithaha.mobilesafe.MyAdmin;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.Toast;

public class LockScreenActivity extends Activity {

	/**
	 * 设备策略服务
	 */
	private DevicePolicyManager dpm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdminSample = new ComponentName(this,MyAdmin.class);
		if(dpm.isAdminActive(mDeviceAdminSample)) {
			dpm.lockNow(); // 锁屏
			dpm.resetPassword("123456", 0); // 设置屏幕密码
			
			// 清楚SD卡
			// dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
			// 恢复出厂设置
			// dpm.wipeData(0);
		} else {
			Toast.makeText(this, "还没有打开管理员权限", 1).show();
			return ;
		}
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
