package com.example.arteam2.Utility;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtility {
	/**
	 * session 을 처음 만들 때 사용하면 됨
	 * @param activity
	 */
	public static void requestCameraPermission(AppCompatActivity activity) {
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
		    != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(
					activity,
					new String[]{Manifest.permission.CAMERA},
					0);
		}
	}
}
