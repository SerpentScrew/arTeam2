package com.example.arteam2.GL;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class VertexBuffer {
	private int vboID;
	
	VertexBuffer(int size, int glEnum) {
		int[] buffers = new int[1];
		GLES20.glGenBuffers(1, buffers, 0);
		vboID = buffers[0];
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboID);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, size, null, glEnum);
	}
	
	public void bind() {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboID);
	}
	
	public void unBind() {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	public void fillData(FloatBuffer floatBuffer) {
		this.bind();
		// TODO : size (현재 9999999) 변수로바꿔주기
		GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, 9999999, floatBuffer);
		this.unBind();
	}
}
