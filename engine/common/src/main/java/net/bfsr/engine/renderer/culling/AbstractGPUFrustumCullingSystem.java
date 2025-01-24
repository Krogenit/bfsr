package net.bfsr.engine.renderer.culling;

import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;

public interface AbstractGPUFrustumCullingSystem {
    void init(AbstractShaderProgram shaderProgram, AbstractRenderer renderer);
    void renderFrustumCulled(int count, AbstractBuffersHolder buffersHolder);
    void reloadShaders();
    void clear();
}
