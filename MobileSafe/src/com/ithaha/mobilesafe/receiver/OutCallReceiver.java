package com.ithaha.mobilesafe.receiver;

import com.ithaha.mobilesafe.db.dao.NumAddressQueryUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class OutCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// 拨出去的电话号码
		String phone = getResultData();
		String address = NumAddressQueryUtils.queryNumber(phone);
		Toast.makeText(context, address, 1).show();
	}

}
