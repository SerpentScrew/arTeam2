package com.example.arteam2.Utility;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.arteam2.Bust;
import com.example.arteam2.GL.GLSupport;
import com.example.arteam2.R;
import com.example.arteam2.Renderer.MainRenderer;
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
	public final int REQUIRED_POINTS = 5000;
	public final float FINE_CONFIDENCE = 0.3f;
	
	private Map<Integer, ArrayList<float[]>> allPoints = null;
	private Map<Integer, float[]> filteredPoints;
	private Map<Integer, float[]> objectPoints = null;
	private Map<Integer, float[]> deletedPoints = null;
	
	private FloatBuffer filteredBuffer = null;
	private FloatBuffer targetedBuffer = null;
	private FloatBuffer orThoedBuffer = null;
	private FloatBuffer targetedOrthoBuffer = null;
	
	
	private MainRenderer renderer;
	
	private Bust mode = Bust.StanBy;
	
	private int seedPointID = -1;
	private float[] pointForDrawingPlane = null;
	private float[] pointForDrawingPlane2 = null;
	private float[] pointForDrawingCube = null;
	
	private float[] ur = null;
	private float[] ll = null;
	private float[] ul = null;
	private float[] lr = null;
	
	private float boxHeight = -1;
	private int boxID = -1;
	
	// 평면의 법선벡터
	private float[] groundNorm = null;
	// ax + by + cz + d = 0 의 d 값
	private float groundDVal = -1;
	
	private float z_dis = Float.MIN_VALUE;
	
	private boolean isFiltered = false;
	private boolean isDeleted = false;
	private boolean isGenerated = false;
	private boolean isCubeMade = false;
	
	private int collectDoneMsg = R.string.press_ground;
	// TODO : 이거 그냥 디버그용. 지우기
	private Plane plane = null;
	private Plane plane2 = null;
	
	private int collectOrthoDoneMsg = R.string.press_ground;
	
	public void whenGLCreate(Context context) {
		filteredPoints = new HashMap<>();
		renderer = new MainRenderer();
		renderer.whenGLCreate(context);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void draw(PointCloud pointCloud, float[] cameraView, float[] cameraPerspective) {
		if (mode == Bust.StanBy) return;
		
		float[] modelViewProjection = new float[16];
		Matrix.multiplyMM(modelViewProjection, 0, cameraPerspective, 0, cameraView, 0);
		
		// TODO : 쓰레드가 널 참조하지 않게 하는 법 모르겠어서 그냥 순서도 식으로 일단 짬
		// TODO : 간지나게 바꿀 필요 있음
		switch (mode) {
			case Collecting:
				push(pointCloud.getIds(), pointCloud.getPoints());
				renderer.pointDraw(pointCloud.getPoints(), modelViewProjection, Color.valueOf(Color.RED));
				break;
			
			case CollectDone:
				if (!isFiltered()) filterPoints();
				if (filteredBuffer == null || !filteredBuffer.hasRemaining()) break;
				renderer.pointDraw(filteredBuffer, modelViewProjection, Color.valueOf(Color.CYAN));
				break;
			
			case FindingFloor:
				if (targetedBuffer == null || !targetedBuffer.hasRemaining()) break;
				renderer.pointDraw(targetedBuffer, modelViewProjection, Color.valueOf(Color.GREEN), 20.0f);
				break;
			
			case FoundFloor:
				if (plane == null) break;
				if (pointForDrawingPlane == null) {
					pointForDrawingPlane = new float[]{
							plane.getll()[0], plane.getll()[1], plane.getll()[2], 1.0f,
							plane.getlr()[0], plane.getlr()[1], plane.getlr()[2], 1.0f,
							plane.getur()[0], plane.getur()[1], plane.getur()[2], 1.0f,
							plane.getll()[0], plane.getll()[1], plane.getll()[2], 1.0f,
							plane.getur()[0], plane.getur()[1], plane.getur()[2], 1.0f,
							plane.getul()[0], plane.getul()[1], plane.getul()[2], 1.0f
					};
				}
				renderer.planeDraw(
						GLSupport.makeFloatBuffer(pointForDrawingPlane), modelViewProjection, Color.valueOf(Color.YELLOW)
				                  );
				break;
			
			case DeletingFloor:
				renderer.planeDraw(
						GLSupport.makeFloatBuffer(pointForDrawingPlane), modelViewProjection, Color.valueOf(Color.YELLOW)
				                  );
				if (!isDeleted) break;
				if (deletedPoints.size() <= 0) break;
				renderer.pointDraw(
						GLSupport.makeFloatBuffer(deletedPoints), modelViewProjection, Color.valueOf(Color.BLACK)
				                  );
				break;
			case FloorDeleted:
				if (objectPoints.size() <= 0) break;
				renderer.pointDraw(
						GLSupport.makeFloatBuffer(objectPoints), modelViewProjection, Color.valueOf(Color.BLUE)
				                  );
				break;
			case OrthoProject:
				if (!isGenerated()) break;
				if (orThoedBuffer == null || !orThoedBuffer.hasRemaining()) break;
				renderer.pointDraw(orThoedBuffer, modelViewProjection, Color.valueOf(Color.MAGENTA), 8.0f);
				break;
			
			case FindingOrthoFloor:
//				if (targetedOrthoBuffer == null || !targetedOrthoBuffer.hasRemaining()) break;
//				renderer.pointDraw(targetedOrthoBuffer, modelViewProjection, Color.valueOf(Color.GREEN), 20.0f);
				if (pointForDrawingPlane2 == null) break;
				renderer.planeDraw(
						GLSupport.makeFloatBuffer(pointForDrawingPlane2), modelViewProjection, Color.valueOf(Color.YELLOW)
				                  );
				break;
			
			case FoundOrthoFloor:
				if (!isCubeMade) break;
				if (pointForDrawingCube == null) break;
				renderer.planeDraw(
						GLSupport.makeFloatBuffer(pointForDrawingCube), modelViewProjection, Color.valueOf(Color.BLUE)
				                  );
				break;
			
		}
	}
	
	public void makeFunCube() {
		float[] lul = ul;
		float[] lur = ur;
		float[] lll = ll;
		float[] llr = lr;
		
		float[] uul = new float[]{
				lul[0] + java.lang.Math.abs(groundNorm[0] * boxHeight),
				lul[1] + java.lang.Math.abs(groundNorm[1] * boxHeight),
				lul[2] + java.lang.Math.abs(groundNorm[2] * boxHeight)
		};
		
		float[] uur = new float[]{
				lur[0] + java.lang.Math.abs(groundNorm[0] * boxHeight),
				lur[1] + java.lang.Math.abs(groundNorm[1] * boxHeight),
				lur[2] + java.lang.Math.abs(groundNorm[2] * boxHeight)
		};
		
		float[] ull = new float[]{
				lll[0] + java.lang.Math.abs(groundNorm[0] * boxHeight),
				lll[1] + java.lang.Math.abs(groundNorm[1] * boxHeight),
				lll[2] + java.lang.Math.abs(groundNorm[2] * boxHeight)
		};
		
		float[] ulr = new float[]{
				llr[0] + java.lang.Math.abs(groundNorm[0] * boxHeight),
				llr[1] + java.lang.Math.abs(groundNorm[1] * boxHeight),
				llr[2] + java.lang.Math.abs(groundNorm[2] * boxHeight)
		};
		
		float[][] boxPoints = new float[][]{
				ull, lll, ulr, ulr, lll, llr,
				ulr, llr, uur, uur, llr, lur,
				uur, lur, uul, uul, lur, lul,
				uul, lul, ull, ull, lul, lll,
				uul, ull, uur, uur, ull, ulr,
				lll, lul, llr, llr, lul, lur
		};
		
		if (pointForDrawingCube == null)
			pointForDrawingCube = new float[4 * 6 * 6];
		
		
		int k = 0;
		for (int i = 0; i < boxPoints.length; ++i) {
			for (int j = 0; j < boxPoints[i].length; ++j) {
				pointForDrawingCube[k++] = boxPoints[i][j];
			}
			pointForDrawingCube[k++] = 0.0f;
		}
		
		isCubeMade = true;
	}
	
	public void orThoObject() {
		HashMap<Integer, float[]> tempMap = new HashMap<>();
		
		float a = groundNorm[0];
		float b = groundNorm[1];
		float c = groundNorm[2];
		float d = groundDVal;
		
		int maxID = -1;
		
		for (int ID : objectPoints.keySet()) {
			if (ID > maxID) maxID = ID;
			
			float[] p = objectPoints.get(ID);
			if (p == null) continue;
			
			double distance =
					((a * p[0]) + (b * p[1]) + (c * p[2]) + d)
					/ java.lang.Math.sqrt((a * a) + (b * b) + (c * c));
			
			double absDistance = java.lang.Math.abs(distance);
			
			if (distance > boxHeight) {
				boxID = ID;
				boxHeight = (float) absDistance;
			}
			
			p[0] = p[0] - (a * (float) distance);
			p[1] = p[1] - (b * (float) distance);
			p[2] = p[2] - (c * (float) distance);
			
			tempMap.put(ID, p);
		}
		
		if (tempMap.isEmpty()) {
			Log.d(this.getClass().getName(), "orThoObject: (Box Id : " + boxID + ", BoxHeight : " + boxHeight + ")");
			return;
		}
		
		float[] point = new float[]{
				Objects.requireNonNull(tempMap.get(boxID))[0],
				Objects.requireNonNull(tempMap.get(boxID))[1],
				Objects.requireNonNull(tempMap.get(boxID))[2],
				1.0f
		};
		
		targetedOrthoBuffer = GLSupport.makeFloatBuffer(Objects.requireNonNull(point));
		
		
		// TODO : 일단 자꾸 request fail 해서 정규분포라 가정하고 신뢰구간 이용해서 땅 찾음
		float[] avg = new float[]{0.0f, 0.0f, 0.0f};
		int size = 0;
		for (int ID : tempMap.keySet()) {
			avg[0] += tempMap.get(ID)[0];
			avg[1] += tempMap.get(ID)[1];
			avg[2] += tempMap.get(ID)[2];
			size++;
		}
		avg[0] /= size;
		avg[1] /= size;
		avg[2] /= size;
		
		float avgDistance = 0.0f;
		double cubeAvg = 0.0f;
		size = 0;
		for (int ID : tempMap.keySet()) {
			float length = Math.lengthBetween(avg, tempMap.get(ID));
			avgDistance += length;
			cubeAvg += java.lang.Math.pow(length, 2);
			size++;
		}
		avgDistance /= size;
		cubeAvg /= size;
		double standardDeviation = java.lang.Math.sqrt(cubeAvg - java.lang.Math.pow(avgDistance, 2));
		
		System.out.println("avg : " + avgDistance + ", 표준편차 : " + standardDeviation + ", size : " + size);
		
		HashMap<Integer, float[]> projectedPoints = new HashMap<>();
		for (int ID : tempMap.keySet()) {
			if (avgDistance - (2.58 * standardDeviation) <= Math.lengthBetween(avg, tempMap.get(ID))
			    && Math.lengthBetween(avg, tempMap.get(ID)) <= avgDistance + (2.58 * standardDeviation)) {
				projectedPoints.put(ID, tempMap.get(ID));
			}
		}
		
		
		if (projectedPoints.isEmpty()) {
			Log.d(this.getClass().getName(), "orThoObject: something wrong with projectedPoints");
		}
		
		System.out.println("///////////// size : " + projectedPoints.size());
		
		orThoedBuffer = GLSupport.makeFloatBuffer(projectedPoints);
		
		///////////////////
		avg[0] = 0;
		avg[1] = 0;
		avg[2] = 0;
		size = 0;
		for (int ID : projectedPoints.keySet()) {
			avg[0] += projectedPoints.get(ID)[0];
			avg[1] += projectedPoints.get(ID)[1];
			avg[2] += projectedPoints.get(ID)[2];
			size++;
		}
		avg[0] /= size;
		avg[1] /= size;
		avg[2] /= size;
		
		float[] max = new float[]{0.0f, 0.0f, 0.0f};
		double curMaxLen = 0.0f;
		for (int ID : projectedPoints.keySet()) {
			double len = Math.lengthBetween(avg, projectedPoints.get(ID));
			if (curMaxLen < len) {
				curMaxLen = len;
				max[0] = projectedPoints.get(ID)[0];
				max[1] = projectedPoints.get(ID)[1];
				max[2] = projectedPoints.get(ID)[2];
			}
		}
		
		ur = new float[]{
				max[0], max[1], max[2]
		};
		ll = new float[]{
				(2 * avg[0]) - ur[0],
				(2 * avg[1]) - ur[1],
				(2 * avg[2]) - ur[2]
		};
		
		float[] lineVector = new float[]{
				ll[0] - ur[0],
				ll[1] - ur[1],
				ll[2] - ur[2]
		};
		
		ul = null;
		lr = null;
		
		float[] iteratingPoint = new float[]{0.0f, 0.0f, 0.0f};
		
		curMaxLen = 0;
		for (int ID : projectedPoints.keySet()) {
			float[] pointPointVector = new float[]{
					projectedPoints.get(ID)[0] - ur[0],
					projectedPoints.get(ID)[1] - ur[1],
					projectedPoints.get(ID)[2] - ur[2]
			};
			
			double tmp =
					Math.vectorSize(Math.outer(lineVector, pointPointVector))
					/ Math.vectorSize(lineVector);
			
			if (curMaxLen < tmp) {
				curMaxLen = tmp;
				iteratingPoint[0] = projectedPoints.get(ID)[0];
				iteratingPoint[1] = projectedPoints.get(ID)[1];
				iteratingPoint[2] = projectedPoints.get(ID)[2];
			}
		}
		
		float[] oppositeIteratingPoint = {
				(2 * avg[0]) - iteratingPoint[0],
				(2 * avg[1]) - iteratingPoint[1],
				(2 * avg[2]) - iteratingPoint[2]
		};
		
		if (Math.inner(
				plane.getNormal(),
				Math.outer(Math.sub(ll, ur), Math.sub(ll, iteratingPoint))
		              ) >= 0) {
			ul = iteratingPoint;
			lr = oppositeIteratingPoint;
		} else {
			ul = oppositeIteratingPoint;
			lr = iteratingPoint;
		}
		
		if (pointForDrawingPlane2 == null) {
			pointForDrawingPlane2 = new float[]{
					ll[0], ll[1], ll[2], 1.0f,
					lr[0], lr[1], lr[2], 1.0f,
					ur[0], ur[1], ur[2], 1.0f,
					ll[0], ll[1], ll[2], 1.0f,
					ur[0], ur[1], ur[2], 1.0f,
					ul[0], ul[1], ul[2], 1.0f
			};
		}
		isGenerated = true;
	}
	
	public void deleteFloor() {
		float[] vec1 = Math.sub(plane.getlr(), plane.getll());
		float[] vec2 = Math.sub(plane.getul(), plane.getll());
		
		groundNorm = Math.outer(vec1, vec2);
		Math.normalize(groundNorm);
		groundDVal =
				-(groundNorm[0] * plane.getul()[0]
				  + groundNorm[1] * plane.getul()[1]
				  + groundNorm[2] * plane.getul()[2]);
		
		if (objectPoints == null) objectPoints = new HashMap<>();
		if (deletedPoints == null) deletedPoints = new HashMap<>();
		final float epsilon = 0.10f;
		
		for (int id : filteredPoints.keySet()) {
			float[] point = filteredPoints.get(id);
			if (point == null) continue;
			float a = Math.inner(groundNorm, point);
			if (a < -groundDVal - epsilon || a > -groundDVal + epsilon) {
				objectPoints.put(id, point);
				continue;
			}
			deletedPoints.put(id, point);
		}
		
		isDeleted = true;
	}
	
	
	public void pickToEraseFloor(float xPx, float yPx, int width, int height, Camera camera) {
		boolean isSucceedPickPoint = false;
		float[] ray = CoreSystem.screenPointToWorldRay(xPx, yPx, width, height, camera);
		
		float thresholdDistance = 0.01f; // 10cm = 0.1m * 0.1m = 0.01f
		
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
			setCollectDoneMsg(R.string.press_ground_noFeaturePoint);
			collectingEnd();
			return;
		}
		z_dis = seedPoint[2];
		Log.d("pickSeed", String.format("%.2f %.2f %.2f : %d", seedPoint[0], seedPoint[1], seedPoint[2], seedPointID));
	}
	
	public void push(IntBuffer ID, FloatBuffer pointCloud) {
		if (allPoints == null) allPoints = new HashMap<>();
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
			if (list == null) continue;
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
			}
			
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
		
		isFiltered = true;
		filteredBuffer = GLSupport.makeFloatBuffer(filteredPoints);
	}
	
	/*
	이하 그냥 mode switch 하는 것,
	TODO : 간지나는 방법 찾기 **
	 */
	
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
	
	public void deletingFloorStart() {
		mode = Bust.DeletingFloor;
	}
	
	public void deletingFloorEnd() {
		mode = Bust.FloorDeleted;
	}
	
	public void orthoProjectingStart() {
		mode = Bust.OrthoProject;
	}
	
	public void findOrthoFloorStart() {
		mode = Bust.FindingOrthoFloor;
	}
	
	public void findOrthoFloorEnd() {
		mode = Bust.FoundOrthoFloor;
	}
	
	/*
	이하 그냥 getters. 볼 필요 x
	 */
	public int getCollectDoneMsg() {
		return collectDoneMsg;
	}
	
	public int getCollectOrthoDoneMsg() {
		return collectOrthoDoneMsg;
	}
	
	public Bust getMode() {
		return mode;
	}
	
	public void setCollectOrthoDoneMsg(int collectOrthoDoneMsg) {
		this.collectOrthoDoneMsg = collectOrthoDoneMsg;
	}
	
	public void setPlane(Plane plane) {
		this.plane = plane;
	}
	
	public void setCollectDoneMsg(int collectDoneMsg) {
		this.collectDoneMsg = collectDoneMsg;
	}
	
	public void setPlane2(Plane plane) {
		this.plane2 = plane;
	}
	
	public int getCollectedPointsNum() {
		if (allPoints == null) return 0;
		return allPoints.size();
	}
	
	public boolean hasEnoughPoint() {
		if (allPoints == null) return false;
		return allPoints.size() > REQUIRED_POINTS;
	}
	
	public boolean isFiltered() {
		return isFiltered;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}
	
	public FloatBuffer getFilteredBuffer() {
		return filteredBuffer;
	}
	
	public int getBoxID() {
		return boxID;
	}
	
	public float getBoxHeight() {
		return boxHeight;
	}
	
	public FloatBuffer getOrThoedBuffer() {
		return orThoedBuffer;
	}
	
	public boolean isGenerated() {
		return isGenerated;
	}
	
	public int getSeedPointID() {
		return seedPointID;
	}
	
	//	public void orThoObject() {
//		HashMap<Integer, float[]> projectedPoints = new HashMap<>();
//
//		float a = groundNorm[0];
//		float b = groundNorm[1];
//		float c = groundNorm[2];
//		float d = groundDVal;
//
//		int maxID = -1;
//
//		for (int ID : objectPoints.keySet()) {
//			if (ID > maxID) maxID = ID;
//
//			float[] p = objectPoints.get(ID);
//			if (p == null) continue;
//
//			double distance =
//					((a * p[0]) + (b * p[1]) + (c * p[2]) + d)
//					/ java.lang.Math.sqrt((a * a) + (b * b) + (c * c));
//
//			double absDistance = java.lang.Math.abs(distance);
//
//			if (distance > boxHeight) {
//				boxID = ID;
//				boxHeight = (float) absDistance;
//			}
//
//			p[0] = p[0] - (a * (float) distance);
//			p[1] = p[1] - (b * (float) distance);
//			p[2] = p[2] - (c * (float) distance);
//
//			projectedPoints.put(ID, p);
//		}
//
//		if (projectedPoints.isEmpty()) {
//			Log.d(this.getClass().getName(), "orThoObject: (Box Id : " + boxID + ", BoxHeight : " + boxHeight + ")");
//			return;
//		}
//
//		Log.d(this.getClass().getName(), "orThoObject: (Box Id : " + boxID + ", BoxHeight : " + boxHeight + ")");
//
//		float[] point = new float[]{
//				Objects.requireNonNull(projectedPoints.get(boxID))[0],
//				Objects.requireNonNull(projectedPoints.get(boxID))[1],
//				Objects.requireNonNull(projectedPoints.get(boxID))[2],
//				1.0f
//		};
//		targetedOrthoBuffer = GLSupport.makeFloatBuffer(
//				Objects.requireNonNull(point)
//		                                               );
//
//		float[] customPoint = new float[3];
//		System.out.println("///////////// size : " + projectedPoints.size());
//		HashMap<Integer, float[]> copiedMap = GLSupport.copyMap(projectedPoints);
//		System.out.println("maxID : " + maxID);
//		int num = 0, num1 = 0;
//		for (int IDLeft : copiedMap.keySet()) {
//			num1++;
//			for (int IDRight : copiedMap.keySet()) {
//				if (IDLeft == IDRight) continue;
//				if (Math.lengthBetween(copiedMap.get(IDLeft), copiedMap.get(IDRight)) < 0.03)
//					continue;
//				customPoint[0] = (Objects.requireNonNull(copiedMap.get(IDLeft))[0] / 2)
//				                 + (Objects.requireNonNull(copiedMap.get(IDRight))[0] / 2);
//				customPoint[1] = (Objects.requireNonNull(copiedMap.get(IDLeft))[1] / 2)
//				                 + (Objects.requireNonNull(copiedMap.get(IDRight))[1] / 2);
//				customPoint[2] = (Objects.requireNonNull(copiedMap.get(IDLeft))[2] / 2)
//				                 + (Objects.requireNonNull(copiedMap.get(IDRight))[2] / 2);
////				System.out.println("maxID : " + maxID + " customPoint : (" + customPoint[0] + ", " + customPoint[1] + ", " + customPoint[2] + ")");
//				projectedPoints.put(++maxID, customPoint);
//				num++;
//			}
//		}
//		System.out.println("///////////// size : " + projectedPoints.size());
//		System.out.println("maxID : " + maxID + ", num : " + num + ", num1 : " + num1);
//		orThoedBuffer = GLSupport.makeFloatBuffer(projectedPoints);
//		System.out.println("orThoedBuffer remaining : " + orThoedBuffer.remaining());
//		isGenerated = true;
//	}
	
}