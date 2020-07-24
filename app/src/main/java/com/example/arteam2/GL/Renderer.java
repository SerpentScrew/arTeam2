package com.example.arteam2.GL;

import android.opengl.GLES20;

public class Renderer {
	static public void draw(Shader shader, int glModeEnum, int first, int count) {
		shader.bind();
		GLES20.glDrawArrays(glModeEnum, first, count);
		shader.unBind();
	}
	
	static public void blendDraw(Shader shader, int glModeEnum, int first, int count) {
		shader.bind();
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glDrawArrays(glModeEnum, first, count);
		shader.unBind();
	}
	
	// VBO 랑 다르게 , Texture 는 그려지기 전에 bind 되어야 할 필요가 있는듯 함!
	static public void draw(Shader shader, GLObject glObject, int glModeEnum, int first, int count) {
		shader.bind();
		glObject.bind();
		GLES20.glDrawArrays(glModeEnum, first, count);
		glObject.unBind();
		shader.unBind();
	}
}
