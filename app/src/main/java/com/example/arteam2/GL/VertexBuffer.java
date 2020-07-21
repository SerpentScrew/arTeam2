package com.example.arteam2.GL;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class VertexBuffer {
	private static final int BYTES_PER_FLOAT = Float.SIZE / 8; // float 는 몇바이트인지
	private static final int FLOATS_PER_POINT = 4; // 한 점에 몇개의 수가 들어가는지
	private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT; // 한 점에 몇 바이트가 들어가는지
	private static final int INITIAL_BUFFER_POINTS = 1000; // ?
	private final String TAG = this.getClass().getName();
	
	private int vboID;
	private int vboSize = INITIAL_BUFFER_POINTS * BYTES_PER_POINT;
	private int glDrawWayEnum;
	private int byteOfOneVertex;
	private String vertexType;
	
	
	public VertexBuffer(int glDrawWayEnum) {
		GLSupport.check(TAG, "in VertexBuffer Constructor");
		this.glDrawWayEnum = glDrawWayEnum;
		
		int[] buffers = new int[1];
		GLES20.glGenBuffers(1, buffers, 0);
		vboID = buffers[0];
		this.bind();
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vboSize, null, glDrawWayEnum);
		this.unBind();
		System.out.println("////////////////// vb constructed");
		GLSupport.check(TAG, "in VertexBuffer Constructor");
	}
	
	public void bind() {
		GLSupport.check(TAG, "in VertexBuffer Bind");
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboID);
		GLSupport.check(TAG, "in VertexBuffer Bind");
	}
	
	public void unBind() {
		GLSupport.check(TAG, "in VertexBuffer Unbind");
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLSupport.check(TAG, "in VertexBuffer Unbind");
	}
	
	public int getVboSize() {
		return vboSize;
	}
	
	public void reSize(int numPoints) {
		GLSupport.check(TAG, "in VertexBuffer Resize");
		if (numPoints * BYTES_PER_POINT > vboSize) {
			while (numPoints * BYTES_PER_POINT > vboSize) {
				vboSize *= 2;
			}
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vboSize, null, glDrawWayEnum);
		}
		GLSupport.check(TAG, "in VertexBuffer Resize");
	}
	
	public void fillData(FloatBuffer floatBuffer) {
		GLSupport.check(TAG, "in VertexBuffer FillData");
		this.bind();
		int numPoints = floatBuffer.remaining() / FLOATS_PER_POINT;
		this.reSize(numPoints);
		GLES20.glBufferSubData(
				GLES20.GL_ARRAY_BUFFER, 0, numPoints * BYTES_PER_POINT, floatBuffer
		                      );
		GLSupport.check(TAG, "in VertexBuffer FillData");
		this.unBind();
	}
	
	public void setAttrib(int progID, String string, int size, int type, boolean normalized, int stride, int offset) {
		GLSupport.check(TAG, "in VertexBuffer SetAttrib");
		int pos = GLES20.glGetAttribLocation(progID, string);
		GLES20.glEnableVertexAttribArray(pos);
		this.bind();
		GLES20.glVertexAttribPointer(pos, size, type, normalized, stride, offset);
		GLSupport.check(TAG, "in VertexBuffer SetAttrib");
//		this.unBind();
	}
	
	public void freeAtrib(int progID, String string) {
		GLSupport.check(TAG, "in VertexBuffer FreeAttrib");
		this.bind();
		int pos = GLES20.glGetAttribLocation(progID, string);
		GLES20.glDisableVertexAttribArray(pos);
		GLSupport.check(TAG, "in VertexBuffer FreeAttrib");
		this.unBind();
	}
}
