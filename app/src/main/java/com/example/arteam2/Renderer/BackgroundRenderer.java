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
import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.example.arteam2.GL.GLSupport;
import com.example.arteam2.GL.Renderer;
import com.example.arteam2.GL.Shader;
import com.example.arteam2.GL.Texture;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;

import java.nio.FloatBuffer;

public class BackgroundRenderer {
	private static final String TAG = BackgroundRenderer.class.getSimpleName();
	
	private static final String VERTEX_SHADER_PATH = "screenquad.vert";
	private static final String FRAGMENT_SHADER_PATH = "screenquad.frag";
	
	private static final int COORDS_PER_VERTEX = 2;
	private static final int TEXCOORDS_PER_VERTEX = 2;
	private static final int FLOAT_SIZE = 4;
	
	private static final float[] QUAD_COORDS =
			new float[]{
					-1.0f, -1.0f,
					+1.0f, -1.0f,
					-1.0f, +1.0f,
					+1.0f, +1.0f,
			};
	
	private FloatBuffer quadCoords;
	private FloatBuffer quadTexCoords;
	
	Shader backGroundShader;
	Texture texture;
	
	private boolean suppressTimestampZeroRendering = true;
	
	public int getTextureId() {
		return texture.getTextureID();
	}
	
	public void whenGLCreate(Context context) {
		texture = new Texture();
		
		int numVertices = 4;
		if (numVertices != QUAD_COORDS.length / COORDS_PER_VERTEX) {
			throw new RuntimeException("Unexpected number of vertices in BackgroundRenderer.");
		}
		
		quadCoords = GLSupport.makeFloatBuffer(QUAD_COORDS);
		
		quadTexCoords = GLSupport.makeFloatBuffer(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE);
		
		backGroundShader = new Shader(context, FRAGMENT_SHADER_PATH, VERTEX_SHADER_PATH);
		backGroundShader.makeProgram().bind();
	}
	
	public void suppressTimestampZeroRendering(boolean suppressTimestampZeroRendering) {
		this.suppressTimestampZeroRendering = suppressTimestampZeroRendering;
	}
	
	public void draw(@NonNull Frame frame) {
		// If display rotation changed (also includes view size change), we need to re-query the uv
		// coordinates for the screen rect, as they may have changed as well.
		if (frame.hasDisplayGeometryChanged()) {
			frame.transformCoordinates2d(
					Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
					quadCoords,
					Coordinates2d.TEXTURE_NORMALIZED,
					quadTexCoords);
		}
		
		if (frame.getTimestamp() == 0 && suppressTimestampZeroRendering) {
			// Suppress rendering if the camera did not produce the first frame yet. This is to avoid
			// drawing possible leftover data from previous sessions if the texture is reused.
			return;
		}
		
		draw();
	}
	
	public void draw(
			int imageWidth, int imageHeight, float screenAspectRatio, int cameraToDisplayRotation) {
		// Crop the camera image to fit the screen aspect ratio.
		float imageAspectRatio = (float) imageWidth / imageHeight;
		float croppedWidth;
		float croppedHeight;
		if (screenAspectRatio < imageAspectRatio) {
			croppedWidth = imageHeight * screenAspectRatio;
			croppedHeight = imageHeight;
		} else {
			croppedWidth = imageWidth;
			croppedHeight = imageWidth / screenAspectRatio;
		}
		
		float u = (imageWidth - croppedWidth) / imageWidth * 0.5f;
		float v = (imageHeight - croppedHeight) / imageHeight * 0.5f;
		
		float[] texCoordTransformed;
		switch (cameraToDisplayRotation) {
			case 90:
				texCoordTransformed = new float[]{1 - u, 1 - v, 1 - u, v, u, 1 - v, u, v};
				break;
			case 180:
				texCoordTransformed = new float[]{1 - u, v, u, v, 1 - u, 1 - v, u, 1 - v};
				break;
			case 270:
				texCoordTransformed = new float[]{u, v, u, 1 - v, 1 - u, v, 1 - u, 1 - v};
				break;
			case 0:
				texCoordTransformed = new float[]{u, 1 - v, 1 - u, 1 - v, u, v, 1 - u, v};
				break;
			default:
				throw new IllegalArgumentException("Unhandled rotation: " + cameraToDisplayRotation);
		}
		
		// Write image texture coordinates.
		quadTexCoords.position(0);
		quadTexCoords.put(texCoordTransformed);
		
		draw();
	}
	
	
	private void draw() {
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthMask(false);
		
		backGroundShader.setAttrib(texture, "a_Position", COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadCoords);
		backGroundShader.setAttrib(texture, "a_TexCoord", TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoords);
		
		Renderer.draw(backGroundShader, texture, GLES20.GL_TRIANGLE_STRIP, 0, 4);
		
		backGroundShader.freeAtrib(texture, "a_Position");
		backGroundShader.freeAtrib(texture, "a_TexCoord");
		
		GLES20.glDepthMask(true);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}
}
