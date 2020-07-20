package com.example.arteam2.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.arteam2.Renderer.MeasureRenderer;
import com.example.arteam2.R;
import com.example.arteam2.Utility.FullScreenUtility;
import com.example.arteam2.Utility.PermissionUtility;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.Objects;

public class MeasureActivity extends AppCompatActivity {
	
	private GLSurfaceView measureView;
	
	private Session session;
	
	private boolean userRequestedInstall = false;
	
	private MeasureRenderer measureRenderer = new MeasureRenderer(this, new MeasureRenderer.Interface() {
		@Override
		public Session getSession() {
			return session;
		}
	});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		FullScreenUtility.makeNoStatusBar(this);
//		FullScreenUtility.makeNoTitle(this);
		
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
		
		if (session == null) {
			try {
				switch (ArCoreApk.getInstance().requestInstall(this, !userRequestedInstall)) {
					case INSTALL_REQUESTED:
						Toast.makeText(this, "ARCore Not Installed", Toast.LENGTH_SHORT).show();
						userRequestedInstall = true;
						return;
					case INSTALLED:
						Toast.makeText(this, "ARCore Installed", Toast.LENGTH_SHORT).show();
						break;
				}
				
				PermissionUtility.requestCameraPermission(this);
				
				session = new Session(this);
				Config config = new Config(session);
				session.configure(config);
				
			} catch (Exception e) {
				Log.d("Exception in onResume", Objects.requireNonNull(e.getMessage()));
				return;
			}
		}
		
		try {
			session.resume();
		} catch (CameraNotAvailableException e) {
			Log.d("Exception in Session.resume()", Objects.requireNonNull(e.getMessage()));
			session = null;
			return;
		}
		
		measureView.onResume();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}