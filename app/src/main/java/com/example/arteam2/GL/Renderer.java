package com.example.arteam2.GL;

import android.opengl.GLES20;

public class Renderer {
	static public void draw(Shader shader, VertexBuffer vbo, int glEnum) {
		shader.bind();
		vbo.bind();
		GLES20.glDrawArrays(glEnum, 0, vbo.getVboSize());
	}
}
