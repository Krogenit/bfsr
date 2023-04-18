package net.bfsr.client.renderer.shader;

import net.bfsr.client.renderer.shader.loader.Definition;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;

public class NebulaShader extends ShaderProgram {
    private int loc_textureOpaque, loc_textureNoise;
    private int loc_color;
    private int loc_offset;
    private int loc_scale;
    private int loc_density;
    private int loc_falloff;
    private int loc_textureNoiseSize;
    private int loc_noiseType;
    private int loc_pNoiseRepeatVector;

    public NebulaShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "nebula.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "nebula.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_color = getUniformLocation("color");
        loc_textureOpaque = getUniformLocation("textureOpaque");
        loc_offset = getUniformLocation("offset");
        loc_scale = getUniformLocation("scale");
        loc_density = getUniformLocation("density");
        loc_falloff = getUniformLocation("falloff");
        loc_textureNoise = getUniformLocation("textureNoise");
        loc_textureNoiseSize = getUniformLocation("textureNoiseSize");
        loc_noiseType = getUniformLocation("noiseType");
        loc_pNoiseRepeatVector = getUniformLocation("pNoiseRepeatVector");
    }

    @Override
    protected void initUniforms() {
        setTextureOpaqueId(0);
        setNoiseTextureId(1);
    }

    public void setPNoiseRepeatVector(Vector4f value) {
        setVector(loc_pNoiseRepeatVector, value);
    }

    public void setNoiseType(int id) {
        setInt(loc_noiseType, id);
    }

    public void setTextureOpaqueId(int id) {
        setInt(loc_textureOpaque, id);
    }

    public void setNoiseTextureId(int id) {
        setInt(loc_textureNoise, id);
    }

    public void setColor(Vector3f color) {
        setVector(loc_color, color);
    }

    public void setOffset(Vector2f value) {
        setVector(loc_offset, value);
    }

    public void setScale(float value) {
        setFloat(loc_scale, value);
    }

    public void setDensity(float value) {
        setFloat(loc_density, value);
    }

    public void setFalloff(float value) {
        setFloat(loc_falloff, value);
    }

    public void setNoiseSize(float value) {
        setFloat(loc_textureNoiseSize, value);
    }
}