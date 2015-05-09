package com.ithaha.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * 设置向导页一.
 * @author hello
 *
 */
public class Setup1Activity extends BaseSetupActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup1);
	}

	@Override
	public void showNext() {
		Intent intent = new Intent(Setup1Activity.this, Setup2Activity.class);
		startActivity(intent);
		finish();
		// 指定一个明确的过度动画，要求在finish()或者startActivity(intent);后面执行
		// Call immediately after one of the flavors of startActivity(Intent) or finish() to specify an explicit transition animation to perform next.
		overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
	}

	@Override
	public void showPre() {
		// TODO Auto-generated method stub
		
	}
	
}
