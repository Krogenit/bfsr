package net.bfsr.client.shader.primitive;

import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.client.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

public class VertexColorTextureShader extends ShaderProgram {
    public VertexColorTextureShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "vertex_color_texture.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "vertex_color_texture.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {

    }

    @Override
    protected void initUniforms() {

    }
}
