package com.example.arteam2.GL;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

public class Texture implements GLObject {
	private final String TAG = this.getClass().getName();
	
	private int textureID;
	
	@Override
	public void bind() {
		GLSupport.checkError(TAG, "in Texture Bind");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID);
		GLSupport.checkError(TAG, "in Texture Bind");
	}
	
	@Override
	public void unBind() {
		GLSupport.checkError(TAG, "in Texture UnBind");
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
		GLSupport.checkError(TAG, "in Texture UnBind");
	}
}
