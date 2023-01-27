package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

public class ParticleInstancedShader extends ShaderProgram {
    private int loc_orthoMat;

    public ParticleInstancedShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "particleInstanced.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "particleInstanced.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {
        loc_orthoMat = getUniformLocation("orthoMat");
    }

    @Override
    protected void initUniforms() {
        setInt(getUniformLocation("textureOpaque"), 0);
    }

    public void setOrthoMatrix(Matrix4f matrix) {
        setMat4(loc_orthoMat, matrix);
    }
}
