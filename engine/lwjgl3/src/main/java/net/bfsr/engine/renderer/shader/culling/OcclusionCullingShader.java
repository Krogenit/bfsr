package net.bfsr.engine.renderer.shader.culling;

import net.bfsr.engine.renderer.shader.ShaderProgram;
import net.bfsr.engine.renderer.shader.loader.Definition;

import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

public class OcclusionCullingShader extends ShaderProgram {
    public OcclusionCullingShader() {
        super(new Definition(GL_COMPUTE_SHADER, "culling/occlusion-culling.comp.glsl"));
    }
}
