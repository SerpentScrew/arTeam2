package com.example.arteam2.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.arteam2.Renderer.MeasureRenderer;
import com.example.arteam2.R;
import com.google.ar.core.Session;

public class MeasureActivity extends AppCompatActivity {
	
	private GLSurfaceView measureView;
	
	private Session session;
	
	private MeasureRenderer measureRenderer = new MeasureRenderer();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_measure);
		
		measureView = findViewById(R.id.gl_surface_view);
		
		measureView.setPreserveEGLContextOnPause(true);
		measureView.setEGLContextClientVersion(2);
		measureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		measureView.setRenderer(measureRenderer);
		measureView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (session != null) {
			measureView.onPause();
			session.pause();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}