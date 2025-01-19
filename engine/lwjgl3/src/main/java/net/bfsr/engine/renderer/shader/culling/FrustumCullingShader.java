package net.bfsr.engine.renderer.shader.culling;

import net.bfsr.engine.renderer.shader.ShaderProgram;
import net.bfsr.engine.renderer.shader.loader.Definition;

import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

public class FrustumCullingShader extends ShaderProgram {
    public FrustumCullingShader() {
        super(new Definition(GL_COMPUTE_SHADER, "culling/occlusion-culling.comp.glsl"));
    }
}
