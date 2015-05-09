package com.ithaha.mobilesafe.service;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.ithaha.mobilesafe.db.dao.BlackNumberDao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.BoringLayout;

/**
 * 黑名单拦截服务
 * @author hello
 *
 */
public class CallSmsSafeService extends Service {

	private InnerSmsReceiver receiver;
	private BlackNumberDao dao;
	private TelephonyManager tm;
	private MyListener listener;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class InnerSmsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 检查发件人是否是黑名单号码，并且设置了短信拦截 or 全部拦截
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for(Object obj : objs) {
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[])obj);
				// 得到短信发件人
				String sender = smsMessage.getOriginatingAddress();
				String mode = dao.findMode(sender);
				if("2".equals(mode) || "3".equals(mode)){
					// 拦截短信
					abortBroadcast();
				}
				// 智能拦截演示
				String body = smsMessage.getMessageBody();
				if(body.contains("fapiao")) {
					abortBroadcast();
				}
			}
		}
		
	}
	
	@Override
	public void onCreate() {
		dao = new BlackNumberDao(this);
		receiver = new InnerSmsReceiver();
		
		// 动态注册一个广播接收器
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(receiver, filter);
		
		
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		
		unregisterReceiver(receiver);
		receiver = null;
		
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		super.onDestroy();
	}
	
	private class MyListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:	// 响铃状态
				String result = dao.findMode(incomingNumber);
				if("1".equals(result) || "3".equals(result)) {
					// 挂断电话
					endCall();
					
					// 删除呼叫记录
					// 联系人应用的私有数据库里面,只有使用内容提供者
					// deleteCallLog(incomingNumber);
					// 观察呼叫记录数据库内容的变化
					getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"), true, new CallLogObserver(new Handler(),incomingNumber));
				}
				break;

			default:
				break;
			}
			
			super.onCallStateChanged(state, incomingNumber);
		}
		
	}
	
	private class CallLogObserver extends ContentObserver {

		private String incomingNumber;
		
		public CallLogObserver(Handler handler,String incomingNumber) {
			super(handler);
			this.incomingNumber = incomingNumber;
		}

		@Override
		public void onChange(boolean selfChange) {
			// 数据库内容变化了，产生了呼叫记录
			getContentResolver().unregisterContentObserver(this);
			deleteCallLog(incomingNumber);
			super.onChange(selfChange);
		}
		
	}

	/**
	 * 挂断电话
	 */
	public void endCall() {
		try {
			// 加载servicemanager的字节码
			Class clazz = CallSmsSafeService.class.getClassLoader().loadClass("android.os.ServiceManager");
			
			Method method = clazz.getDeclaredMethod("getService", String.class);
			IBinder ibinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
			ITelephony.Stub.asInterface(ibinder).endCall();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 利用内容提供者删除呼叫记录
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver resolver = getContentResolver();
		// 呼叫记录uri的路径   CallLog.CONTENT_URI;
		Uri uri = Uri.parse("content://call_log/calls");
		
		resolver.delete(uri, "number = ?", new String[]{incomingNumber});
	}
}
