package net.bfsr.engine.renderer.shader;

import net.bfsr.engine.renderer.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

public class BaseShader extends ShaderProgram {
    public BaseShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "base.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "base.frag.glsl"));
    }
}