package com.example.arteam2.Renderer.Shader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class GLSupport {
	/**
	 * GLSL 코드들을 와일문으로 한줄 한줄 읽어들임
	 *
	 * @param context  getAsset 하기 위해 activity 를 받아옴
	 * @param filePath GLSL 코드가 담긴 파일의 주소
	 * @return 다 읽어들여 하나의 스트링으로 리턴
	 */
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
	
	/**
	 * OpenGL 을 다루며 에러가 있었는지, 어디서 에러가 생겼는지 출력해줌
	 *
	 * @param tag   로그 찍기 위한 태그
	 * @param label
	 */
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
	
	
	public static int glMakeShader(String tag, Context context, int type, String filename) {
		// 경로의 파일을 string 으로 바꿔줌
		String code = glReadFromAssets(context, filename);
		
		// Compiles shader code.
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, code);
		GLES20.glCompileShader(shader);
		
		// Get the compilation status.
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		
		// If the compilation failed, delete the shader.
		if (compileStatus[0] == 0) {
			Log.e(tag, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}
		
		if (shader == 0) {
			throw new RuntimeException("Error creating shader.");
		}
		
		return shader;
	}
}

