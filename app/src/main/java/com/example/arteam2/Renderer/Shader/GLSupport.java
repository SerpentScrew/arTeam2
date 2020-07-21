package com.example.arteam2.Renderer.Shader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

//  https://developer.android.com/reference/android/opengl/GLES20

public class GLSupport {
	public static String glReadFromAssets(Context context, String filePath) {
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
	
	public static void glCheckError(String tag, String label) {
		int lastError = GLES20.GL_NO_ERROR;
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(tag, label + ": glError " + error);
			lastError = error;
		}
		if (lastError != GLES20.GL_NO_ERROR) {
			throw new RuntimeException(label + ": glError " + lastError);
		}
	}
	
	static private final int glTrue = 1, glFalse = 0;
	
	public static int glMakeShader(String tag, Context context, int type, String filename) {
		String shaderCode = glReadFromAssets(context, filename);
		
		int ID = GLES20.glCreateShader(type);
		GLES20.glShaderSource(ID, shaderCode);
		GLES20.glCompileShader(ID);
		
		int[] infoLogLen = new int[1];
		GLES20.glGetShaderiv(ID, GLES20.GL_INFO_LOG_LENGTH, infoLogLen, 0);
		
		if (infoLogLen[0] != glFalse) {
			String log = GLES20.glGetShaderInfoLog(ID);
			Log.d(tag, "glMakeShader: " + log);
		}
		
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(ID, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		
		if (compileStatus[0] != glTrue) {
			Log.e(tag, "Error compiling shader: " + GLES20.glGetShaderInfoLog(ID));
			GLES20.glDeleteShader(ID);
			ID = 0;
		}
		
		if (ID == glFalse) {
			throw new RuntimeException("Error creating shader.");
		}
		
		return ID;
	}
	
	public static int glMakeProgram(String tag, Context context, String vertPath, String fragPath) {
		int program = GLES20.glCreateProgram();
		int vertShader = glMakeShader(tag, context, GLES20.GL_VERTEX_SHADER, vertPath);
		int fragShader = glMakeShader(tag, context, GLES20.GL_FRAGMENT_SHADER, fragPath);
		
		GLES20.glAttachShader(program, vertShader);
		GLES20.glAttachShader(program, fragShader);
		
		GLES20.glLinkProgram(program);
		
		final int[] infoLogLen = new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_INFO_LOG_LENGTH, infoLogLen, 0);
		
		if (infoLogLen[0] != glFalse) {
			String info = GLES20.glGetProgramInfoLog(program);
			Log.d(tag, "glMakeProgram: " + info);
		}
		
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != glTrue) {
			GLES20.glDeleteProgram(program);
			
		}
		
		GLES20.glValidateProgram(program);

//		GLES20.glDeleteShader(vertShader);
//		GLES20.glDeleteShader(fragShader);
		
		GLES20.glUseProgram(program);
		
		return program;
	}
}

