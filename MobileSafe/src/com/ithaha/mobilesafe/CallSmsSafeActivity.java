package com.ithaha.mobilesafe;

import java.util.List;

import com.ithaha.mobilesafe.db.dao.BlackNumberDao;
import com.ithaha.mobilesafe.domain.BlackNumberInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 通讯卫士,黑名单拦截
 * @author hello
 *
 */
public class CallSmsSafeActivity extends Activity {
	public static final String TAG = "CallSmsSafeActivity";
	private ListView lv_callsms_safe;
	private List<BlackNumberInfo> infos;
	private BlackNumberDao dao;
	private CallSmsSafeAdapter adapter;
	private LinearLayout ll_loading;
	private int offset = 0;
	private int maxnumber = 20;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_sms_safe);
		
		lv_callsms_safe = (ListView) findViewById(R.id.lv_callsms_safe);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		
		dao = new BlackNumberDao(this);
		
		fillData();
		
		// listview注册一个滚动事件的监听器
		lv_callsms_safe.setOnScrollListener(new OnScrollListener() {
			
			/**
			 * 当滚动的状态发送变化
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:			// 空闲状态
					// 判断当前listview的滚动位置
					// 获取最后一个可见条目在集合里面的位置
					int lastposition = lv_callsms_safe.getLastVisiblePosition();
					if(lastposition == infos.size()-1) {
						// 列表被移动到最后一个位置
						offset+=maxnumber;
						fillData();
					}
					break;

				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:	// 手指触摸滚动
					
					break;
					
				case OnScrollListener.SCROLL_STATE_FLING:			// 惯性滑行状态
					
					break;
				default:
					break;
				}
			}
			
			/**
			 * 滚动的时候调用
			 */
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
			}
		});
		

		lv_callsms_safe.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i(TAG, "正在进行长按...");
				// 得到item的黑名单号码
				TextView tv_black_number = (TextView) view.findViewById(R.id.tv_black_number);
				
				String blackNumber = tv_black_number.getText().toString().trim();
				
				// 根据黑名单号码来查找黑名单模式
				String mode = dao.findMode(blackNumber);
				alterBlackNumber(view, blackNumber, mode);
				
				String newMode = dao.findMode(blackNumber);
				if(mode != newMode) {
					infos.remove(position);
				}
				
				return true;
			}
			
		});
		
	}

	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				if(infos == null) {
					infos = dao.findPart(offset, maxnumber);
				} else {
					// 原来已经加载过数据了
					infos.addAll(dao.findPart(offset, maxnumber));
				}
				// 在子线程中更新界面
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						ll_loading.setVisibility(View.INVISIBLE);
						if(adapter == null) {
							adapter = new CallSmsSafeAdapter();
							lv_callsms_safe.setAdapter(adapter);			
						} else {
							adapter.notifyDataSetChanged();
						}
					}
				});
			};
		}.start();
	}
	
	private class CallSmsSafeAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return infos.size();
		}
		//有多少个条目被显示，这个方法就会被调用多少次
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			//1.减少内存中view对象创建的个数
			if(convertView==null){
				Log.i(TAG,"创建新的view对象："+position);
				//把一个布局文件转化成  view对象。
				view  = View.inflate(getApplicationContext(), R.layout.list_item_callsms, null);
				//2.减少子孩子查询的次数  内存中对象的地址。
				holder = new ViewHolder();
				holder.tv_number = (TextView) view.findViewById(R.id.tv_black_number);
				holder.tv_mode = (TextView) view.findViewById(R.id.tv_black_mode);
				holder.iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
				//当孩子生出来的时候找到他们的引用，存放在记事本，放在父亲的口袋
				view.setTag(holder);
			}else{
				Log.i(TAG,"厨房有历史的view对象，复用历史缓存的view对象："+position);
				view = convertView;
				holder = (ViewHolder) view.getTag();//5%
			}
			holder.tv_number.setText(infos.get(position).getNumber());
			String mode = infos.get(position).getMode();
			if("1".equals(mode)){
				holder.tv_mode.setText("电话拦截");
			}else if("2".equals(mode)){
				holder.tv_mode.setText("短信拦截");
			}else{
				holder.tv_mode.setText("全部拦截");
			}
			holder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new Builder(CallSmsSafeActivity.this);
					builder.setTitle("警告");
					builder.setMessage("确定要删除这条记录么？");
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//删除数据库的内容
							dao.delete(infos.get(position).getNumber());
							//更新界面。
							infos.remove(position);
							//通知listview数据适配器更新
							adapter.notifyDataSetChanged();
						}
					});
					builder.setNegativeButton("取消", null);
					builder.show();
				}
			});
			return view;
		}
		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}
	/**
	 * view对象的容器
	 *记录孩子的内存地址。
	 *相当于一个记事本
	 */
	static class ViewHolder{
		TextView tv_number;
		TextView tv_mode;
		ImageView iv_delete;
	}
	
	
	private EditText et_blacknumber;
	private CheckBox cb_phone;
	private CheckBox cb_sms;
	private Button bt_ok;
	private Button bt_cancel;
	
	/**
	 * 增加黑名单成员
	 * @param view
	 */
	public void addBlackNumber(View view){
		AlertDialog.Builder builder = new Builder(this);
		final AlertDialog dialog = builder.create();
		View contentView = View.inflate(this, R.layout.dialog_add_blacknum, null);
		et_blacknumber = (EditText) contentView.findViewById(R.id.et_blacknum);
		cb_phone = (CheckBox) contentView.findViewById(R.id.cb_phone);
		cb_sms = (CheckBox) contentView.findViewById(R.id.cb_sms);
		bt_cancel = (Button) contentView.findViewById(R.id.cancel);
		bt_ok = (Button) contentView.findViewById(R.id.ok);
		dialog.setView(contentView, 0, 0, 0, 0);
		dialog.show();
		
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String blacknumber = et_blacknumber.getText().toString().trim();
				if(dao.find(blacknumber)) {
					Toast.makeText(getApplicationContext(), "该号码已在黑名单中", 0).show();
					return;
				} 
				
				
				if(TextUtils.isEmpty(blacknumber)){
					Toast.makeText(getApplicationContext(), "黑名单号码不能为空", 0).show();
					return;
				}
				String mode ;
				if(cb_phone.isChecked()&&cb_sms.isChecked()){
					//全部拦截
					mode = "3";
				}else if(cb_phone.isChecked()){
					//电话拦截
					mode = "1";
				}else if(cb_sms.isChecked()){
					//短信拦截
					mode = "2";
				}else{
					Toast.makeText(getApplicationContext(), "请选择拦截模式", 0).show();
					return;
				}
				//数据被加到数据库
				dao.add(blacknumber, mode);
				//更新listview集合里面的内容。
				BlackNumberInfo info = new BlackNumberInfo();
				info.setMode(mode);
				info.setNumber(blacknumber);
				infos.add(0, info);
				//通知listview数据适配器数据更新了。
				adapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * 修改黑名单拦截内容
	 * @param view
	 */
	public void alterBlackNumber(final View view, final String blackNumber, String mode) {
		AlertDialog.Builder builder = new Builder(this);
		final AlertDialog dialog = builder.create();
		View contentView = View.inflate(this, R.layout.dialog_add_blacknum, null);
		et_blacknumber = (EditText) contentView.findViewById(R.id.et_blacknum);
		et_blacknumber.setText(blackNumber);
		// Set the enabled state of this view. The interpretation of the enabled state varies by subclass.
		et_blacknumber.setEnabled(false);
		cb_phone = (CheckBox) contentView.findViewById(R.id.cb_phone);
		cb_sms = (CheckBox) contentView.findViewById(R.id.cb_sms);
		
		if("1".equals(mode)) {
			// 电话拦截
			cb_phone.setChecked(true);
			cb_sms.setChecked(false);
		} else if("2".equals(mode)){
			// 短信拦截
			cb_phone.setChecked(false);
			cb_sms.setChecked(true);
		} else if("3".equals(mode)) {
			// 全部拦截
			cb_phone.setChecked(true);
			cb_sms.setChecked(true);
		}
		bt_cancel = (Button) contentView.findViewById(R.id.cancel);
		bt_ok = (Button) contentView.findViewById(R.id.ok);
		dialog.setView(contentView, 0, 0, 0, 0);
		dialog.show();
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String newMode ;
				if(cb_phone.isChecked()&&cb_sms.isChecked()){
					//全部拦截
					newMode = "3";
				}else if(cb_phone.isChecked()){
					//电话拦截
					newMode = "1";
				}else if(cb_sms.isChecked()){
					//短信拦截
					newMode = "2";
				}else{
					Toast.makeText(getApplicationContext(), "请选择拦截模式", 0).show();
					return;
				}
								
				// 更新数据库
				dao.update(blackNumber, newMode);
								
				//通知listview数据适配器数据更新了。
				adapter.notifyDataSetChanged();
				dialog.dismiss();
				fillData();
			}
		});
	}
}
