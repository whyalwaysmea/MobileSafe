package com.ithaha.mobilesafe;

import com.ithaha.mobilesafe.utils.LockScreenActivity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 手机防盗
 * @author hello
 *
 */
public class LostFindActivity extends Activity {
	
	private SharedPreferences sp;
	private TextView tv_safenum;
	private ImageView iv_protecting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 判断是否做过设置向导
		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean configed = sp.getBoolean("configed", false);
		
		if(configed) {
			// 做过设置向导
			setContentView(R.layout.activity_lost_find);
			tv_safenum = (TextView) findViewById(R.id.tv_safenum);
			iv_protecting = (ImageView) findViewById(R.id.iv_protecting);
			
			// 得到安全号码
			String safePhone = sp.getString("safephone", null);
			tv_safenum.setText(safePhone);
			
			// 得到是否开始防盗保护
			boolean protecting = sp.getBoolean("protecting", false);
			if(protecting) {
				// 如果开启了	
				iv_protecting.setImageResource(R.drawable.lock);
			} else {
				// 如果没有开启
				iv_protecting.setImageResource(R.drawable.unlock);
			}
			
			
		} else {
			// 没有做过设置向导
			Intent intent = new Intent(this,Setup1Activity.class);
			startActivity(intent);
			
			// 关闭当前页面
			finish();
		}
		
	}
	
	/**
	 * 重新进入防盗设置向导
	 * @param view
	 */
	public void reEnterSetup(View view) {
		Intent intent = new Intent(LostFindActivity.this,Setup1Activity.class);
		startActivity(intent);
		
	}
		
}
