package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;

public class BaseShader extends Program {
    protected int loc_useTexture;
    protected int loc_textureOpaque;
    protected int loc_color;
    protected int loc_orthoMat;
    protected int loc_modelViewMat;
    private int loc_uv_scale;
    private int loc_uv_offset;
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
        loc_textureOpaque = getUniformLocation("textureOpaque");

        loc_orthoMat = getUniformLocation("orthoMat");
        loc_modelViewMat = getUniformLocation("modelViewMat");

        loc_uv_scale = getUniformLocation("uv_scale");
        loc_uv_offset = getUniformLocation("uv_offset");
    }

    @Override
    protected void initUniforms() {
        setTextureOpaqueId(0);
        setColor(new Vector4f(1, 1, 1, 1));
        setUVScale(1.0f, 1.0f);
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

    public void setColor(Vector4f color) {
        setVector(loc_color, color);
    }

    public void setUVScale(float x, float y) {
        setVector(loc_uv_scale, x, y);
    }

    public void setUVOffset(float x, float y) {
        setVector(loc_uv_offset, x, y);
    }
}
