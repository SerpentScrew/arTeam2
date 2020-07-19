package com.example.arteam2.Utility;

import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class FullScreenUtility {
	
	/**
	 * activity 를 setContentView 하기 전에 사용하면 됨
	 * @param activity
	 */
	public void makeScreenFullSize(AppCompatActivity activity) {
		activity.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
		activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
		                             );
	}
}
