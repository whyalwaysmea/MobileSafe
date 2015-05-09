package com.ithaha.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

/*
 * 自定义一个TextView
 */
public class FocusedTextView extends TextView {

	public FocusedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FocusedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FocusedTextView(Context context) {
		super(context);
	}

	/**
	 * 当前并没有焦点,只是系统认为有了
	 */
	@Override
	@ExportedProperty(category = "focus")
	public boolean isFocused() {
		return true;

	}
}
