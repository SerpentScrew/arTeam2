package com.example.arteam2.GL;

import android.opengl.GLES20;
import android.util.Log;

//  https://developer.android.com/reference/android/opengl/GLES20

public class GLSupport {
	public static void check(String tag, String label) {
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
}

