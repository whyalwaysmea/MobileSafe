package com.ithaha.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import com.ithaha.mobilesafe.domain.TaskInfo;
import com.ithaha.mobilesafe.engine.TaskInfoProvider;
import com.ithaha.mobilesafe.utils.SystemInfoUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Formatter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TaskManagerActivity extends Activity {
	
	private TextView tv_process_count;
	private TextView tv_men_info;
	private LinearLayout ll_loading;
	private ListView lv_task_manager;
	private List<TaskInfo> allTaskInfos;
	private List<TaskInfo> userTaskInfos;
	private List<TaskInfo> systemTaskInfos;
	private TextView tv_status;
	
	private TaskManagerAdapter adapter;
	
	private String TAG = "TaskManagerActivity";
	private int processCount;
	private long availMem;
	private long totalMem;
	
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_manager);
		
		tv_process_count = (TextView) findViewById(R.id.tv_process_count);
		tv_men_info = (TextView) findViewById(R.id.tv_men_info);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		lv_task_manager = (ListView) findViewById(R.id.lv_task_manager);
		tv_status = (TextView) findViewById(R.id.tv_status);
		
		setTitle();
	
		fillData();
		lv_task_manager.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(userTaskInfos != null && systemTaskInfos != null) {
					if(firstVisibleItem > userTaskInfos.size()) {
						tv_status.setText("系统进程:" + systemTaskInfos.size() + "个");
					} else {
						tv_status.setText("用户进程:" + userTaskInfos.size() + "个");
					}
				}
			}
		});
		
		lv_task_manager.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TaskInfo taskInfo;
				if(position == 0) {
					return ;
				} else if(position == (1+userTaskInfos.size())) {
					// 系统进程的标签
					return ;
				} else if(position <= userTaskInfos.size()) {
					taskInfo = userTaskInfos.get(position - 1); 
				} else {
					taskInfo = systemTaskInfos.get(position - 1 - userTaskInfos.size() - 1);
				}
				if(getPackageName().equals(taskInfo.getPackname())){
					return;
				}
				ViewHolder holder = (ViewHolder) view.getTag();
				if(taskInfo.isChecked()) {
					taskInfo.setChecked(false);
					holder.cb_status.setChecked(false);
				} else {
					taskInfo.setChecked(true);
					holder.cb_status.setChecked(true);
				}
			}
		});
		
	}

	private void setTitle() {
		processCount = SystemInfoUtils.getRunningProcessCount(this);
		tv_process_count.setText("运行中的进程:" + processCount +"个");
		availMem = SystemInfoUtils.getAvailMem(this);
		totalMem = SystemInfoUtils.getTotalMem(this);
		tv_men_info.setText("剩余/总内存" + Formatter.formatFileSize(this, availMem) + "/" + Formatter.formatFileSize(this, totalMem));
	}

	/**
	 * 填充数据
	 */
	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(){

			public void run() {
				allTaskInfos = TaskInfoProvider.getTaskInfos(getApplicationContext());
				userTaskInfos = new ArrayList<TaskInfo>();
				systemTaskInfos = new ArrayList<TaskInfo>();
				for(TaskInfo info : allTaskInfos) {
					if(info.isUserTask()) {
						userTaskInfos.add(info);
					} else {
						systemTaskInfos.add(info);
					}
				}
				
				// 更新设置界面
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						ll_loading.setVisibility(View.INVISIBLE);
						if(adapter == null) {
							adapter = new TaskManagerAdapter();
							lv_task_manager.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged();
						}
						setTitle();
					}
				});
			};
		}.start();
	}
	
	private class TaskManagerAdapter extends BaseAdapter {

		@Override
		public int getCount() {

			sp = getSharedPreferences("config", MODE_PRIVATE);
			if(sp.getBoolean("showsystem", false)) {
				return userTaskInfos.size() + 1 + systemTaskInfos.size() + 1;
			} else {
				return userTaskInfos.size() + 1;
			}
			
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TaskInfo taskInfo;
			if(position == 0) {
				// 用户进程的标签
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("用户进程:" + userTaskInfos.size() + "个");
				return tv;
			} else if(position == (1+userTaskInfos.size())) {
				// 系统进程的标签
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("系统进程:" + systemTaskInfos.size() + "个");
				return tv;
			} else if(position <= userTaskInfos.size()) {
				taskInfo = userTaskInfos.get(position - 1); 
			} else {
				taskInfo = systemTaskInfos.get(position - 1 - userTaskInfos.size() - 1);
			}
			
			View view;
			ViewHolder holder;
			if(convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(getApplicationContext(), R.layout.list_item_taskinfo, null);
				holder = new ViewHolder();
				holder.iv_app_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
				holder.tv_app_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.tv_app_memsize = (TextView) view.findViewById(R.id.tv_app_memsize);
				holder.cb_status = (CheckBox) view.findViewById(R.id.cb_status);
				view.setTag(holder);
			}
			
			holder.iv_app_icon.setImageDrawable(taskInfo.getIcon());
			holder.tv_app_name.setText(taskInfo.getName());
			Log.i(TAG, "内存占用：" +taskInfo.getMemsize());
			Log.i(TAG, "是否勾选了：" +taskInfo.isChecked());
			holder.tv_app_memsize.setText("内存占用:" + Formatter.formatFileSize(getApplicationContext(), taskInfo.getMemsize()));
			holder.cb_status.setChecked(taskInfo.isChecked());
			
			if(getPackageName().equals(taskInfo.getPackname())) {
				holder.cb_status.setVisibility(View.INVISIBLE);
			} else {
				holder.cb_status.setVisibility(View.VISIBLE);
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
		ImageView iv_app_icon;
		TextView tv_app_name;
		TextView tv_app_memsize;
		CheckBox cb_status;
	}
	
	/**
	 * 选中全部
	 * @param view
	 */
	public void selectAll(View view) {
		for(TaskInfo info : userTaskInfos) {
			if(getPackageName().equals(info.getPackname())) {
				continue;
			}
			info.setChecked(true);
		}
		for(TaskInfo info : systemTaskInfos) {
			info.setChecked(true);
		}
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * 反选
	 * @param view
	 */
	public void selectOppp(View view) {
		for(TaskInfo info : userTaskInfos) {
			if(getPackageName().equals(info.getPackname())) {
				continue;
			}
			info.setChecked(!info.isChecked());
		}
		for(TaskInfo info : systemTaskInfos) {
			info.setChecked(!info.isChecked());
		}
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * 清理
	 * @param view
	 */
	public void killAll(View view) {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		int count = 0;
		long savedMen = 0;
		List<TaskInfo> killedTaskInfos = new ArrayList<TaskInfo>();
		for(TaskInfo info : allTaskInfos) {
			if(info.isChecked()) {
				am.killBackgroundProcesses(info.getPackname());
				if(info.isUserTask()) {
					userTaskInfos.remove(info);
				} else {
					systemTaskInfos.remove(info);
				}
				killedTaskInfos.add(info);
				count ++;
				savedMen += info.getMemsize();
			}
		}
		
		allTaskInfos.removeAll(killedTaskInfos);
		
		adapter.notifyDataSetChanged();
		Toast.makeText(getApplicationContext(), "消除"+ count + "个进程，释放" + Formatter.formatFileSize(this, savedMen) + "M内存", 0).show();
		
		processCount -=count;
		availMem +=savedMen;
		tv_process_count.setText("运行中的进程:" + processCount +"个");
		tv_men_info.setText("剩余/总内存" + Formatter.formatFileSize(this, availMem) + "/" + Formatter.formatFileSize(this, totalMem));
	}
	
	/**
	 * 设置
	 * @param view
	 */
	public void enterSetting(View view) {
		Intent intent = new Intent(this, TaskSettingActivity.class);
		startActivityForResult(intent,0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		adapter.notifyDataSetChanged();
		super.onActivityResult(requestCode, resultCode, data);
	}
}
