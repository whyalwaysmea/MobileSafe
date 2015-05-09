package com.ithaha.mobilesafe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceUtils {

	/**
	 * 检验某个服务是否存活
	 * @param context
	 * @param nameService 服务名
	 * @return
	 */
	public static boolean isServiceRunning(Context context, String nameService) {
		// ActivityManager可以管理Activity和Service
		ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> infos = am.getRunningServices(100);
		for(RunningServiceInfo info : infos) {
			// 得到正在运行的服务的名字
			String name = info.service.getClassName();
			if(nameService.equals(name)) {
				return true;
			}
		}
		
		return false;
	}
}
