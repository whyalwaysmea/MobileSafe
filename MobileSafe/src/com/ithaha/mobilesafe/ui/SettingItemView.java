package com.ithaha.mobilesafe.ui;

import com.ithaha.mobilesafe.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 我们自定义的组合控件,它里面有两个TextView，一个CheckBox，一个View
 * 此处需要将三个构造方法都实现
 * @author hello
 *
 */
public class SettingItemView extends RelativeLayout {

	private CheckBox cb_states;
	private TextView tv_description;
	private TextView tv_title;
	
	private String desc_on;
	private String desc_off;
	
	/**
	 * 初始化布局文件
	 * @param context
	 */
	private void initView(Context context) {
		// 把一个布局文件--->View，并且加载在SettingItemView
		View.inflate(context, R.layout.setting_item_view, this);
		cb_states = (CheckBox) this.findViewById(R.id.cb_states);
		tv_description = (TextView) this.findViewById(R.id.tv_description);
		tv_title = (TextView) this.findViewById(R.id.tv_title);
	}
	
	public SettingItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	/**
	 * 带有两个参数的构造方法，布局文件使用的时候调用
	 * @param context
	 * @param attrs
	 */
	public SettingItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		String title = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.ithaha.mobilesafe", "title1");
		desc_on = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.ithaha.mobilesafe", "desc_on");
		desc_off = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.ithaha.mobilesafe", "desc_off");
		
		tv_title.setText(title);
		setDesc(desc_off);
	}

	public SettingItemView(Context context) {
		super(context);
		initView(context);
	}
	
	/**
	 * 校验组合控件是否选中
	 */
	public boolean isChecked() {
		return cb_states.isChecked();
	}
	
	/**
	 * 设置组合控件的状态
	 */
	public void setChecked(boolean checked) {
		if(checked) {
			setDesc(desc_on);
		} else {
			setDesc(desc_off);
		}
		cb_states.setChecked(checked);
	}
	
	/**
	 * 设置组合控件的描述信息
	 */
	public void setDesc(String text) {
		tv_description.setText(text);
	}
}
