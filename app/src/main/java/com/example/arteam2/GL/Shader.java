package com.example.arteam2.GL;

public class Shader {
	private int programID;
	private int fragID, vertID;
	private String fragPath, vertPath;
	
	Shader(String fragPath, String vertPath) {
		this.fragPath = fragPath;
		this.vertPath = vertPath;
	}
	
}
