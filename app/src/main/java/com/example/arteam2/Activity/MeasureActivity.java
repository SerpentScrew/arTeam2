package com.example.arteam2.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.example.arteam2.Renderer.BackgroundRenderer;
import com.example.arteam2.R;
import com.example.arteam2.Renderer.PlaneRenderer;
import com.example.arteam2.Renderer.PointCloudRenderer;
import com.example.arteam2.Utility.FullScreenUtility;
import com.example.arteam2.Utility.PermissionUtility;
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
import java.nio.FloatBuffer;
import java.util.Objects;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MeasureActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
	ScreenStatus screenStatus = new ScreenStatus();
	
	private BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
	private PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
	private PlaneRenderer planeRenderer = new PlaneRenderer();
	
	private GLSurfaceView measureView;
	
	private Session session;
	
	private boolean userRequestedInstall = false;
	
	@Override
	public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		
		try {
			backgroundRenderer.whenGLCreate(this);
			pointCloudRenderer.whenGLCreate(this);
			planeRenderer.whenGLCreate(this);
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
	
	@Override
	public void onDrawFrame(GL10 gl10) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		if (session == null) return;
		
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
			
			Camera camera = frame.getCamera();
			
			float[] projMTX = new float[16];
			camera.getProjectionMatrix(projMTX, 0, 0.1f, 100f);
			
			float[] viewMTX = new float[16];
			camera.getViewMatrix(viewMTX, 0);
			
			PointCloud pointCloud = frame.acquirePointCloud();
			pointCloudRenderer.update(pointCloud);
			pointCloudRenderer.draw(viewMTX, projMTX);
			
		} catch (CameraNotAvailableException e) {
			e.printStackTrace();
			finish();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FullScreenUtility.makeNoStatusBar(this);
		FullScreenUtility.makeNoTitle(this);
		
		setContentView(R.layout.activity_measure);
		
		measureView = findViewById(R.id.gl_surface_view);
		
		measureView.setPreserveEGLContextOnPause(true);
		measureView.setEGLContextClientVersion(2);
		measureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		measureView.setRenderer(this);
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
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

//	public class PlaneFinder extends AsyncTask<Object, ResponseForm.PlaneParam, ResponseForm.PlaneParam> {
//		@Override
//		protected ResponseForm.PlaneParam doInBackground(Object... objects) {
//			// Ready Point Cloud
//			FloatBuffer points = pointCloudRenderer.finalPointBuffer;
//
//			// Ready Request Form
//			RequestForm rf = new RequestForm();
//
//			rf.setPointBufferDescription(points.capacity() / 4, 16, 0); //pointcount, pointstride, pointoffset
//			rf.setPointDataDescription(0.05f, 0.01f); //accuracy, meanDistance
//			rf.setTargetROI(pointCloudRenderer.seedPointID, Math.max(z_dis * circleRad, 0.1f));//seedIndex,touchRadius
//			rf.setAlgorithmParameter(RequestForm.SearchLevel.NORMAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp
//			Log.d("PointsBuffer", points.toString());
//			FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);
//			// Request Find Surface
//			try {
//				Log.d("PlaneFinder", "request");
//				ResponseForm resp = fsr.request(rf, points);
//				if (resp != null && resp.isSuccess()) {
//					ResponseForm.PlaneParam param = resp.getParamAsPlane();
//					Log.d("PlaneFinder", "request success");
//					return param;
//				} else {
//					Log.d("PlaneFinder", "request fail");
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return null;
//		}
//	}
	
	private boolean isPlaneExist() {
		for (Plane plane : session.getAllTrackables(Plane.class)) {
			if (plane.getTrackingState() == TrackingState.TRACKING
			    && plane.getSubsumedBy() == null) {
				return true;
			}
		}
		return false;
	}
	
	static class ScreenStatus {
		public boolean isRotated = false;
		public int width, height;
	}
}