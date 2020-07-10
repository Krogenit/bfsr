package ru.krogenit.bfsr.client.shader;

import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import ru.krogenit.bfsr.client.loader.ShaderLoader;

public abstract class ShaderGeometryProgram extends ShaderProgram {

	public ShaderGeometryProgram(String name) {
		int[] shader = ShaderLoader.loadShaderVFG(name);
		programId = shader[0];
		vertexShaderId = shader[1];
		fragmentShaderId = shader[2];
		geometryShaderId = shader[3];
	}

	@Override
	public void clear() {
		disable();
		glDetachShader(programId, vertexShaderId);
		glDetachShader(programId, fragmentShaderId);
		glDetachShader(programId, geometryShaderId);
		glDeleteShader(vertexShaderId);
		glDeleteShader(fragmentShaderId);
		glDeleteShader(geometryShaderId);
		glDeleteProgram(programId);
	}
}
