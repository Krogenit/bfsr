package net.bfsr.engine.renderer.shader.postprocessing;

import net.bfsr.engine.renderer.shader.ShaderProgram;
import net.bfsr.engine.renderer.shader.loader.Definition;
import org.lwjgl.opengl.GL20;

public class SimpleShader extends ShaderProgram {
    public SimpleShader() {
        super(new Definition(GL20.GL_VERTEX_SHADER, "postprocessing/simple.vert.glsl"),
                new Definition(GL20.GL_FRAGMENT_SHADER, "postprocessing/simple.frag.glsl"));
    }
}
