package com.example.arteam2.Utility;

import android.opengl.Matrix;

import com.google.ar.core.Camera;

public class CoreSystem {
	static public float[] screenPointToWorldRay(float xPx, float yPx, int width, int height, Camera camera) {
		//xPx : 터치한 x좌표, yPx : 터치한 y 좌표
		// ray[0~2] : camera pose
		// ray[3~5] : Unit vector of ray
		float[] ray_clip = new float[4];
		ray_clip[0] = 2.0f * xPx / width - 1.0f;
		// +y is up (android UI Y is down):
		ray_clip[1] = 1.0f - 2.0f * yPx / height;
		ray_clip[2] = -1.0f; // +z is forwards (remember clip, not camera) 클립이 뭔 뜻일까
		ray_clip[3] = 1.0f; // w (homogenous coordinates)
		//위치 조절 하는 건가봐
		
		float[] ProMatrices = new float[32];  // {proj, inverse proj}
		camera.getProjectionMatrix(ProMatrices, 0, 0.1f, 100.0f);
		Matrix.invertM(ProMatrices, 16, ProMatrices, 0);
		float[] ray_eye = new float[4];
		Matrix.multiplyMV(ray_eye, 0, ProMatrices, 16, ray_clip, 0);
		
		ray_eye[2] = -1.0f;
		ray_eye[3] = 0.0f;
		
		float[] out = new float[6];
		float[] ray_wor = new float[4];
		float[] ViewMatrices = new float[32];
		
		camera.getViewMatrix(ViewMatrices, 0);
		Matrix.invertM(ViewMatrices, 16, ViewMatrices, 0);
		Matrix.multiplyMV(ray_wor, 0, ViewMatrices, 16, ray_eye, 0);
		
		float size = (float) java.lang.Math.sqrt(ray_wor[0] * ray_wor[0] + ray_wor[1] * ray_wor[1] + ray_wor[2] * ray_wor[2]);
		
		out[3] = ray_wor[0] / size;
		out[4] = ray_wor[1] / size;
		out[5] = ray_wor[2] / size;
		
		out[0] = camera.getPose().tx();
		out[1] = camera.getPose().ty();
		out[2] = camera.getPose().tz();
		
		return out;
	}
	
}
