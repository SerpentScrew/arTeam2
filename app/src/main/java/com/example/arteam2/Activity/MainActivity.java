package com.example.arteam2.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.arteam2.R;
import com.example.arteam2.Utility.FullScreenUtility;

public class MainActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FullScreenUtility.makeNoStatusBar(this);
		FullScreenUtility.makeNoTitle(this);

		setContentView(R.layout.activity_main);

		Button b = (Button) findViewById(R.id.button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(
						getApplicationContext(), // 현재 화면의 제어권자
						MeasureActivity.class); // 다음 넘어갈 클래스 지정
				startActivity(intent); // 다음 화면으로 넘어간다
			}
		});
	}


}