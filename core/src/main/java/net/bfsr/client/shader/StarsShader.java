package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

public class StarsShader extends ShaderProgram {
    int loc_textureOpaque;
    int loc_coreColor, loc_haloColor;
    int loc_center, loc_resolution;
    int loc_coreRadius, loc_haloFalloff, loc_scale;

    public StarsShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "stars.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "stars.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_coreColor = getUniformLocation("coreColor");
        loc_haloColor = getUniformLocation("haloColor");

        loc_textureOpaque = getUniformLocation("textureOpaque");

        loc_center = getUniformLocation("center");
        loc_resolution = getUniformLocation("resolution");

        loc_coreRadius = getUniformLocation("coreRadius");
        loc_haloFalloff = getUniformLocation("haloFalloff");
        loc_scale = getUniformLocation("scale");
    }

    @Override
    protected void initUniforms() {
        setTextureOpaqueId(0);
    }

    public void setTextureOpaqueId(int id) {
        setInt(loc_textureOpaque, id);
    }

    public void setCoreColor(Vector3f color) {
        setVector(loc_coreColor, color);
    }

    public void sethaloColor(Vector3f value) {
        setVector(loc_haloColor, value);
    }

    public void setScale(float value) {
        setFloat(loc_scale, value);
    }

    public void setCenter(Vector2f value) {
        setVector(loc_center, value);
    }

    public void setResolution(Vector2f value) {
        setVector(loc_resolution, value);
    }

    public void setFalloff(float value) {
        setFloat(loc_haloFalloff, value);
    }

    public void setCoreRadius(float value) {
        setFloat(loc_coreRadius, value);
    }
}
