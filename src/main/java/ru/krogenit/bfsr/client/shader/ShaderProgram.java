package ru.krogenit.bfsr.client.shader;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import ru.krogenit.bfsr.client.loader.ShaderLoader;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public abstract class ShaderProgram {

	private static int currentProgram;
	private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);

	protected int programId;
	protected int vertexShaderId;
	protected int fragmentShaderId;
	protected int geometryShaderId;

	public ShaderProgram() { }

	public ShaderProgram(String name) {
		int[] shader = ShaderLoader.loadShaderVF(name);
		programId = shader[0];
		vertexShaderId = shader[1];
		fragmentShaderId = shader[2];
	}

	public void initialize() {
		getAllUniformLocations();
		enable();
		init();
		disable();
	}

	protected abstract void getAllUniformLocations();

	protected abstract void init();

	protected void bindAttribute(int attribute, String variableName) {
		glBindAttribLocation(programId, attribute, variableName);
	}

	protected int getUniformLocation(String uniformName) {
		return glGetUniformLocation(programId, uniformName);
	}

	protected void setBoolean(int location, boolean value) {
		glUniform1f(location, value ? 1 : 0);
	}

	protected void setInt(int location, int value) {
		glUniform1i(location, value);
	}

	protected void setFloat(int location, float value) {
		glUniform1f(location, value);
	}

	protected void setVector(int location, Vector2f vector) {
		glUniform2f(location, vector.x, vector.y);
	}

	protected void setVector(int location, Vector3f vector) {
		glUniform3f(location, vector.x, vector.y, vector.z);
	}

	protected void setVector(int location, Vector4f vector) {
		glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
	}

	protected void setVector(int location, float x, float y, float z, float w) {
		glUniform4f(location, x, y, z, w);
	}

	protected void setVector(int location, float x, float y, float z) {
		glUniform3f(location, x, y, z);
	}

	protected void setVector(int location, float x, float y) {
		glUniform2f(location, x, y);
	}

	protected void setMat4(int location, Matrix4f matrix) {
		matrix.get(MATRIX_BUFFER);
		glUniformMatrix4fv(location, false, MATRIX_BUFFER);
	}

	public void enable() {
		if (currentProgram != programId) {
			glUseProgram(programId);
			currentProgram = programId;
		}
	}

	public void disable() {
		if (currentProgram != 0) {
			glUseProgram(0);
			currentProgram = 0;
		}
	}

	public void clear() {
		disable();
		glDetachShader(programId, vertexShaderId);
		glDetachShader(programId, fragmentShaderId);
		glDeleteShader(vertexShaderId);
		glDeleteShader(fragmentShaderId);
		glDeleteProgram(programId);
	}
}
