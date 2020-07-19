package com.example.arteam2.Utility;

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
}
