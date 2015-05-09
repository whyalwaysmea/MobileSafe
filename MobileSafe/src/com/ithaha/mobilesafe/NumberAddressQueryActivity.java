package com.ithaha.mobilesafe;

import com.ithaha.mobilesafe.db.dao.NumAddressQueryUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NumberAddressQueryActivity extends Activity {

	private EditText et_phone;
	private TextView result;
	
	/**
	 * 振动服务
	 */
	private Vibrator vibrator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_address_query);
		
		et_phone = (EditText) findViewById(R.id.et_phone);
		result = (TextView) findViewById(R.id.result);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		et_phone.addTextChangedListener(new TextWatcher() {
			
			/**
			 * 当文本发生变化的时候回调
			 */
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s != null && s.length() >= 3) {
					// 查询数据库，并且显示结果
					String address = NumAddressQueryUtils.queryNumber(s.toString());
					result.setText(address);
				}
			}
			
			/**
			 * 当文本发生变化之前回调
			 */
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			/**
			 * 当文本发生变化之后回调
			 */
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	/**
	 * 查询号码归属地
	 * @param view
	 */
	public void numberAddressQuery(View view) {
		String phone = et_phone.getText().toString().trim();
		if(TextUtils.isEmpty(phone)) {
			Toast.makeText(this, "号码不能为空", 0).show();
			// 输入框跳动
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			et_phone.startAnimation(shake);
			
			//手机震动
//			vibrator.vibrate(500);
			long[] pattern = {200,200,300,300,1000,2000};
			// -1 不重复，0表示重复
			vibrator.vibrate(pattern, -1);
			return ;
		} else {
			// 去数据库查询号码归属地
			String addres = NumAddressQueryUtils.queryNumber(phone);
			result.setText(addres);
		}
		
	}
}
