package net.bfsr.engine.renderer.shader;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.renderer.shader.loader.Definition;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUseProgram;

@Getter
public class ShaderProgram extends AbstractShaderProgram {
    @Setter
    private int program;
    private final Definition[] definitions;

    protected ShaderProgram(Definition... definitions) {
        this.definitions = definitions;
    }

    @Override
    public void load() {
        ShaderManager.INSTANCE.createProgram(this);
    }

    @Override
    public void init() {
        getAllUniformLocations();
        enable();
        initUniforms();
        disable();
    }

    protected void getAllUniformLocations() {}

    protected void initUniforms() {}

    protected int getUniformLocation(String uniformName) {
        return glGetUniformLocation(program, uniformName);
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

    @Override
    public void enable() {
        glUseProgram(program);
    }

    public void disable() {
        glUseProgram(0);
    }

    @Override
    public void delete() {
        if (program != 0) {
            for (int i = 0, length = definitions.length; i < length; i++) {
                Definition definition = definitions[i];
                glDetachShader(program, definition.getShader());
            }

            glDeleteProgram(program);
        }
    }
}