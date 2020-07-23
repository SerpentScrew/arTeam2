package com.example.arteam2.Activity;

import android.annotation.SuppressLint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arteam2.R;
import com.example.arteam2.Renderer.BackgroundRenderer;
import com.example.arteam2.Renderer.PlaneRenderer;
import com.example.arteam2.Utility.FullScreenUtility;
import com.example.arteam2.Utility.PermissionUtility;
import com.example.arteam2.Utility.PointHandler;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;
import java.util.Objects;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MeasureActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
	ScreenStatus screenStatus = new ScreenStatus();
	
	private BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
	private PlaneRenderer planeRenderer = new PlaneRenderer();
	
	Snackbar snackbar;
	
	private Camera camera = null;
	
	private GLSurfaceView measureView;
	private PointHandler pointHandler = new PointHandler();
	
	private Session session;
	
	private boolean userRequestedInstall = false;
	private boolean surfacedCreated = false;
	private TextView numPointsView;
	
	@Override
	public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
		
		System.out.println("///////////////////////// surface created");
		
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		surfacedCreated = true;
		
		try {
			backgroundRenderer.whenGLCreate(this);
			planeRenderer.whenGLCreate(this);
			
			pointHandler.whenGLCreate(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl10, int i, int i1) {
		screenStatus.isRotated = true;
		screenStatus.width = i;
		screenStatus.height = i1;
		GLES20.glViewport(0, 0, screenStatus.width, screenStatus.height);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onDrawFrame(GL10 gl10) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		if (session == null) return;
		if (!surfacedCreated) return;
		
		if (screenStatus.isRotated) {
			int displayRotation = this.getWindowManager().getDefaultDisplay().getRotation();
			session.setDisplayGeometry(
					displayRotation, screenStatus.width, screenStatus.height
			                          );
			screenStatus.isRotated = false;
		}
		
		try {
			session.setCameraTextureName(backgroundRenderer.getTextureId());
			
			Frame frame = session.update();
			backgroundRenderer.draw(frame);
			
			camera = frame.getCamera();
			
			float[] projMTX = new float[16];
			camera.getProjectionMatrix(projMTX, 0, 0.1f, 100f);
			
			float[] viewMTX = new float[16];
			camera.getViewMatrix(viewMTX, 0);
			
			PointCloud pointCloud = frame.acquirePointCloud();
			pointHandler.draw(pointCloud, viewMTX, projMTX);
			
		} catch (CameraNotAvailableException e) {
			e.printStackTrace();
			finish();
		}
		
		
		runOnUiThread(new Runnable() {
			@SuppressLint("SetTextI18n")
			@Override
			public void run() {
				switch (pointHandler.getMode()) {
					case StanBy:
						numPointsView.setVisibility(View.INVISIBLE);
						snackbar.setText("터치하면 정보를 수집하기 시작합니다.");
						snackbar.show();
						break;
					
					case Collecting:
						snackbar.dismiss();
						numPointsView.setText("수집된 점의 개수 : " + pointHandler.getCollectedPointsNum());
						numPointsView.setVisibility(View.VISIBLE);
						if (pointHandler.hasEnoughPoint()) {
							pointHandler.collectingEnd();
						}
						break;
					
					case CollectDone:
						snackbar.setText(pointHandler.isRetry() ?
								                 "근처에 특징점이 없습니다. 다른 곳을 터치해 주세요." :
								                 "땅을 터치해 주세요!");
						snackbar.show();
						break;
					case FindingFloor:
						snackbar.setText("땅을 찾는 중입니다.");
						snackbar.show();
						break;
				}
			}
		});
		
		
		measureView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (pointHandler.getMode()) {
					case StanBy:
						pointHandler.collectingStart();
						break;
					case Collecting:
						break;
					case CollectDone:
						if (camera == null) break;
						pointHandler.findFloorStart();
						pointHandler.pickToEraseFloor(
								event.getX(), event.getY(),
								measureView.getMeasuredWidth(), measureView.getMeasuredHeight(),
								camera
						                             );
						break;
				}
				return false;
			}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FullScreenUtility.makeNoStatusBar(this);
		FullScreenUtility.makeNoTitle(this);
		
		setContentView(R.layout.activity_measure);
		
		measureView = findViewById(R.id.gl_surface_view);
		numPointsView = findViewById(R.id.numPointsView);
		
		measureView.setPreserveEGLContextOnPause(true);
		measureView.setEGLContextClientVersion(2);
		measureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		measureView.setRenderer(this);
		measureView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		
		snackbar = Snackbar.make(findViewById(android.R.id.content),
		                         "aaa",
		                         Snackbar.LENGTH_INDEFINITE);
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
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	private boolean isPlaneExist() {
		for (Plane plane : session.getAllTrackables(Plane.class)) {
			if (plane.getTrackingState() == TrackingState.TRACKING
			    && plane.getSubsumedBy() == null) {
				return true;
			}
		}
		return false;
	}
	
	
	public Camera getCamera() {
		return camera;
	}
	
	static class ScreenStatus {
		public boolean isRotated = false;
		public int width, height;
	}
}
