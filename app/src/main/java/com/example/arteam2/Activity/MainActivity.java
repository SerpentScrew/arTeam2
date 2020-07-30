package com.example.arteam2.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


import com.example.arteam2.Utility.FullScreenUtility;
import com.example.arteam2.R;

public class MainActivity extends AppCompatActivity {

	Toolbar myToolbar;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FullScreenUtility.makeNoTitle(this);
		setContentView(R.layout.activity_main);

		myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);

		//앱 이름 변경
		getSupportActionBar().setTitle("Measure App");  //해당 액티비티의 툴바에 있는 타이틀 이름


		//start
		ImageButton cameraButton = (ImageButton) findViewById(R.id.camera);
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(
						getApplicationContext(), // 현재 화면의 제어권자
						MeasureActivity.class); // 다음 넘어갈 클래스 지정
				startActivity(intent); // 다음 화면으로 넘어간다
			}
		});

		//settings
		ImageButton settingsButton = (ImageButton) findViewById(R.id.settings);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
				intent.putExtra("data", "Test Popup");
				startActivityForResult(intent, 1);
			}
		});



	}



}