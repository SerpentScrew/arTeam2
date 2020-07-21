package com.example.arteam2.Utility;

import android.util.Log;

public class Math {
	private static final String TAG = Math.class.getName();
	
	static class Vec3 {
		private float[] vec = new float[4];
		
		public Vec3() {
			for (float num : vec) {
				num = 0.0f;
			}
		}
		
		public Vec3(float a, float b, float c) {
			vec[0] = a;
			vec[1] = b;
			vec[2] = c;
		}
		
		public Vec3(float[] floats) {
			switch (floats.length) {
				case 0:
					for (int i = 0; i < 3; ++i) {
						vec[i] = 0.0f;
					}
					break;
				case 1:
					System.arraycopy(floats, 0, vec, 0, 1);
					vec[2] = 0.0f;
					vec[3] = 0.0f;
					break;
				case 2:
					System.arraycopy(floats, 0, vec, 0, 2);
					vec[3] = 0.0f;
					break;
				case 3:
					System.arraycopy(floats, 0, vec, 0, 3);
					break;
				default:
					Log.d(TAG, "Vec4: has more than 4 elements");
					System.exit(1);
			}
		}
		
		public float get(int i) {
			return this.vec[i];
		}
		
		public float inner(Vec3 vec3) {
			return (vec[0] * vec3.get(0)) *
			       (vec[1] * vec3.get(1)) *
			       (vec[2] * vec3.get(2));
		}
		
		public Vec3 outer(Vec3 vec3) {
			float[] ret = new float[3];
			ret[0] = (vec[1] * vec3.get(2)) - (vec[2] * vec3.get(1));
			ret[1] = (vec[2] * vec3.get(0)) - (vec[0] * vec3.get(2));
			ret[2] = (vec[0] * vec3.get(1)) - (vec[1] * vec3.get(0));
			return new Vec3(ret);
		}
		
		public Vec3 add(Vec3 vec3) {
			float[] ret = new float[3];
			for (int i = 0; i < 3; ++i) {
				ret[i] = vec[i] + vec3.get(i);
			}
			return new Vec3(ret);
		}
		
		public Vec3 sub(Vec3 vec3) {
			float[] ret = new float[3];
			for (int i = 0; i < 3; ++i) {
				ret[i] = vec[i] - vec3.get(i);
			}
			return new Vec3(ret);
		}
		
		public float size() {
			return (float) java.lang.Math.sqrt(
					(vec[0] * vec[0]) + (vec[1] * vec[1]) + (vec[2] * vec[2])
			                                  );
		}
		
		public void normalize() {
			for (int i = 0; i < 3; ++i) {
				vec[i] /= this.size();
			}
		}
		
		public Vec3 IDP(Vec3 vec3, int m, int n) {
			float[] ret = new float[3];
			for (int i = 0; i < 3; ++i) {
				ret[i] = (m * vec3.get(i) + n * vec[i]) / (m + n);
			}
			return new Vec3(ret);
		}
	}
}
