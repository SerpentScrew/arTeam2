package com.example.arteam2.Utility;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.arteam2.Bust;
import com.example.arteam2.GL.GLSupport;
import com.example.arteam2.Renderer.PointCloudRenderer;
import com.google.ar.core.Camera;
import com.google.ar.core.PointCloud;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class PointHandler {
	private final int REQUIRED_POINTS = 1000;
	private final float FINE_CONFIDENCE = 0.3f;
	public Map<Integer, ArrayList<float[]>> allPoints;
	public Map<Integer, float[]> filteredPoints;
	public FloatBuffer filteredBuffer = null;
	public FloatBuffer targetedBuffer = null;
	private PointCloudRenderer pcRenderer;
	private Bust mode = Bust.StanBy;
	
	private boolean isFiltered = false;
	private boolean isRetry = false;
	
	public void whenGLCreate(Context context) {
		allPoints = new HashMap<>();
		filteredPoints = new HashMap<>();
		pcRenderer = new PointCloudRenderer();
		pcRenderer.whenGLCreate(context);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void draw(PointCloud pointCloud, float[] cameraView, float[] cameraPerspective) {
		if (mode == Bust.StanBy) return;
		
		float[] modelViewProjection = new float[16];
		Matrix.multiplyMM(modelViewProjection, 0, cameraPerspective, 0, cameraView, 0);
		
		switch (mode) {
			case Collecting:
				push(pointCloud.getIds(), pointCloud.getPoints());
				pcRenderer.draw(pointCloud.getPoints(), modelViewProjection, Color.valueOf(Color.RED));
				break;
			
			case CollectDone:
				if (!isFiltered()) filterPoints();
				if (filteredBuffer == null || !filteredBuffer.hasRemaining()) break;
				pcRenderer.draw(filteredBuffer, modelViewProjection, Color.valueOf(Color.CYAN));
				break;
			
			case FindingFloor:
				if (targetedBuffer == null || !targetedBuffer.hasRemaining()) break;
				pcRenderer.draw(targetedBuffer, modelViewProjection, Color.valueOf(Color.GREEN), 20.0f);
		}
		
	}
	
	public void pickToEraseFloor(float xPx, float yPx, int width, int height, Camera camera) {
		boolean isSucceedPickPoint = false;
		float[] ray = CoreSystem.screenPointToWorldRay(xPx, yPx, width, height, camera);
		
		float thresholdDistance = 0.01f; // 10cm = 0.1m * 0.1m = 0.01f
		
		int seedPointID = -1;
		float[] seedPoint = new float[]{0, 0, 0, Float.MAX_VALUE};
		
		for (int i = 0; i < filteredBuffer.remaining(); i += 4) {
			float[] point = new float[]{filteredBuffer.get(i), filteredBuffer.get(i + 1), filteredBuffer.get(i + 2), filteredBuffer.get(i + 3)};
			float[] product = new float[]{point[0] - ray[0], point[1] - ray[1], point[2] - ray[2], 1.0f};
			
			float distanceSq = (float) (java.lang.Math.pow(product[0], 2) + java.lang.Math.pow(product[1], 2) + java.lang.Math.pow(product[2], 2));// length between camera and point
			float innerProduct = ray[3] * product[0] + ray[4] * product[1] + ray[5] * product[2];
			distanceSq = distanceSq - (innerProduct * innerProduct);  //c^2 - a^2 = b^2
			
			// determine candidate points
			if (distanceSq < thresholdDistance && distanceSq < seedPoint[3]) {
				seedPoint[0] = point[0];
				seedPoint[1] = point[1];
				seedPoint[2] = point[2];
				seedPoint[3] = distanceSq;
				seedPointID = i / 4;
				isSucceedPickPoint = true;
				targetedBuffer = GLSupport.makeFloatBuffer(seedPoint);
			}
		}
		if (!isSucceedPickPoint) {
			isRetry = true;
			collectingEnd();
		}
		Log.d("pickSeed", String.format("%.2f %.2f %.2f : %d", seedPoint[0], seedPoint[1], seedPoint[2], seedPointID));
	}
	
	public void push(IntBuffer ID, FloatBuffer pointCloud) {
		for (int i = 0; i < pointCloud.capacity() / 4; i++) {
			float[] temp = {pointCloud.get(i * 4), pointCloud.get(i * 4 + 1), pointCloud.get(i * 4 + 2), pointCloud.get(i * 4 + 3)};
			//if hash map's IDth element doesn't exist, create array list
			if (temp[3] < FINE_CONFIDENCE) continue;
			
			int id = ID.get(i);
			if (!allPoints.containsKey(id)) {
				ArrayList<float[]> list = new ArrayList<>();
				list.add(temp);
				allPoints.put(id, list);
			} else {
				Objects.requireNonNull(allPoints.get(id)).add(temp);
			}
		}
	}
	
	public void filterPoints() {
		for (int id : allPoints.keySet()) {
			ArrayList<float[]> list = allPoints.get(id);
			float mean_x = 0.0f, mean_y = 0.0f, mean_z = 0.0f;
			for (float[] p : list) {
				mean_x += p[0]; //  x
				mean_y += p[1]; //  y
				mean_z += p[2]; //  z
			}
			mean_z /= list.size();
			mean_x /= list.size();
			mean_y /= list.size();
			
			if (list.size() < 5) {
				float[] finalPoint = new float[]{mean_x, mean_y, mean_z};
				filteredPoints.put(id, finalPoint);
				continue;   // no more calculation
			}
			
			// calculate variance
			float distance_mean = 0.f;
			float variance = 0.f;
			for (float[] tmp : list) {
				float sqDistance = (float) (java.lang.Math.pow((tmp[0] - mean_x), 2.0) + java.lang.Math.pow((tmp[1] - mean_y), 2.0) + java.lang.Math.pow((tmp[2] - mean_z), 2.0));
				variance += sqDistance;
				distance_mean += java.lang.Math.sqrt(sqDistance);
			}
			distance_mean /= list.size();
			variance = (variance / list.size()) - distance_mean * distance_mean;
			
			// variance가 0일 때
			if (variance == 0) {
				float[] finalPoint = new float[]{mean_x, mean_y, mean_z};
				filteredPoints.put(id, finalPoint);
				continue; // no more calculation
			} else {
				Iterator<float[]> iter = list.iterator();
				while (iter.hasNext()) {
					float[] tmp = iter.next();
					float sqDistance = (float) (java.lang.Math.pow((tmp[0] - mean_x), 2) + java.lang.Math.pow((tmp[1] - mean_y), 2) + java.lang.Math.pow((tmp[2] - mean_z), 2));
					float z_score = (float) (java.lang.Math.abs(java.lang.Math.sqrt(sqDistance) - distance_mean) / java.lang.Math.sqrt(variance));
					if (z_score >= 1.2f) {
						iter.remove();
					}
				}
				
				if (list.size() == 0) continue;
				
				mean_x = 0.f;
				mean_y = 0.f;
				mean_z = 0.f;
				for (float[] tmp : list) {
					mean_x += tmp[0];
					mean_y += tmp[1];
					mean_z += tmp[2];
				}
				mean_x /= list.size();
				mean_z /= list.size();
				mean_y /= list.size();
				
				filteredPoints.put(id, new float[]{mean_x, mean_y, mean_z});
			}
		}
		
		isFiltered = true;
		filteredBuffer = GLSupport.makeFloatBuffer(filteredPoints);
	}
	
	public void collectingStart() {
		mode = Bust.Collecting;
	}
	
	public void collectingEnd() {
		mode = Bust.CollectDone;
	}
	
	public void findFloorStart() {
		mode = Bust.FindingFloor;
	}
	
	public void findFloorEnd() {
		mode = Bust.FoundFloor;
	}
	
	public Bust getMode() {
		return mode;
	}
	
	public boolean isRetry() {
		return isRetry;
	}
	
	public int getCollectedPointsNum() {
		return allPoints.size();
	}
	
	public boolean hasEnoughPoint() {
		return allPoints.size() > REQUIRED_POINTS;
	}
	
	public boolean isFiltered() {
		return isFiltered;
	}
}