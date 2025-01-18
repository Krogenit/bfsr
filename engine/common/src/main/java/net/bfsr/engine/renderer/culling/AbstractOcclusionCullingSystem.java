package net.bfsr.engine.renderer.culling;

import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;

public interface AbstractOcclusionCullingSystem {
    void init(AbstractShaderProgram shaderProgram);
    void renderOcclusionCulled(int count, AbstractBuffersHolder buffersHolder);
    void reloadShaders();
    void clear();
}
