package com.example.arteam2.GL;

import android.opengl.GLES20;

public class Renderer {
	static public void draw(Shader shader, int glModeEnum, int first, int count) {
		shader.bind();
		GLES20.glDrawArrays(glModeEnum, first, count);
		shader.unBind();
	}
}
