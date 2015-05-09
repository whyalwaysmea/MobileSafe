package com.ithaha.mobilesafe.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.ithaha.mobilesafe.domain.AppInfo;

/**
 * 用来提供手机里面安装的所有应用程序信息
 * @author hello
 *
 */
public class AppInfoProvider {

	/**
	 * 获取所有的安装的应用程序的信息
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context) {
		
		PackageManager pm = context.getPackageManager();
		// 所有的安装在系统上的应用程序的信息
		List<PackageInfo> packInfos = pm.getInstalledPackages(0);
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		
		for(PackageInfo packInfo : packInfos) {
			AppInfo appInfo = new AppInfo();
			// packageInfo 相当于一个应用程序apk包的清单文件
			String packageName = packInfo.packageName;
			Drawable icon = packInfo.applicationInfo.loadIcon(pm);
			String name = packInfo.applicationInfo.loadLabel(pm).toString();
			// 应用程序信息的标记
			int flags = packInfo.applicationInfo.flags;
			if((flags&ApplicationInfo.FLAG_SYSTEM) == 0) {
				// 用户程序
				appInfo.setUserApp(true);
			} else {
				// 系统程序
				appInfo.setUserApp(false);
			}
			if((flags&ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
				// 手机的内存里
				appInfo.setInRom(true);
			} else {
				// SD卡里
				appInfo.setInRom(false);
			}
			
			int uid = packInfo.applicationInfo.uid;	//操作系统分配给应用程序的一个固定的id，
			
//			File rcvFile = new File("/proc/uid_stat/" + uid + "/tcp_rcv");
//			File sndFile = new File("/proc/uid_stat/" + uid + "/tcp_snd");
			
			appInfo.setUid(uid);
			appInfo.setIcon(icon);
			appInfo.setName(name);
			appInfo.setPackname(packageName);
			
			appInfos.add(appInfo);
		}
		
		return appInfos;
	}
}
