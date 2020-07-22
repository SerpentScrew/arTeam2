package com.example.arteam2.GL;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

public class Texture implements GLObject {
	private final String TAG = this.getClass().getName();
	
	private int textureID;
	
	public Texture() {
		GLSupport.checkError(TAG, "in Texture Constructor");
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		textureID = textures[0];
		int textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
		
		GLES20.glBindTexture(textureTarget, textureID);
		
		GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		
		GLES20.glBindTexture(textureTarget, 0);
		
		GLSupport.checkError(TAG, "in Texture Constructor");
		System.out.println("///////////////////////////// Texture constructed");
	}
	
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
	
	public int getTextureID() {
		return textureID;
	}
}
