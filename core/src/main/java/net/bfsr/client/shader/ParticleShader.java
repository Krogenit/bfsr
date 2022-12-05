package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;

public class ParticleShader extends BaseShader {
    private int loc_animatedTexture;
    private int loc_texCoordInfo;
    private int loc_texOffset;

    public ParticleShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "particle.vert.glsl"),
                new Definition(GL20.GL_FRAGMENT_SHADER, "particle.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        super.getAllUniformLocations();

        loc_animatedTexture = getUniformLocation("animatedTexture");
        loc_texCoordInfo = getUniformLocation("texCoordInfo");
        loc_texOffset = getUniformLocation("texOffset");
    }

    @Override
    protected void initUniforms() {
        setTextureOpaqueId(0);
        setColor(new Vector4f(1, 1, 1, 1));
    }

    public void setTextureCoordInfo(Vector2f offset1, Vector2f offset2, float numRows, float blend) {
        setVector(loc_texOffset, offset1.x, offset1.y, offset2.x, offset2.y);
        setVector(loc_texCoordInfo, new Vector2f(numRows, blend));

    }

    private boolean prevAnimatedTexture;

    public void setAnimatedTexture(boolean value) {
        if (prevAnimatedTexture != value) {
            setBoolean(loc_animatedTexture, value);
            prevAnimatedTexture = value;
        }
    }

    public void setTexCoordInfo(Vector2f value) {
        setVector(loc_texCoordInfo, value);
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
}
