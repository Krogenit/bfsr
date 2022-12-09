package net.bfsr.client.shader.font;

import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.client.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

public class FontShader extends ShaderProgram {
    public FontShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "font/font.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "font/font.frag.glsl"));
    }

    @Override
    protected void getAllUniformLocations() {}

    @Override
    protected void initUniforms() {}
}
