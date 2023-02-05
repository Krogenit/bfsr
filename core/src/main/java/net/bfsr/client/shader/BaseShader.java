package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class BaseShader extends ShaderProgram {
    protected int loc_useTexture;
    protected int loc_color;
    protected int loc_modelMatrix;
    protected boolean useTexture;

    public BaseShader(Definition... definitions) {
        super(definitions);
    }

    public BaseShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "base.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "base.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_color = getUniformLocation("color");
        loc_useTexture = getUniformLocation("useTexture");
        loc_modelMatrix = getUniformLocation("modelMatrix");
    }

    @Override
    protected void initUniforms() {
        setInt(getUniformLocation("textureOpaque"), 0);
        setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void setModelMatrix(FloatBuffer matrixBuffer) {
        setMat4(loc_modelMatrix, matrixBuffer);
    }

    public void enableTexture() {
        if (!useTexture) {
            setBoolean(loc_useTexture, true);
            useTexture = true;
        }
    }

    public void disableTexture() {
        if (useTexture) {
            setBoolean(loc_useTexture, false);
            useTexture = false;
        }
    }

    public void setColor(float r, float g, float b, float a) {
        setVector(loc_color, r, g, b, a);
    }
}
