package com.example.arteam2.Utility;

import java.util.ArrayList;

public class PointCluster {
	public final float LEAST_DISTANCE = 0.1f;
	private ArrayList<float[]> points = new ArrayList<>();
	private float[] centreGravity = null;
	private float radius = 0.0f;
	
	PointCluster() {
	}
	
	PointCluster(ArrayList<float[]> points, float[] centreGravity, float radius) {
		this.points = points;
		this.centreGravity = centreGravity;
		this.radius = radius;
	}
	
	private void reArrangeRadius(float[] newCentre) {
	
	}
	
	public void mergeCircle(PointCluster p2) {
		centreGravity = new float[]{
				((getSize() * centreGravity[0]) + (p2.getSize() * p2.getCentreGravity()[0])) / (this.getSize() + p2.getSize()),
				((getSize() * centreGravity[1]) + (p2.getSize() * p2.getCentreGravity()[1])) / (this.getSize() + p2.getSize()),
				((getSize() * centreGravity[2]) + (p2.getSize() * p2.getCentreGravity()[2])) / (this.getSize() + p2.getSize())
		};
		// TODO : Merge Radius ..
		points.addAll(p2.getPoints());
	}
	
	public boolean isFitCircle(float[] point) {
		return Math.lengthBetween(centreGravity, point) <= radius;
	}
	
	public ArrayList<float[]> getPoints() {
		return points;
	}
	
	public float[] getCentreGravity() {
		return centreGravity;
	}
	
	public float getRadius() {
		return radius;
	}
	
	public int getSize() {
		return points.size();
	}
	
}
