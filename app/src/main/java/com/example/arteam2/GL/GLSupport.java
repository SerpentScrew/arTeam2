package com.example.arteam2.GL;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

//  https://developer.android.com/reference/android/opengl/GLES20

public class GLSupport {
	private static final int FLOAT_SIZE = 4;
	
	public static void checkError(String tag, String label) {
		int lastError = GLES20.GL_NO_ERROR;
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(tag, label + ": glError " + error);
			lastError = error;
		}
		if (lastError != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("glError " + lastError);
		}
	}
	
	public static FloatBuffer makeFloatBuffer(float[] arr) {
		FloatBuffer fb;
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * FLOAT_SIZE);
		bb.order(ByteOrder.nativeOrder());
		fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}
}

