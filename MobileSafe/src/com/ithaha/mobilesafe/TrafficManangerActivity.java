package com.ithaha.mobilesafe;

import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;

/**
 * 获取流量统计
 * @author hello
 *
 */
public class TrafficManangerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic_manager);
		
		// 1.获取一个包管理器
		PackageManager pm = getPackageManager();
		
		// 2.遍历手机操作系统，获取所有的应用程序的uid
		List<ApplicationInfo> appLicationInfos = pm.getInstalledApplications(0);
		for(ApplicationInfo info : appLicationInfos) {
			int uid = info.uid;
			long uidTxBytes = TrafficStats.getUidTxBytes(uid);		// 上传的流量
			long uidRxBytes = TrafficStats.getUidRxBytes(uid);		// 下载的流量
			// 方法返回值为-1代表的是应用程序没有产生流量，或者操作系统不支持流量统计
		}
	}
}
