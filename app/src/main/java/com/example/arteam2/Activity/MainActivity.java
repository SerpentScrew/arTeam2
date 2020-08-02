package com.example.arteam2.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.arteam2.Utility.FullScreenUtility;
import com.example.arteam2.R;

public class MainActivity extends AppCompatActivity {

	Toolbar myToolbar;
	public static String airline;
	public static float[] size;



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
				if(size != null && airline != null) {
					Intent intent = new Intent(
							getApplicationContext(), // 현재 화면의 제어권자
							MeasureActivity.class); // 다음 넘어갈 클래스 지정
					startActivity(intent); // 다음 화면으로 넘어간다
				}
				else {
					Toast.makeText(getApplicationContext(), "항공사를 선택해주세요", Toast.LENGTH_LONG).show();
				}
			}
		});

		//setting
		ImageButton settingsButton = (ImageButton) findViewById(R.id.settings);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
				startActivityForResult(intent, 1);
			}

		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		TextView currentStandard = findViewById(R.id.currentStandard);
		if(requestCode == 1 && resultCode == RESULT_OK){
			airline = data.getStringExtra("airline");
			size = data.getFloatArrayExtra("size");
			currentStandard.setText(airline + "\n" + size[0] + " x " +size[1] + " x " + size[2] + " (cm)");
		}
	}

	public float[] getSize() {
		return size;
	}

	public String getAirline() {
		return airline;
	}
}