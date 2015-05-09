package com.ithaha.mobilesafe.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

public class BootCompleteReceiver extends BroadcastReceiver {

	private SharedPreferences sp;
	private TelephonyManager tm;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		boolean protecting = sp.getBoolean("protecting", false);
		if(protecting) {
			// 开启了防盗保护
			
			// 读取之前保存的sim卡信息
			String saveSim = sp.getString("sim", null);

			// 读取当前的sim卡信息
			String realSim = tm.getSimSerialNumber();
			
			// 比较是否一样
			if(saveSim.equals(realSim)) {
				// sim卡没有变
				
			} else {
				// sim卡改变了
				String safephone = sp.getString("safephone", null);
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(safephone, null, "sim has changed....", null, null);
			}
		}
		
		
		
	}

}
