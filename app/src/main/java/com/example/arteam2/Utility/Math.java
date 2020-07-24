package com.example.arteam2.Utility;

public class Math {
	private static final String TAG = Math.class.getName();
	
	static public float lengthBetween(float[] a, float[] b) {
		float ans = 0;
		ans += (a[0] - b[0]) * (a[0] - b[0]);
		ans += (a[1] - b[1]) * (a[1] - b[1]);
		ans += (a[2] - b[2]) * (a[2] - b[2]);
		return (float) java.lang.Math.sqrt(ans);
	}
	
	static public float vectorSize(float[] a) {
		return (float) java.lang.Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
	}
	
	static public float inner(float[] a, float[] b) {
		return (a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2]);
	}
	
	static public float inner(float[] a, float[] b, int i) {
		return (a[0] * b[0 + i]) + (a[1] * b[1 + i]) + (a[2] * b[2 + i]);
	}
	
	static public float[] outer(float[] a, float[] b) {
		float[] result = new float[3];
		
		result[0] = a[1] * b[2] - a[2] * b[1];
		result[1] = a[2] * b[0] - a[0] * b[2];
		result[2] = a[0] * b[1] - a[1] * b[0];
		
		return result;
	}
	
	static public void normalize(float[] a) {
		a[0] /= vectorSize(a);
		a[1] /= vectorSize(a);
		a[2] /= vectorSize(a);
	}
	
	static public float[] IDP(float[] a, float[] b, int m, int n) {  // 내분점 : internally dividing point
		float[] idp = new float[]{0, 0, 0};
		int mn = m + n;
		
		idp[0] = (m * b[0] + n * a[0]) / mn;
		idp[1] = (m * b[1] + n * a[1]) / mn;
		idp[2] = (m * b[2] + n * a[2]) / mn;
		
		return idp;
	}
	
	static public float[] sub(float[] a, float[] b) {
		if (a.length == 4) {
			float[] result = new float[4];
			
			result[0] = a[0] - b[0];
			result[1] = a[1] - b[1];
			result[2] = a[2] - b[2];
			result[3] = 1;
			
			return result;
		}
		float[] result = new float[3];
		
		result[0] = a[0] - b[0];
		result[1] = a[1] - b[1];
		result[2] = a[2] - b[2];
		
		return result;
	}
	
}
