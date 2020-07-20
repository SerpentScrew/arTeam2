package com.example.arteam2.Renderer.Shader;

public class GLSL {
	static class Plane {
		//language=GLSL
		static String frag =
				"precision highp float;\n" +
				"\n" +
				"varying vec4 color;\n" +
				"\n" +
				"void main() {\n" +
				"    gl_FragColor = color;\n" +
				"}";
		
		//language=GLSL
		static String vert =
				"uniform mat4 u_ModelViewProjection;\n" +
				"attribute vec3 a_XZPositionAlpha;\n" +
				"varying vec4 color;\n" +
				"\n" +
				"void main() {\n" +
				"    vec4 local_pos = vec4(a_XZPositionAlpha.x, 0.0, a_XZPositionAlpha.y, 1.0);\n" +
				"    gl_Position = u_ModelViewProjection * local_pos;\n" +
				"\n" +
				"    color = vec4(0, 0.807843, 0.819608, 0.9);\n" +
				"}";
	}
	
	static class PointCloud {
		//language=GLSL
		static String frag =
				"precision mediump float;\n" +
				"varying vec4 v_Color;\n" +
				"\n" +
				"void main() {\n" +
				"    gl_FragColor = v_Color;\n" +
				"}";
		
		//language=GLSL
		static String vert =
				"uniform mat4 u_ModelViewProjection;\n" +
				"uniform vec4 u_Color;\n" +
				"uniform float u_PointSize;\n" +
				"\n" +
				"attribute vec4 a_Position;\n" +
				"\n" +
				"varying vec4 v_Color;\n" +
				"\n" +
				"void main() {\n" +
				"    v_Color = u_Color;\n" +
				"    gl_Position = u_ModelViewProjection * vec4(a_Position.xyz, 1.0);\n" +
				"    gl_PointSize = u_PointSize;\n" +
				"}";
	}
	
	static class Background {
		//language=GLSL
		static String frag =
				"#extension GL_OES_EGL_image_external : require\n" +
				"\n" +
				"precision mediump float;\n" +
				"varying vec2 v_TexCoord;\n" +
				"uniform samplerExternalOES sTexture;\n" +
				"\n" +
				"\n" +
				"void main() {\n" +
				"    gl_FragColor = texture2D(sTexture, v_TexCoord);\n" +
				"}";
		
		//language=GLSL
		static String vert =
				"attribute vec4 a_Position;\n" +
				"attribute vec2 a_TexCoord;\n" +
				"\n" +
				"varying vec2 v_TexCoord;\n" +
				"\n" +
				"void main() {\n" +
				"    gl_Position = a_Position;\n" +
				"    v_TexCoord = a_TexCoord;\n" +
				"}";
	}
}

