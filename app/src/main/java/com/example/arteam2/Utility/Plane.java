package com.example.arteam2.Utility;

import android.annotation.SuppressLint;

public class Plane {
	private float[] ll, lr, ul, ur;
	private float[] normal = null;
	
	private float[] planeVertex;
	
	public Plane(float[] ll, float[] lr, float[] ur, float[] ul) {
		this.ll = ll;
		this.lr = lr;
		this.ul = ul;
		this.ur = ur;
		
		planeVertex = new float[]{
				ul[0], ul[1], ul[2],
				ll[0], ll[1], ll[2],
				lr[0], lr[1], lr[2],
				ur[0], ur[1], ur[2],
		};
	}
	
	public Plane(float[] ll, float[] lr, float[] ur, float[] ul, float[] z_dir) {
		this.ll = ll;
		this.lr = lr;
		this.ul = ul;
		this.ur = ur;
		
		planeVertex = new float[]{
				ul[0], ul[1], ul[2],
				ll[0], ll[1], ll[2],
				lr[0], lr[1], lr[2],
				ur[0], ur[1], ur[2],
		};
		
		normal = new float[3];
		this.calNormal();
		this.checkNormal(z_dir);
	}
	
	public float[] getPlaneVertex() {
		return planeVertex;
	}
	
	protected void calNormal() {
		// Calculate normal vector
		float[] vec1 = Math.sub(lr, ll);
		float[] vec2 = Math.sub(ul, ll);
		
		this.normal = Math.outer(vec1, vec2);
		Math.normalize(this.normal);
	}
	
	public float getDVal() {
		// D = 0 - Ax - By - Cz
		return -normal[0] * ll[0]
		       - normal[1] * ll[1]
		       - normal[2] * ll[2];
	}
	
	public float[] getll() {
		return ll;
	}
	
	public float[] getlr() {
		return lr;
	}
	
	public float[] getul() {
		return ul;
	}
	
	public float[] getur() {
		return ur;
	}
	
	public float[] getNormal() {
		return normal;
	}
	
	
	public void checkNormal(float[] z_dir) {
		if (z_dir[0] * normal[0] + z_dir[1] * normal[1] + z_dir[2] * normal[2] >= 0) return;
		
		normal[0] = -normal[0];
		normal[1] = -normal[1];
		normal[2] = -normal[2];
	}
	
	public boolean isInputUL(float[] ll, float[] ur, float[] tmp) {
		{
			float[] normInput = Math.outer(Math.sub(ur, ll), Math.sub(tmp, ll));
			float[] planeNorm = Math.outer(Math.sub(ur, ll), Math.sub(this.ul, ll));
			return (Math.inner(this.normal, planeNorm) >= 0) && (Math.inner(this.normal, normInput) >= 0);
		}
	}
	
	@Override
	public String toString() {
		@SuppressLint("DefaultLocale")
		String ret = String.format("ll: (%f, %f, %f),\n" +
		                           "lr: (%f, %f, %f),\n" +
		                           "ul: (%f, %f, %f),\n" +
		                           "ur: (%f, %f, %f)\n",
		                           ll[0], ll[1], ll[2],
		                           lr[0], lr[1], lr[2],
		                           ul[0], ul[1], ul[2],
		                           ur[0], ur[1], ur[2]
		                          );
		return ret;
	}
}
