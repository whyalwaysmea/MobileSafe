package com.ithaha.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Setup5Activity extends BaseSetupActivity {

	private SharedPreferences sp;
	private CheckBox cb_setup4_status;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup5);

		sp = getSharedPreferences("config", MODE_PRIVATE);
		cb_setup4_status = (CheckBox) findViewById(R.id.cb_setup5_status);
		
		boolean protect = sp.getBoolean("protecting", false);
		
		if(protect) {
			// 手机防盗已经开启
			cb_setup4_status.setChecked(true);
			cb_setup4_status.setText("手机防盗已经开启");
		} else {
			// 手机防盗还未开启
			cb_setup4_status.setChecked(false);
			cb_setup4_status.setText("手机防盗还未开启");
			
		}
		
		cb_setup4_status.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					// 如果勾选了
					cb_setup4_status.setText("手机防盗已经开启");
				} else {
					// 如果没有勾选
					cb_setup4_status.setText("手机防盗还未开启");
				}
				
				// 保存选择的状态
				Editor edit = sp.edit();
				edit.putBoolean("protecting", isChecked);
				edit.commit();
			}
		});
	}

	@Override
	public void showNext() {
		Editor edit = sp.edit();
		edit.putBoolean("configed", true);
		edit.commit();

		Intent intent = new Intent(Setup5Activity.this, LostFindActivity.class);
		startActivity(intent);
		finish();

	}

	@Override
	public void showPre() {
		Intent intent = new Intent(Setup5Activity.this, Setup4Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);

	}
	
	
}
