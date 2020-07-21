package com.example.arteam2.GL;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Shader {
	private final String TAG = this.getClass().getName();
	
	private final int glTrue = 1, glFalse = 0;
	private int programID;
	private Map<Integer, String> shaderCode = new HashMap<>();
	
	public Shader(Context context, String fragPath, String vertPath) {
		this.shaderCode.put(GLES20.GL_FRAGMENT_SHADER, readFromAssets(context, fragPath));
		this.shaderCode.put(GLES20.GL_VERTEX_SHADER, readFromAssets(context, vertPath));
		System.out.println("///////////////////////////// shader constructed");
	}
	
	public void bind() {
		GLSupport.check(TAG, "in Shader Bind");
		GLES20.glUseProgram(programID);
		GLSupport.check(TAG, "in Shader Bind");
	}
	
	public void unBind() {
		GLSupport.check(TAG, "in Shader Unbind");
		GLES20.glUseProgram(0);
		GLSupport.check(TAG, "in Shader Unbind");
	}
	
	String readFromAssets(Context context, String filePath) {
		StringBuilder shaderCode = new StringBuilder();
		try (Scanner scanner = new Scanner(context.getAssets().open(filePath))) {
			while (scanner.hasNextLine()) {
				shaderCode.append(scanner.nextLine());
				shaderCode.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return shaderCode.toString();
	}
	
	int makeShader(int glEnum) {
		int ID;
		GLSupport.check(TAG, "in Shader MakeShader");
		ID = GLES20.glCreateShader(glEnum);
		GLSupport.check(TAG, "in Shader MakeShader");
		GLSupport.check(TAG, "in Shader MakeShader");
		GLES20.glShaderSource(ID, shaderCode.get(glEnum));
		GLSupport.check(TAG, "in Shader MakeShader");
		GLSupport.check(TAG, "in Shader MakeShader");
		GLES20.glCompileShader(ID);
		GLSupport.check(TAG, "in Shader MakeShader");
		
		int[] infoLogLen = new int[1];
		GLSupport.check(TAG, "in Shader MakeShader");
		GLES20.glGetShaderiv(ID, GLES20.GL_INFO_LOG_LENGTH, infoLogLen, 0);
		GLSupport.check(TAG, "in Shader MakeShader");
		
		if (infoLogLen[0] != glTrue) {
			String log = GLES20.glGetShaderInfoLog(ID);
			Log.d(this.getClass().getName(), "glMakeShader: " + log);
		}
		
		final int[] compileStatus = new int[1];
		GLSupport.check(TAG, "in Shader MakeShader");
		GLES20.glGetShaderiv(ID, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		GLSupport.check(TAG, "in Shader MakeShader");
		
		if (compileStatus[0] != glTrue) {
			Log.e(this.getClass().getName(), "Error compiling shader: " + GLES20.glGetShaderInfoLog(ID));
			GLSupport.check(TAG, "in Shader MakeShader");
			GLES20.glDeleteShader(ID);
			GLSupport.check(TAG, "in Shader MakeShader");
			ID = 0;
		}
		
		if (ID == glFalse) {
			throw new RuntimeException("Error creating shader.");
		}
		
		
		return ID;
	}
	
	public Shader makeProgram() {
		programID = GLES20.glCreateProgram();
		int fragID = makeShader(GLES20.GL_FRAGMENT_SHADER);
		int vertID = makeShader(GLES20.GL_VERTEX_SHADER);
		
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLES20.glAttachShader(programID, fragID);
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLES20.glAttachShader(programID, vertID);
		GLSupport.check(TAG, "in Shader MakeProgram");
		
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLES20.glLinkProgram(programID);
		GLSupport.check(TAG, "in Shader MakeProgram");
		
		final int[] infoLogLen = new int[1];
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLES20.glGetProgramiv(programID, GLES20.GL_INFO_LOG_LENGTH, infoLogLen, 0);
		GLSupport.check(TAG, "in Shader MakeProgram");
		
		if (infoLogLen[0] != glTrue) {
			String info = GLES20.glGetProgramInfoLog(programID);
			Log.d(this.getClass().getName(), "glMakeProgram: " + info);
		}
		
		final int[] linkStatus = new int[1];
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLES20.glGetProgramiv(programID, GLES20.GL_LINK_STATUS, linkStatus, 0);
		GLSupport.check(TAG, "in Shader MakeProgram");
		if (linkStatus[0] != glTrue) {
			GLSupport.check(TAG, "in Shader MakeProgram");
			GLES20.glDeleteProgram(programID);
			GLSupport.check(TAG, "in Shader MakeProgram");
		}
		
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLES20.glValidateProgram(programID);
		GLSupport.check(TAG, "in Shader MakeProgram");
		
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLES20.glDeleteShader(vertID);
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLSupport.check(TAG, "in Shader MakeProgram");
		GLES20.glDeleteShader(fragID);
		GLSupport.check(TAG, "in Shader MakeProgram");
		
		
		return this;
	}
	
	// TODO : 이거 지우고 암튼 바꾸기
	public int getProgramID() {
		return programID;
	}
	
	public void setUniform(String target, float f1) {
		GLSupport.check(TAG, "in Shader SetUniform");
		GLES20.glUniform1f(GLES20.glGetUniformLocation(programID, target), f1);
		GLSupport.check(TAG, "in Shader SetUniform");
	}
	
	public void setUniform(String target, float f1, float f2, float f3, float f4) {
		GLSupport.check(TAG, "in Shader SetUniform");
		GLES20.glUniform4f(GLES20.glGetUniformLocation(programID, target), f1, f2, f3, f4);
		GLSupport.check(TAG, "in Shader SetUniform");
	}
	
	public void setUniform(String target, int count, boolean t, float[] value, int offset) {
		GLSupport.check(TAG, "in Shader SetUniform");
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programID, target), count, t, value, offset);
		GLSupport.check(TAG, "in Shader SetUniform");
	}
}
