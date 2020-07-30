package com.example.arteam2.Utility;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class FullScreenUtility {
	
	/**
	 * 타이틀 제거하는 메뉴
	 * activity 를 setContentView 하기 전에 사용하면 됨
	 * @param activity
	 */
	public static void makeNoTitle(AppCompatActivity activity) {
		activity.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
	/**
	 * 상태바 (폰 위의 시커먼거) 제거하는 메뉴
	 * 마찬가지로 setContentView 하기 전에 사용하면 됨
	 * @param activity
	 */
	public static void makeNoStatusBar(AppCompatActivity activity) {
		activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
		                             );
	}

	/**
	 * navagate bar 삭제하고 싶으면 복붙
	 * hide는 마찬가지로 setContentView 하기 전에 사용하면 됨
	 * 함수는 아래 따로
	 */

	public static void hideNavigateBar(AppCompatActivity activity) {
		activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	/*public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}*/

}
