package net.bfsr.client.shader;

import net.bfsr.client.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

public class ParticleInstancedShader extends ShaderProgram {
    public ParticleInstancedShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "particleInstanced.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "particleInstanced.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {}

    @Override
    protected void initUniforms() {}
}
