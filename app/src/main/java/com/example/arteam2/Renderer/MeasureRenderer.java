package com.example.arteam2.Renderer;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.widget.TextView;

import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MeasureRenderer implements GLSurfaceView.Renderer {
	Interface ssInterface;
	ScreenStatus screenStatus = new ScreenStatus();
	private Activity activity;
	private BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
	private PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
	private PlaneRenderer planeRenderer = new PlaneRenderer();
	
	public MeasureRenderer(Activity activity, Interface sessionInterface) {
		this.activity = activity;
		this.ssInterface = sessionInterface;
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		try {
			backgroundRenderer.whenGLCreate(activity);
			pointCloudRenderer.whenGLCreate(activity);
			planeRenderer.whenGLCreate(activity);
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
		
		if (ssInterface.getSession() == null) return;
		
		if (screenStatus.isRotated) {
			int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
			ssInterface.getSession().setDisplayGeometry(
					displayRotation, screenStatus.width, screenStatus.height
			                                           );
			screenStatus.isRotated = false;
		}
		
		try {
			ssInterface.getSession().setCameraTextureName(backgroundRenderer.getTextureId());
			
			Frame frame = ssInterface.getSession().update();
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
			activity.finish();
		}
	}
	
	private boolean isPlaneExist() {
		for (Plane plane : ssInterface.getSession().getAllTrackables(Plane.class)) {
			if (plane.getTrackingState() == TrackingState.TRACKING
			    && plane.getSubsumedBy() == null) {
				return true;
			}
		}
		return false;
	}
	
	public interface Interface {
		Session getSession();
	}
	
	static class ScreenStatus {
		public boolean isRotated = false;
		public int width, height;
	}
}
