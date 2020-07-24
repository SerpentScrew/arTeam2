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
	
	static public class FindSurface extends AsyncTask<Object, ResponseForm.PlaneParam, ResponseForm.PlaneParam> {
		private static final String REQUEST_URL = "https://developers.curvsurf.com/FindSurface/plane"; // Plane searching server address
		private PointHandler pointHandler = null;
		private Camera camera = null;
		private float circleRad = 0.25f;
		private float z_dis = 0;
		
		public FindSurface(PointHandler pointHandler, Camera camera) {
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
			rf.setTargetROI(pointHandler.getSeedPointID(), java.lang.Math.max(z_dis * circleRad, 0.1f));//seedIndex,touchRadius
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
				pointHandler.updatePlaneVertex(plane.getPlaneVertex());
				float[] points = new float[]{
						o.ll[0], o.ll[1], o.ll[2], 1.0f,
						o.lr[0], o.lr[1], o.lr[2], 1.0f,
						o.ur[0], o.ur[1], o.ur[2], 1.0f,
						o.ll[0], o.ll[1], o.ll[2], 1.0f,
						o.ur[0], o.ur[1], o.ur[2], 1.0f,
						o.ul[0], o.ul[1], o.ul[2], 1.0f
				};
				pointHandler.setFloor4Points(points);
				System.out.println(plane);
				pointHandler.findFloorEnd();
			} catch (Exception e) {
				Log.d("Plane", e.getMessage());
			}
		}
	}

//	private void findPlane(float[] ul, float[] ur, float[] ll, float[] lr) {
//		float[] vec1 = VectorCal.sub(lr, ll);
//		float[] vec2 = VectorCal.sub(ul, ll);
//
//		float[] normalVector = VectorCal.outer(vec1, vec2);
//		VectorCal.normalize(normalVector);
//
//		HashMap<Integer, float[]> tmp;
//		tmp = pointCloudRenderer.filteredPoints;
//		for (int id : tmp.keySet()) {
//			float[] point = tmp.get(id);
//			float a = VectorCal.inner(normalVector, point);
//
//			if (a < -1 - epsilon || a > -1 + epsilon) {
//				pointCloudRenderer.objectPoints.put(id, point);
//			}
//
//		}
//	}
}

