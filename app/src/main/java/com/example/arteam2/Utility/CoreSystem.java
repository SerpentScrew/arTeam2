package com.example.arteam2.Utility;

import android.opengl.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.example.arteam2.R;
import com.google.ar.core.Camera;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Objects;

public class CoreSystem {
	static public float[] screenPointToWorldRay(float xPx, float yPx, int width, int height, Camera camera) {
		//xPx : 터치한 x좌표, yPx : 터치한 y 좌표
		// ray[0~2] : camera pose
		// ray[3~5] : Unit vector of ray
		float[] ray_clip = new float[4];
		ray_clip[0] = 2.0f * xPx / width - 1.0f;
		// +y is up (android UI Y is down):
		ray_clip[1] = 1.0f - 2.0f * yPx / height;
		ray_clip[2] = -1.0f; // +z is forwards (remember clip, not camera) 클립이 뭔 뜻일까
		ray_clip[3] = 1.0f; // w (homogenous coordinates)
		//위치 조절 하는 건가봐
		
		float[] ProMatrices = new float[32];  // {proj, inverse proj}
		camera.getProjectionMatrix(ProMatrices, 0, 0.1f, 100.0f);
		Matrix.invertM(ProMatrices, 16, ProMatrices, 0);
		float[] ray_eye = new float[4];
		Matrix.multiplyMV(ray_eye, 0, ProMatrices, 16, ray_clip, 0);
		
		ray_eye[2] = -1.0f;
		ray_eye[3] = 0.0f;
		
		float[] out = new float[6];
		float[] ray_wor = new float[4];
		float[] ViewMatrices = new float[32];
		
		camera.getViewMatrix(ViewMatrices, 0);
		Matrix.invertM(ViewMatrices, 16, ViewMatrices, 0);
		Matrix.multiplyMV(ray_wor, 0, ViewMatrices, 16, ray_eye, 0);
		
		float size = (float) java.lang.Math.sqrt(ray_wor[0] * ray_wor[0] + ray_wor[1] * ray_wor[1] + ray_wor[2] * ray_wor[2]);
		
		out[3] = ray_wor[0] / size;
		out[4] = ray_wor[1] / size;
		out[5] = ray_wor[2] / size;
		
		out[0] = camera.getPose().tx();
		out[1] = camera.getPose().ty();
		out[2] = camera.getPose().tz();
		
		return out;
	}
	
	// TODO : 이거 아래 두 AsyncTask 합치는 방법 생각..
	
	static public class FindFloor extends AsyncTask<Object, ResponseForm.PlaneParam, ResponseForm.PlaneParam> {
		private static final String REQUEST_URL = "https://developers.curvsurf.com/FindSurface/plane"; // Plane searching server address
		private PointHandler pointHandler = null;
		private Camera camera = null;
		private float circleRad = 0.25f;
		private float z_dis = 0;
		
		public FindFloor(PointHandler pointHandler, Camera camera) {
			this.pointHandler = pointHandler;
			this.camera = camera;
			System.out.println("Findsurface constructed. : " + pointHandler + camera);
		}
		
		@Override
		protected ResponseForm.PlaneParam doInBackground(Object[] objects) {
			System.out.println("doinbackgrounds~~");
			
			// Ready Point Cloud
			FloatBuffer points = pointHandler.getFilteredBuffer().duplicate();
			
			// Ready Request Form
			RequestForm rf = new RequestForm();
			
			rf.setPointBufferDescription(points.capacity() / 4, 16, 0); //pointcount, pointstride, pointoffset
			rf.setPointDataDescription(0.05f, 0.01f); //accuracy, meanDistance
			rf.setTargetROI(pointHandler.getSeedPointID(), java.lang.Math.max(z_dis * circleRad, 0.05f));//seedIndex,touchRadius
			rf.setAlgorithmParameter(RequestForm.SearchLevel.NORMAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp
			Log.d("PointsBuffer", points.toString());
			FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);
			// Request Find Surface
			try {
				Log.d("PlaneFinder", "request");
				ResponseForm resp = fsr.request(rf, points);
				if (resp != null && resp.isSuccess()) {
					ResponseForm.PlaneParam param = resp.getParamAsPlane();
					Log.d("PlaneFinder", "request success");
					return param;
				} else {
					Log.d("PlaneFinder", "request fail");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(ResponseForm.PlaneParam o) {
			super.onPostExecute(o);
			System.out.println("onpostexecute ~~");
			if (o == null) {
				Log.d(this.getClass().getName(), "onPostExecute: 평면 추출 실패!!");
				pointHandler.setCollectDoneMsg(R.string.press_ground_findSurfFailed);
				pointHandler.collectingEnd();
				return;
			}
			try {
				Plane plane = new Plane(o.ll, o.lr, o.ur, o.ul, camera.getPose().getZAxis());
				pointHandler.setPlane(plane);
//				System.out.println(plane);
				pointHandler.findFloorEnd();
			} catch (Exception e) {
				Log.d("Plane", e.getMessage());
			}
		}
	}
	
	static public float[] avgOfPointMap(Map<Integer, float[]> map) {
		if (map == null) return null;
		
		float[] avg = new float[]{0.0f, 0.0f, 0.0f};
		
		int size = 0;
		for (int ID : map.keySet()) {
			if (map.get(ID) == null) continue;
			
			avg[0] += Objects.requireNonNull(map.get(ID))[0];
			avg[1] += Objects.requireNonNull(map.get(ID))[1];
			avg[2] += Objects.requireNonNull(map.get(ID))[2];
			size++;
		}
		
		avg[0] /= size;
		avg[1] /= size;
		avg[2] /= size;
		
		return avg;
	}
	
	static public float avgDistOfPointMap(Map<Integer, float[]> map, float[] avg) {
		if (map == null || avg == null) return Float.MIN_VALUE;
		
		float avgDistance = 0.0f;
		int size = 0;
		for (int ID : map.keySet()) {
			if (map.get(ID) == null) break;
			
			float length = Math.lengthBetween(avg, Objects.requireNonNull(map.get(ID)));
			avgDistance += length;
			size++;
		}
		
		avgDistance /= size;
		
		return avgDistance;
	}
	
	static public float stdDeviationOfPointMap(Map<Integer, float[]> map, float[] avg, float avgDistance) {
		if (map == null || avg == null) return Float.MIN_VALUE;
		
		double cubeAvg = 0.0f;
		int size = 0;
		for (int ID : map.keySet()) {
			if (map.get(ID) == null) break;
			
			float length = Math.lengthBetween(avg, Objects.requireNonNull(map.get(ID)));
			cubeAvg += java.lang.Math.pow(length, 2);
			size++;
		}
		
		cubeAvg /= size;
		double standardDeviation = java.lang.Math.sqrt(cubeAvg - java.lang.Math.pow(avgDistance, 2));
		
		return (float) standardDeviation;
	}
	
	static public boolean isInReliability99(float[] avg, float[] a, float avgDistance, float stdDeviation) {
		return ((avgDistance - (2.58 * stdDeviation)) <= Math.lengthBetween(avg, a))
		       &&
		       (Math.lengthBetween(avg, a) <= (avgDistance + (2.58 * stdDeviation)));
	}
	
	static public float[] farthestPointFromAvg(Map<Integer, float[]> map, float[] avg) {
		if (map == null || avg == null) return null;
		
		float[] max = new float[]{0.0f, 0.0f, 0.0f};
		double curMaxLen = 0.0f;
		
		for (int ID : map.keySet()) {
			if (map.get(ID) == null) continue;
			
			double len = Math.lengthBetween(avg, Objects.requireNonNull(map.get(ID)));
			
			if (curMaxLen < len) {
				curMaxLen = len;
				max[0] = Objects.requireNonNull(map.get(ID))[0];
				max[1] = Objects.requireNonNull(map.get(ID))[1];
				max[2] = Objects.requireNonNull(map.get(ID))[2];
			}
		}
		
		return max;
	}
	
	static public Plane findLeastPlane(Map<Integer, float[]> map, float[] avg, Plane plane) {
		
		if (map == null || avg == null) return null;
		
		float[] farthestPoint = CoreSystem.farthestPointFromAvg(map, avg);
		float[] ur = new float[]{
				farthestPoint[0], farthestPoint[1], farthestPoint[2]
		};
		float[] ll = new float[]{
				(2 * avg[0]) - ur[0],
				(2 * avg[1]) - ur[1],
				(2 * avg[2]) - ur[2]
		};
		
		float[] lineVector = new float[]{
				ll[0] - ur[0],
				ll[1] - ur[1],
				ll[2] - ur[2]
		};
		
		float[] ul = null, lr = null;
		float[] certainPoint = null;
		
		float curMaxLen = 0;
		
		for (int ID : map.keySet()) {
			if (map.get(ID) == null) continue;
			
			float[] pointPointVector = new float[]{
					Objects.requireNonNull(map.get(ID))[0] - ur[0],
					Objects.requireNonNull(map.get(ID))[1] - ur[1],
					Objects.requireNonNull(map.get(ID))[2] - ur[2]
			};
			
			float tmp =
					Math.vectorSize(Math.outer(lineVector, pointPointVector))
					/ Math.vectorSize(lineVector);
			
			if (curMaxLen < tmp) {
				curMaxLen = tmp;
				if (certainPoint == null) certainPoint = new float[3];
				certainPoint[0] = Objects.requireNonNull(map.get(ID))[0];
				certainPoint[1] = Objects.requireNonNull(map.get(ID))[1];
				certainPoint[2] = Objects.requireNonNull(map.get(ID))[2];
			}
		}
		
		if (certainPoint == null) return null;
		
		float radius = Math.vectorSize(Math.sub(ur, avg));
		
		float[] tmp = new float[]{
				certainPoint[0] - avg[0],
				certainPoint[1] - avg[1],
				certainPoint[2] - avg[2],
		};
		
		certainPoint[0] = avg[0] + (tmp[0] * radius / Math.vectorSize(tmp));
		certainPoint[1] = avg[1] + (tmp[1] * radius / Math.vectorSize(tmp));
		certainPoint[2] = avg[2] + (tmp[2] * radius / Math.vectorSize(tmp));
		
		float[] oppositeOfCertainPoint = {
				(2 * avg[0]) - certainPoint[0],
				(2 * avg[1]) - certainPoint[1],
				(2 * avg[2]) - certainPoint[2]
		};
		
		if (plane.isInputUL(ll, ur, certainPoint)) {
			ul = certainPoint;
			lr = oppositeOfCertainPoint;
		} else {
			ul = oppositeOfCertainPoint;
			lr = certainPoint;
		}
		
		return new Plane(ll, lr, ur, ul);
	}
}

