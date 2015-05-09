package com.ithaha.mobilesafe.service;

import com.ithaha.mobilesafe.NumberAddressQueryActivity;
import com.ithaha.mobilesafe.R;
import com.ithaha.mobilesafe.db.dao.NumAddressQueryUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class AddressService extends Service {

	/**
	 * 监听来电
	 */
	private TelephonyManager tm;
	private MyPhoneStateListener listener;
	private OutCallReceiver receiver;
	/**
	 * 窗体管理者
	 */
	private WindowManager wm;
	private View view;
	private WindowManager.LayoutParams params;
	private SharedPreferences sp;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class MyPhoneStateListener extends PhoneStateListener {
		/**
		 * state --> 状态
		 * incomingNumber --> 电话号码
		 */
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:	// 铃声响起的时候,其实也就来电的时候
				
				// 根据得到的来电号码 查询归属地
				String address = NumAddressQueryUtils.queryNumber(incomingNumber);
//				Toast.makeText(getApplicationContext(), "来电归属地:" + address, 1).show();
				myToast(address);
				
				break;
			case TelephonyManager.CALL_STATE_IDLE:		// 电话的空闲状态,
				// 消掉自定义吐司的view
				if(view != null) {
					wm.removeView(view);
				}
				break;

			default:
				break;
			}
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		// 监听来电
		listener = new MyPhoneStateListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		
		// 用代码注册广播接收者
		receiver = new OutCallReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		registerReceiver(receiver, filter);
		
		// 实例化窗体管理者
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// 取消监听来电
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
		
		// 用代码取消广播接收者
		unregisterReceiver(receiver);
		receiver = null;
	}
	
	/**
	 * 服务里面的内部类
	 * @author hello
	 *
	 */
	class OutCallReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 拨出去的电话号码
			String phone = getResultData();
			String address = NumAddressQueryUtils.queryNumber(phone);
//			Toast.makeText(context, address, 1).show();
			myToast(address);
		}

	}
	
	/**
	 * 自定义吐司
	 * @param address
	 */
	private void myToast(String address) {
		view = View.inflate(this, R.layout.address_show, null);
		TextView textViwe = (TextView) view.findViewById(R.id.tv_address);
		
		// 双击居中
		final long[] mHits = new long[2];
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();
				if(mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
					// 双击居中...
					params.x = wm.getDefaultDisplay().getWidth() / 2 - view.getWidth() / 2;
					wm.updateViewLayout(view, params);
					
					Editor edit = sp.edit();
					edit.putInt("lastx", params.x);
					edit.putInt("lasty", params.y);
					edit.commit();
				}
			}
		});
		// 给view对象设置一个触摸的监听器
		view.setOnTouchListener(new OnTouchListener() {
			// 定义手指的初始化位置
			int startX;
			int startY;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:		// 手指按下屏幕
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					
					break;

				case MotionEvent.ACTION_MOVE:		// 手指在屏幕上移动
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					
					// 偏移量
					int dx = newX - startX;
					int dy = newY - startY;

					// 更新ImageView在窗体的位置
					params.x += dx;
					params.y += dy;
					
					// 考虑边界问题
					if(params.x<0) {
						params.x = 0;
					}
					if(params.y<0) {
						params.y = 0;
					}
					if(params.x > (wm.getDefaultDisplay().getWidth() - view.getWidth())) {
						params.x = wm.getDefaultDisplay().getWidth() - view.getWidth();
					}
					if(params.y > (wm.getDefaultDisplay().getHeight() - view.getHeight())) {
						params.y = wm.getDefaultDisplay().getHeight() - view.getHeight();
					}
					
					wm.updateViewLayout(v, params);
					
					// 重新初始化手指的开始，结束位置
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					
					break;
					
				case MotionEvent.ACTION_UP:			// 手指离开屏幕
					// 记录控件距离屏幕左上角的坐标
					Editor edit = sp.edit();
					edit.putInt("lastx", params.x);
					edit.putInt("lasty", params.y);
					edit.commit();
					
					break;
				default:
					break;
				}
				return true;						// 事件处理完毕，不要让父控件，父布局响应触摸事件了
			}
		});
		
		
		textViwe.setText(address);
		// "半透明","活力橙","卫士蓝","金属灰","苹果绿"
		int[] ids = {R.drawable.call_locate_white, R.drawable.call_locate_orange
				,R.drawable.call_locate_blue,R.drawable.call_locate_gray,R.drawable.call_locate_green}; 
		sp = getSharedPreferences("config", MODE_PRIVATE);
		textViwe.setBackgroundResource(ids[sp.getInt("which", 0)]);
		
		params = new WindowManager.LayoutParams();
		
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        // 窗体与左上角对齐
        params.gravity = Gravity.TOP + Gravity.LEFT;
        // 窗体距离左边和上边100
        params.x = sp.getInt("lastx", 0);
        params.y = sp.getInt("lasty", 0);
        
        
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        // android系统里面具有电话优先级的一种窗体类型，需要添加权限
        params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        params.setTitle("Toast");
		
		
		wm.addView(view, params);
		
	}
}


