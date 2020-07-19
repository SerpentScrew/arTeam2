package com.example.arteam2.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

import com.example.arteam2.R;
import com.example.arteam2.Utility.FullScreenUtility;

public class MainActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FullScreenUtility.makeNoStatusBar(this);
		setContentView(R.layout.activity_main);
	}
}