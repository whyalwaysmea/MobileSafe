package com.ithaha.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import com.ithaha.mobilesafe.db.dao.AppLockDao;
import com.ithaha.mobilesafe.domain.AppInfo;
import com.ithaha.mobilesafe.engine.AppInfoProvider;
import com.ithaha.mobilesafe.utils.DensityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 软件管理器
 * @author hello
 *
 */
public class AppManagerActivity extends Activity implements OnClickListener {

	private TextView tv_avail_rom;
	private TextView tv_avail_sd;
	private ListView lv_app_manager;
	private LinearLayout ll_loading;
	private TextView tv_status;
	/**
	 * 所有的应用程序包信息
	 */
	private List<AppInfo> appInfos;
	/**
	 * 用户应用程序的集合
	 */
	private List<AppInfo> userAppInfos;

	/**
	 * 系统应用程序的集合
	 */
	private List<AppInfo> systemAppInfos;

	/**
	 * 弹出的悬浮窗体
	 */
	private PopupWindow popupWindow;
	
	private LinearLayout ll_share;
	private LinearLayout ll_start;
	private LinearLayout ll_uninstall;
	
	/**
	 * 被点击的条目
	 */
	private AppInfo appInfo;
	private String TAG = "AppManagerActivity";
	
	private AppLockDao dao;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);

		tv_avail_rom = (TextView) findViewById(R.id.tv_avail_rom);
		tv_avail_sd = (TextView) findViewById(R.id.tv_avail_sd);
		lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		tv_status = (TextView) findViewById(R.id.tv_status);
		dao = new AppLockDao(this);
		
		long sdSize = getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
		long romSize = getAvailSpace(Environment.getDataDirectory().getAbsolutePath());

		// 显示可用内存大小  					Formats a content size to be in the form of bytes, kilobytes, megabytes, etc
		tv_avail_sd.setText("SD卡可用空间：" + Formatter.formatFileSize(this, sdSize));
		tv_avail_rom.setText("内部可用空间：" + Formatter.formatFileSize(this, romSize));

		ll_loading.setVisibility(View.VISIBLE);
		
		new Thread() {
			public void run() {
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				
				for (AppInfo appInfo : appInfos) {
					if (appInfo.isUserApp()) {
						userAppInfos.add(appInfo);
					} else {
						systemAppInfos.add(appInfo);
					}
				}

				// 加载listview的数据适配器
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						lv_app_manager.setAdapter(new AppManagerAdapter());
						ll_loading.setVisibility(View.INVISIBLE);
					}
				});
			};
		}.start();

		/**
		 * 滚动监听器
		 */
		lv_app_manager.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			/**
			 * 滚动的时候调用的方法 firstVisibleItem 第一个可见条目在ListView中的位置
			 * 
			 */
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				dismissPopuWindow();
				if (userAppInfos != null && systemAppInfos != null) {
					if (firstVisibleItem > userAppInfos.size()) {
						tv_status.setText("系统程序:" + systemAppInfos.size() + "个");
					} else {
						tv_status.setText("用户程序:" + userAppInfos.size() + "个");
					}
				}
			}
		});

		/**
		 * 设置listview的点击事件
		 */
		lv_app_manager.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					return;
				} else if (position == (userAppInfos.size() + 1)) {
					return;
				} else if (position <= userAppInfos.size()) {
					// 用户程序
					int newPosition = position - 1;
					appInfo = userAppInfos.get(newPosition);
				} else {
					// 系统程序
					int newPosition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newPosition);
				}

				dismissPopuWindow();
				View contentView = View.inflate(getApplicationContext(), R.layout.popup_app_item, null);
				
				ll_start = (LinearLayout) contentView.findViewById(R.id.ll_start);
				ll_share = (LinearLayout) contentView.findViewById(R.id.ll_share);
				ll_uninstall = (LinearLayout) contentView.findViewById(R.id.ll_uninstall);
				
				ll_start.setOnClickListener(AppManagerActivity.this);
				ll_share.setOnClickListener(AppManagerActivity.this);
				ll_uninstall.setOnClickListener(AppManagerActivity.this);
				
				
				popupWindow = new PopupWindow(contentView, -2,ViewGroup.LayoutParams.WRAP_CONTENT);
				// 动画效果的播放必须要求窗体有背景颜色
				popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				
				int[] location = new int[2];
				view.getLocationInWindow(location);
				// 在代码里设置的宽高都是像素，需要--> dip
				int dip = 60;
				int px = DensityUtils.dip2px(getApplicationContext(), dip);
				
				popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP, px, location[1]);
				
				// 播放动画
//				ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.5f);
//				sa.setDuration(500);
//				
//				AlphaAnimation aa = new AlphaAnimation(0.5f, 0.5f);
//				aa.setDuration(500);
//				
//				AnimationSet set = new AnimationSet(false);
//				set.addAnimation(sa);
//				set.addAnimation(aa);
//				contentView.startAnimation(set);
			}
		});
	
		/**
		 * 设置listview的长按事件
		 */
		lv_app_manager.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					return true;
				} else if (position == (userAppInfos.size() + 1)) {
					return true;
				} else if (position <= userAppInfos.size()) {
					// 用户程序
					int newPosition = position - 1;
					appInfo = userAppInfos.get(newPosition);
				} else {
					// 系统程序
					int newPosition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newPosition);
				}
				System.out.println("长点击了:" + appInfo.getPackname());
				// 判断条目是否存在在程序锁数据库里面
				ViewHolder holder = (ViewHolder) view.getTag();
				if(dao.find(appInfo.getPackname())){
					// 被锁定的程序，解锁，更新界面
					dao.delete(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.unlock);
				} else {
					// 锁定程序，上锁
					dao.add(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.lock);
				}
				
				return true;
			}
		});
	}

	private void dismissPopuWindow() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	private class AppManagerAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			// return appInfos.size();
			return userAppInfos.size() + 1 + systemAppInfos.size() + 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (position == 0) {
				// 显示用户程序有多少个小标签
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("用户程序:" + userAppInfos.size() + "个");
				return tv;
			} else if (position == userAppInfos.size() + 1) {
				// 显示系统程序有多少个小标签
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("系统程序:" + systemAppInfos.size() + "个");
				return tv;
			} else if (position <= userAppInfos.size()) {
				// 用户程序
				appInfo = userAppInfos.get(position - 1);
			} else {
				// 位置是给系统程序显示的
				appInfo = systemAppInfos.get(position - userAppInfos.size() - 2);
			}

			View view;
			ViewHolder holder;

			if (convertView != null && convertView instanceof RelativeLayout) {
				// 判断缓存是否为空和是否是合适的类型来复用
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(getApplicationContext(),R.layout.list_item_appinfo, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view
						.findViewById(R.id.iv_app_icon);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.tv_location = (TextView) view
						.findViewById(R.id.tv_app_location);
				holder.iv_status = (ImageView) view.findViewById(R.id.iv_status);

				view.setTag(holder);
			}
			holder.iv_icon.setImageDrawable(appInfo.getIcon());
			holder.tv_name.setText(appInfo.getName());
			if (appInfo.isInRom()) {
				holder.tv_location.setText("手机内存" + "     uid" + appInfo.getUid());
			} else {
				holder.tv_location.setText("外部存储" + "     uid" + appInfo.getUid());
			}
			if(dao.find(appInfo.getPackname())) {
				holder.iv_status.setImageResource(R.drawable.lock);
			} else {
				holder.iv_status.setImageResource(R.drawable.unlock);
			}
			
			return view;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	static class ViewHolder {
		TextView tv_name;
		TextView tv_location;
		ImageView iv_icon;
		ImageView iv_status;
	}

	/**
	 * 获取某个目录的可用空间
	 * 
	 * @param path
	 * @return
	 */
	private long getAvailSpace(String path) {

		StatFs statFs = new StatFs(path);
		statFs.getBlockCount(); // 获取分区的个数
		long size = statFs.getBlockSize(); // 获取分区的大小
		long count = statFs.getAvailableBlocks(); // 获取可用的分区个数

		return size * count;
	}

	@Override
	protected void onDestroy() {
		dismissPopuWindow();
		super.onDestroy();
	}

	/**
	 * 条目中的点击事件
	 */
	@Override
	public void onClick(View v) {
		dismissPopuWindow();
		switch (v.getId()) {
		case R.id.ll_share:		// 分享
			shareApplication();
			Log.i(TAG , "分享：" + appInfo.getName());
			break;
		case R.id.ll_start:		// 开始
			startApplication();
			Log.i(TAG , "开始: " + appInfo.getPackname());
			break;
			
		case R.id.ll_uninstall:	// 卸载
			if(appInfo.isUserApp()) {
				uninstallApplication();
				Log.i(TAG , appInfo.getName());
			} else {
				Toast.makeText(this, "无法卸载系统应用", 0).show();
//				Runtime.getRuntime().exec("");
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 分享应用程序
	 */
	private void shareApplication() {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, "推荐你使用一款软件，名称叫:" + appInfo.getName());
		startActivity(intent);
	}

	/**
	 * 卸载应用
	 */
	private void uninstallApplication() {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:"+appInfo.getPackname()));
		startActivityForResult(intent, 0);
	}

	/**
	 * 开启应用程序
	 */
	private void startApplication() {
		// 查询应用程序的入口activity
		PackageManager pm = getPackageManager();
//		Intent intent = new Intent();
//		intent.setAction("android.intent.action.MAIN");
//		intent.addCategory("android.intent.category.LAUNCHER");
//		// 查询出手机上所有可以启动的activity
//		List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
		
		Intent intent = pm.getLaunchIntentForPackage(appInfo.getPackname());
		if(intent != null) {
			startActivity(intent);
		} else {
			Toast.makeText(this, "对不起，无法启动该应用", 0).show();
		}
		
	}
	
	/**
	 * 卸载应用之后
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 刷新界面
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for (AppInfo appInfo : appInfos) {
					if (appInfo.isUserApp()) {
						userAppInfos.add(appInfo);
					} else {
						systemAppInfos.add(appInfo);
					}
				}

				// 加载listview的数据适配器
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						lv_app_manager.setAdapter(new AppManagerAdapter());
						ll_loading.setVisibility(View.INVISIBLE);
					}
				});
			};
		}.start();
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
}
