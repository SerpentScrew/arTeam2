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
import com.google.ar.core.Pose;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class PointHandler {
	public final int REQUIRED_POINTS = 2000;
	public final float FINE_CONFIDENCE = 0.3f;
	public final float epsilon = 0.05f;
	
	private Map<Integer, ArrayList<float[]>> allPoints = null;
	private Map<Integer, float[]> filteredPoints;
	private Map<Integer, float[]> objectPoints = null;
	private Map<Integer, float[]> deletedPoints = null;
	
	private FloatBuffer filteredBuffer = null;
	private FloatBuffer targetedBuffer = null;
	private FloatBuffer orThoedBuffer = null;
	
	private MainRenderer renderer;
	private MainRenderer subRenderer;
	
	private Bust mode = Bust.StanBy;
	
	private int seedPointID = -1;
	private float[] pointForDrawingPlane = null;
	private float[] pointForDrawingPlane2 = null;
	private float[] pointForDrawingCube = null;
	
	private float boxHeight = -1;
	private int boxID = -1;
	
	float width;
	float height;
	
	private float z_dis = Float.MIN_VALUE;
	
	private boolean isFiltered = false;
	private boolean isDeleted = false;
	private boolean isGenerated = false;
	private boolean isCubeMade = false;
	
	private int collectDoneMsg = R.string.press_ground;
	
	private Plane plane = null;
	private Plane planeObject = null;
	
	private int collectOrthoDoneMsg = R.string.press_ground;
	
	public void whenGLCreate(Context context) {
		filteredPoints = new HashMap<>();
		renderer = new MainRenderer();
		subRenderer = new MainRenderer();
		renderer.whenGLCreate(context);
		subRenderer.whenGLCreate(context);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void draw(PointCloud pointCloud, Pose pose, float[] cameraView, float[] cameraPerspective) {
		if (mode == Bust.StanBy) return;
		
		float[] modelViewProjection = new float[16];
		Matrix.multiplyMM(modelViewProjection, 0, cameraPerspective, 0, cameraView, 0);
		
		// TODO : 쓰레드가 널 참조하지 않게 하는 법 모르겠어서 그냥 순서도 식으로 일단 짬
		// TODO : 간지나게 바꿀 필요 있음
		switch (mode) {
			case Collecting:
				push(pose, modelViewProjection, pointCloud.getIds(), pointCloud.getPoints());
//				renderer.pointDraw(checkIt(pose, pointCloud.getPoints(), modelViewProjection), modelViewProjection, Color.valueOf(Color.RED), 10.0f);
				renderer.pointDraw(pointCloud.getPoints(), modelViewProjection, Color.valueOf(Color.RED), 10.0f);
				break;
			
			case CollectDone:
				if (!isFiltered()) filterPoints();
				if (filteredBuffer == null || !filteredBuffer.hasRemaining()) break;
				renderer.pointDraw(filteredBuffer, modelViewProjection, Color.valueOf(Color.CYAN), 10.0f);
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
				renderer.planeDraw(GLSupport.makeFloatBuffer(pointForDrawingPlane), modelViewProjection, Color.valueOf(Color.YELLOW));
				break;
			
			case DeletingFloor:
				renderer.planeDraw(GLSupport.makeFloatBuffer(pointForDrawingPlane), modelViewProjection, Color.valueOf(Color.YELLOW));
				if (!isDeleted) break;
				if (deletedPoints.size() <= 0) break;
				renderer.pointDraw(GLSupport.makeFloatBuffer(deletedPoints), modelViewProjection, Color.valueOf(Color.BLACK), 10.0f);
				break;
			case FloorDeleted:
				if (objectPoints.size() <= 0) break;
				renderer.pointDraw(GLSupport.makeFloatBuffer(objectPoints), modelViewProjection, Color.valueOf(Color.BLUE), 10.0f);
				break;
			case OrthoProject:
				if (!isGenerated()) break;
				if (orThoedBuffer == null || !orThoedBuffer.hasRemaining()) break;
				renderer.pointDraw(orThoedBuffer, modelViewProjection, Color.valueOf(Color.MAGENTA), 8.0f);
				break;
			
			case FindingOrthoFloor:
				if (pointForDrawingPlane2 == null) break;
				renderer.planeDraw(GLSupport.makeFloatBuffer(pointForDrawingPlane2), modelViewProjection, Color.valueOf(Color.YELLOW));
				break;
			
			case FoundOrthoFloor:
				if (!isCubeMade) break;
				if (pointForDrawingCube == null) break;
				renderer.cubeDraw(GLSupport.makeFloatBuffer(pointForDrawingCube), modelViewProjection);
				break;
			
			case DebugMode:
				if (targetedBuffer == null || !targetedBuffer.hasRemaining()) break;
//				float[] projMTX = new float[16];
//				camera.getProjectionMatrix(projMTX, 0, 0.1f, 100f);
//				float[] viewMTX = new float[16];
//				camera.getViewMatrix(viewMTX, 0);
//				float[] viewProj = new float[16];
//				Matrix.multiplyMM(viewProj, 0, projMTX, 0, viewMTX, 0);
				float[] modelViewProj = new float[4];
				float[] point = new float[]{targetedBuffer.get(0), targetedBuffer.get(1), targetedBuffer.get(2), 1.0f};
				Matrix.multiplyMV(modelViewProj, 0, modelViewProjection, 0, point, 0);
//				renderer.pointDraw(GLSupport.makeFloatBuffer(point), modelViewProjection, Color.valueOf(Color.CYAN), 20.0f);
				renderer.debugDraw(GLSupport.makeFloatBuffer(modelViewProj), modelViewProjection, Color.valueOf(Color.GREEN), 20.0f);
//				modelViewProj[0] = (modelViewProj[0] + 1) / 2.0f * width;
//				modelViewProj[1] = (modelViewProj[1] + 1) / 2.0f + height;
				System.out.println("/////////////// cvt projection 어쩌구 : " +
				                   "(" +
				                   modelViewProj[0] / modelViewProj[3] + ", " +
				                   modelViewProj[1] / modelViewProj[3] + ", " +
				                   modelViewProj[2] / modelViewProj[3] + ")");
				
				break;
			
		}
	}
	
	public void makeFunCube() {
		float[] lul = planeObject.getul();
		float[] lur = planeObject.getur();
		float[] lll = planeObject.getll();
		float[] llr = planeObject.getlr();
		
		float[] uul = new float[]{
				lul[0] + java.lang.Math.abs(plane.getNormal()[0] * boxHeight),
				lul[1] + java.lang.Math.abs(plane.getNormal()[1] * boxHeight),
				lul[2] + java.lang.Math.abs(plane.getNormal()[2] * boxHeight)
		};
		
		float[] uur = new float[]{
				lur[0] + java.lang.Math.abs(plane.getNormal()[0] * boxHeight),
				lur[1] + java.lang.Math.abs(plane.getNormal()[1] * boxHeight),
				lur[2] + java.lang.Math.abs(plane.getNormal()[2] * boxHeight)
		};
		
		float[] ull = new float[]{
				lll[0] + java.lang.Math.abs(plane.getNormal()[0] * boxHeight),
				lll[1] + java.lang.Math.abs(plane.getNormal()[1] * boxHeight),
				lll[2] + java.lang.Math.abs(plane.getNormal()[2] * boxHeight)
		};
		
		float[] ulr = new float[]{
				llr[0] + java.lang.Math.abs(plane.getNormal()[0] * boxHeight),
				llr[1] + java.lang.Math.abs(plane.getNormal()[1] * boxHeight),
				llr[2] + java.lang.Math.abs(plane.getNormal()[2] * boxHeight)
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
		
		float a = plane.getNormal()[0], b = plane.getNormal()[1], c = plane.getNormal()[2];
		float d = plane.getDVal();
		
		for (int ID : objectPoints.keySet()) {
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
		
		ArrayList<Map<Integer, float[]>> arrOfArr = new ArrayList<>();
		ArrayList<Integer> visitedID = new ArrayList<>();
		
		for (int ID : tempMap.keySet()) {
			float[] tmp = tempMap.get(ID);
			if (tmp == null) continue;
			Map<Integer, float[]> tmpMap = new HashMap<>();
			CoreSystem.makePointSet(tempMap, tmpMap, visitedID, ID, tmp);
			if (tmpMap.isEmpty()) continue;
			arrOfArr.add(tmpMap);
		}
		
		int maxSize = 0;
		Map<Integer, float[]> maxArr = null;
		for (Map<Integer, float[]> tmp : arrOfArr) {
			if (maxSize == 0) maxArr = tmp;
			if (maxSize < tmp.size()) {
				maxSize = tmp.size();
				maxArr = tmp;
			}
		}
		
		/*
		// TODO : 일단 자꾸 request fail 해서 정규분포라 가정하고 신뢰구간 이용해서 땅 찾음
		// TODO : 그 평면의 점들이 울퉁불퉁한거 없으면 Request 안된다 함
		float[] avg = CoreSystem.avgOfPointMap(tempMap);
		float avgDistance = CoreSystem.avgDistOfPointMap(tempMap, avg);
		float standardDeviation = CoreSystem.stdDeviationOfPointMap(tempMap, avg, avgDistance);
		
		HashMap<Integer, float[]> projectedPoints = new HashMap<>();
		for (int ID : tempMap.keySet()) {
			if (CoreSystem.isInReliability99(avg, tempMap.get(ID), avgDistance, standardDeviation)) {
				projectedPoints.put(ID, tempMap.get(ID));
			}
		}
		
		if (projectedPoints.isEmpty()) {
			Log.d(this.getClass().getName(), "orThoObject: something wrong with projectedPoints");
		}
		*/

//		orThoedBuffer = GLSupport.makeFloatBuffer(projectedPoints);
		orThoedBuffer = GLSupport.makeFloatBuffer(maxArr);

//		planeObject = CoreSystem.findLeastPlane(projectedPoints, avg, plane);
		planeObject = CoreSystem.findLeastPlane(maxArr, CoreSystem.avgOfPointMap(maxArr), plane);
		
		if (pointForDrawingPlane2 == null) {
			pointForDrawingPlane2 = new float[]{
					planeObject.getll()[0], planeObject.getll()[1], planeObject.getll()[2], 1.0f,
					planeObject.getlr()[0], planeObject.getlr()[1], planeObject.getlr()[2], 1.0f,
					planeObject.getur()[0], planeObject.getur()[1], planeObject.getur()[2], 1.0f,
					planeObject.getll()[0], planeObject.getll()[1], planeObject.getll()[2], 1.0f,
					planeObject.getur()[0], planeObject.getur()[1], planeObject.getur()[2], 1.0f,
					planeObject.getul()[0], planeObject.getul()[1], planeObject.getul()[2], 1.0f
			};
		}
		isGenerated = true;
	}
	
	public void deleteFloor() {
		float[] vec1 = Math.sub(plane.getlr(), plane.getll());
		float[] vec2 = Math.sub(plane.getul(), plane.getll());
		
		float[] groundNorm = Math.outer(vec1, vec2);
		Math.normalize(groundNorm);
		float groundDVal =
				-(groundNorm[0] * plane.getul()[0]
				  + groundNorm[1] * plane.getul()[1]
				  + groundNorm[2] * plane.getul()[2]);
		
		if (objectPoints == null) objectPoints = new HashMap<>();
		if (deletedPoints == null) deletedPoints = new HashMap<>();

		
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
	
	public void eraseFloor() {
	
	}
	
	public void pickToEraseFloor(float xPx, float yPx, int width, int height, Camera camera) {
		this.width = width;
		this.height = height;
		
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
	
	public FloatBuffer checkIt(Pose pose, FloatBuffer pointBuffer, float[] mvp) {
		ArrayList<float[]> ret = new ArrayList<>();
		for (int i = 0; i < pointBuffer.capacity() / 4; i++) {
			float[] temp = {pointBuffer.get(i * 4), pointBuffer.get(i * 4 + 1), pointBuffer.get(i * 4 + 2), pointBuffer.get(i * 4 + 3)};
			
			temp[3] = 1.0f;
			
			if (Math.lengthBetween(temp, new float[]{pose.tx(), pose.ty(), pose.tz()}) >= 1.5f)
				continue;
			
			float[] ndc = new float[4];
			Matrix.multiplyMV(ndc, 0, mvp, 0, temp, 0);
			ndc[0] /= ndc[3];
			ndc[1] /= ndc[3];
			if ((ndc[0] <= -0.8f || 0.8f <= ndc[0]) || (ndc[1] <= -0.8f || 0.8f <= ndc[1])) {
				continue;
			}
			
			ret.add(temp);
		}
		return GLSupport.makeFloatBuffer(ret);
	}
	
	public void push(Pose pose, float[] mvp, IntBuffer ID, FloatBuffer pointBuffer) {
		if (allPoints == null) allPoints = new HashMap<>();
		for (int i = 0; i < pointBuffer.capacity() / 4; i++) {
			float[] temp = {pointBuffer.get(i * 4), pointBuffer.get(i * 4 + 1), pointBuffer.get(i * 4 + 2), pointBuffer.get(i * 4 + 3)};
			
			//if hash map's IDth element doesn't exist, create array list
			if (temp[3] < FINE_CONFIDENCE) continue;
			
			temp[3] = 1.0f;
			
			if (Math.lengthBetween(temp, new float[]{pose.tx(), pose.ty(), pose.tz()}) >= 1.5f)
				continue;
			
			float[] ndc = new float[4];
			Matrix.multiplyMV(ndc, 0, mvp, 0, temp, 0);
			ndc[0] /= ndc[3];
			ndc[1] /= ndc[3];
			if ((ndc[0] <= -0.7f || 0.7f <= ndc[0]) || (ndc[1] <= -0.7f || 0.7f <= ndc[1])) {
				continue;
			}
			
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
	
	public void beginDebugMode() {
		mode = Bust.DebugMode;
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

	public Plane getPlaneObject() {
		return planeObject;
	}
}