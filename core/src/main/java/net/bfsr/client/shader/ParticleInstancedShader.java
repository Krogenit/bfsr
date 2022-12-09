package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

public class ParticleInstancedShader extends ShaderProgram {
    private int loc_useTexture;
    private int loc_textureOpaque;
    private int loc_orthoMat;
    private int loc_modelViewMat;

    private boolean useTexture;

    private int loc_animatedTexture;
    private int loc_numberOfRows;

    public ParticleInstancedShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "particleInstanced.vert.glsl"),
                new Definition(GL20.GL_FRAGMENT_SHADER, "particleInstanced.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_useTexture = getUniformLocation("useTexture");
        loc_textureOpaque = getUniformLocation("textureOpaque");

        loc_orthoMat = getUniformLocation("orthoMat");
        loc_modelViewMat = getUniformLocation("modelViewMat");

        loc_animatedTexture = getUniformLocation("animatedTexture");
        loc_numberOfRows = getUniformLocation("numberOfRows");
    }

    @Override
    protected void initUniforms() {
        setTextureOpaqueId(0);
    }

    public void setNumberOfRows(float value) {
        setFloat(loc_numberOfRows, value);
    }

    private boolean prevAnimatedTexture;

    public void setAnimatedTexture(boolean value) {
        if (prevAnimatedTexture != value) {
            setBoolean(loc_animatedTexture, value);
            prevAnimatedTexture = value;
        }
    }

    public void setOrthoMatrix(Matrix4f matrix) {
        setMat4(loc_orthoMat, matrix);
    }

    public void setModelViewMatrix(Matrix4f matrix) {
        setMat4(loc_modelViewMat, matrix);
    }

    public void setTextureOpaqueId(int id) {
        setInt(loc_textureOpaque, id);
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
}
