package net.bfsr.engine.renderer.shader;

import net.bfsr.engine.renderer.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

public class DebugShader extends ShaderProgram {
    public DebugShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "debug.vert.glsl"), new Definition(GL20.GL_FRAGMENT_SHADER, "debug.frag.glsl"));
    }
}