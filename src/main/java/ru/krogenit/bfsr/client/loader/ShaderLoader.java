package ru.krogenit.bfsr.client.loader;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glValidateProgram;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import ru.krogenit.bfsr.util.IOUtil;

public class ShaderLoader {

	public static int[] loadShaderVF(String name) {
		int[] shader = new int[3];
		int program = glCreateProgram();
		int vertexProgram = glCreateShader(GL_VERTEX_SHADER);
		int fragmentProgram = glCreateShader(GL_FRAGMENT_SHADER);

		StringBuilder vertexShaderSource = IOUtil.loadShaderSrc(name + ".vs");
		StringBuilder fragmentShaderSource = IOUtil.loadShaderSrc(name + ".fs");

		compileShader(name + ".vs", vertexProgram, vertexShaderSource);
		compileShader(name + ".fs", fragmentProgram, fragmentShaderSource);

		shader[0] = program;
		shader[1] = vertexProgram;
		shader[2] = fragmentProgram;

		glAttachShader(program, vertexProgram);
		glAttachShader(program, fragmentProgram);

		linkShader(name, program);

		return shader;
	}

	public static int[] loadShaderVFG(String name) {
		int[] shader = new int[4];
		int program = glCreateProgram();
		int vertexProgram = glCreateShader(GL_VERTEX_SHADER);
		int fragmentProgram = glCreateShader(GL_FRAGMENT_SHADER);
		int geometryProgram = glCreateShader(GL_GEOMETRY_SHADER);

		StringBuilder vertexShaderSource = IOUtil.loadShaderSrc(name + ".vs");
		StringBuilder fragmentShaderSource = IOUtil.loadShaderSrc(name + ".fs");
		StringBuilder geometryShaderSource = IOUtil.loadShaderSrc(name + ".gs");

		compileShader(name + ".vs", vertexProgram, vertexShaderSource);
		compileShader(name + ".fs", fragmentProgram, fragmentShaderSource);
		compileShader(name + ".gs", geometryProgram, geometryShaderSource);

		shader[0] = program;
		shader[1] = vertexProgram;
		shader[2] = fragmentProgram;
		shader[3] = geometryProgram;

		glAttachShader(program, vertexProgram);
		glAttachShader(program, fragmentProgram);
		glAttachShader(program, geometryProgram);

		linkShader(name, program);

		return shader;
	}

	private static void linkShader(String name, int program) {
		glLinkProgram(program);
		if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
			String s = glGetProgramInfoLog(program, 500);
			System.err.println(name + " shader link error: " + s);
			System.exit(0);
		}

		glValidateProgram(program);
		if (glGetProgrami(program, GL_VALIDATE_STATUS) == GL_FALSE) {
			String s = glGetProgramInfoLog(program, 500);
			System.err.println(name + " shader validating error: " + s);
			System.exit(0);
		}
	}

	private static void compileShader(String name, int program, StringBuilder src) {
		glShaderSource(program, src);
		glCompileShader(program);

		if (glGetShaderi(program, GL_COMPILE_STATUS) == GL_FALSE) {
			String s = glGetShaderInfoLog(program, 500);
			System.err.println(name + " shader compile error: " + s);
			System.exit(0);
		}
	}
}
