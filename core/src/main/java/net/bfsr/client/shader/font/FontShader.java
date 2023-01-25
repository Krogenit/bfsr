package net.bfsr.client.shader.font;

import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.client.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class FontShader extends ShaderProgram {
    private int modelMatrix;

    public FontShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "font/font.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "font/font.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        modelMatrix = getUniformLocation("modelMatrix");
    }

    @Override
    protected void initUniforms() {}

    public void setModelMatrix(FloatBuffer matrixBuffer) {
        setMat4(modelMatrix, matrixBuffer);
    }
}
