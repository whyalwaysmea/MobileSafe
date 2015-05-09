package com.ithaha.mobilesafe;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.view.View;

/**
 * 基本的设置向导页
 * @author hello
 *
 */
public abstract class BaseSetupActivity extends Activity {

	/**
	 * 手势识别器
	 */
	private GestureDetector detector;
	
	protected SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		// 实例化手势识别器
		// Creates a GestureDetector with the supplied listener.
		detector = new GestureDetector(this, new SimpleGestureDetector());
	}
	
	class SimpleGestureDetector implements OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		/**
		 * 当手指在滑动的时候会调用
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			if ((e2.getRawX() - e1.getRawX()) > 200) {
				// 显示上一页
				showPre();
				return true;
			}

			if ((e1.getRawX() - e2.getRawX()) > 200) {
				// 显示下一页
				showNext();
				return true;
			}
			
			return false;
		}

	}
	
	/**
	 * 使用手势识别器
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	public abstract void showNext();
	public abstract void showPre();
	
	/**
	 * 上一步的点击事件
	 */
	public void next(View view) {
		showNext();
	}
	
	/**
	 * 上一步的点击事件
	 * @param view
	 */
	public void pre(View view) {
		showPre();
	}
}
