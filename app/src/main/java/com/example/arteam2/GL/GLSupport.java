package com.example.arteam2.GL;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

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
	
	public static FloatBuffer makeFloatBuffer(int size) {
		FloatBuffer fb;
		ByteBuffer bb = ByteBuffer.allocateDirect(size);
		bb.order(ByteOrder.nativeOrder());
		fb = bb.asFloatBuffer();
		return fb;
	}
	
	public static FloatBuffer makeFloatBuffer(Map<Integer, float[]> map) {
		FloatBuffer fb;
		ByteBuffer bb = ByteBuffer.allocateDirect(map.size() * 4 * FLOAT_SIZE);
		bb.order(ByteOrder.nativeOrder());
		fb = bb.asFloatBuffer();
		for (int id : map.keySet()) {
			float[] tmp = {
					Objects.requireNonNull(map.get(id))[0],
					Objects.requireNonNull(map.get(id))[1],
					Objects.requireNonNull(map.get(id))[2],
					1.0f,
			};
			fb.put(tmp);
		}
		fb.position(0);
		return fb;
	}
	
	public static FloatBuffer makeListFloatBuffer(Map<Integer, ArrayList<float[]>> map) {
		int numElements = 0;
		for (int id : map.keySet()) {
			for (float[] mapElement : Objects.requireNonNull(map.get(id))) {
				numElements++;
			}
		}
		if (numElements == 0) return null;
		
		FloatBuffer fb;
		ByteBuffer bb = ByteBuffer.allocateDirect(map.size() * 4 * numElements * FLOAT_SIZE);
		bb.order(ByteOrder.nativeOrder());
		fb = bb.asFloatBuffer();
		for (int id : map.keySet()) {
			for (float[] mapElement : Objects.requireNonNull(map.get(id))) {
				float[] tmp = {
						mapElement[0], mapElement[1], mapElement[2], 1.0f,
				};
				fb.put(tmp);
			}
		}
		fb.position(0);
		return fb;
	}
}

