/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.arteam2.Renderer;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.arteam2.GL.Renderer;
import com.example.arteam2.GL.Shader;
import com.example.arteam2.GL.VertexBuffer;
import com.google.ar.core.PointCloud;

import java.nio.FloatBuffer;

public class PointCloudRenderer {
	private static final String TAG = PointCloud.class.getSimpleName();
	
	private static final String VERTEX_SHADER_PATH = "point_cloud.vert";
	private static final String FRAGMENT_SHADER_PATH = "point_cloud.frag";
	
	private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
	private static final int FLOATS_PER_POINT = 4; // X,Y,Z,confidence.
	private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT;
	
	VertexBuffer pointCloudVBO;
	
	Shader pointCloudShader;
	
	public PointCloudRenderer() {
	}
	
	public void whenGLCreate(Context context) {
		pointCloudVBO = new VertexBuffer(GLES20.GL_DYNAMIC_DRAW);
		pointCloudShader = new Shader(context, FRAGMENT_SHADER_PATH, VERTEX_SHADER_PATH);
		pointCloudShader.makeProgram().bind();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void draw(FloatBuffer floatBuffer, float[] modelViewProjection, Color color) {
		pointCloudVBO.fillData(floatBuffer);
		pointCloudShader.setAttrib(pointCloudVBO, "a_Position", 4, GLES20.GL_FLOAT, false, BYTES_PER_POINT, 0);
		pointCloudShader.setUniform("u_Color", color.red(), color.green(), color.blue(), color.alpha());
		pointCloudShader.setUniform("u_ModelViewProjection", 1, false, modelViewProjection, 0);
		pointCloudShader.setUniform("u_PointSize", 10.0f);
		Renderer.draw(pointCloudShader, GLES20.GL_POINTS, 0, floatBuffer.remaining() / FLOATS_PER_POINT);
		pointCloudShader.freeAtrib(pointCloudVBO, "a_Position");
	}
}
