package net.bfsr.client.shader;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.shader.loader.Definition;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

@Getter
public abstract class ShaderProgram {
    public static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);

    @Setter
    private int program;
    private final Definition[] definitions;

    protected ShaderProgram(Definition... definitions) {
        this.definitions = definitions;
    }

    public void load() {
        ProgramManager.INSTANCE.createProgram(this);
    }

    @Deprecated
    public void init() {
        getAllUniformLocations();
        enable();
        initUniforms();
        disable();
    }

    @Deprecated
    protected abstract void getAllUniformLocations();
    @Deprecated
    protected abstract void initUniforms();

    protected int getUniformLocation(String uniformName) {
        return GL20.glGetUniformLocation(program, uniformName);
    }

    protected void setBoolean(int location, boolean value) {
        GL20.glUniform1f(location, value ? 1 : 0);
    }

    protected void setInt(int location, int value) {
        GL20.glUniform1i(location, value);
    }

    protected void setFloat(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    protected void setVector(int location, Vector2f vector) {
        GL20.glUniform2f(location, vector.x, vector.y);
    }

    protected void setVector(int location, Vector3f vector) {
        GL20.glUniform3f(location, vector.x, vector.y, vector.z);
    }

    protected void setVector(int location, Vector4f vector) {
        GL20.glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
    }

    protected void setVector(int location, float x, float y, float z, float w) {
        GL20.glUniform4f(location, x, y, z, w);
    }

    protected void setVector(int location, float x, float y, float z) {
        GL20.glUniform3f(location, x, y, z);
    }

    protected void setVector(int location, float x, float y) {
        GL20.glUniform2f(location, x, y);
    }

    protected void setMat4(int location, Matrix4f matrix) {
        GL20.glUniformMatrix4fv(location, false, matrix.get(MATRIX_BUFFER));
    }

    protected void setMat4(int location, FloatBuffer matrixBuffer) {
        GL20.glUniformMatrix4fv(location, false, matrixBuffer);
    }

    public void enable() {
        GL20.glUseProgram(program);
    }

    public void disable() {
        GL20.glUseProgram(0);
    }

    public void delete() {
        int length = definitions.length;
        for (int i = 0; i < length; i++) {
            Definition definition = definitions[i];
            GL20.glDetachShader(program, definition.getShader());
        }
        GL20.glDeleteProgram(program);
    }
}
