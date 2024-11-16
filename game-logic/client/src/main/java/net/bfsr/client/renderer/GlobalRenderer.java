package net.bfsr.client.renderer;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;
import net.bfsr.engine.util.RunnableUtils;

@RequiredArgsConstructor
public class GlobalRenderer {
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractShaderProgram shader = renderer.shader;
    private final AbstractCamera camera = renderer.camera;
    private final AbstractSpriteRenderer spriteRenderer = renderer.spriteRenderer;
    private final AbstractDebugRenderer debugRenderer = renderer.debugRenderer;

    private final GuiManager guiManager;
    private final Profiler profiler;
    private final RenderManager renderManager;
    private final ParticleManager particleManager;
    private final WorldRenderer worldRenderer;

    private Runnable debugBoxesRenderRunnable = RunnableUtils.EMPTY_RUNNABLE;

    public void init() {
        worldRenderer.init();
    }

    public void update() {
        worldRenderer.update();
    }

    public void render(float interpolation) {
        profiler.start("prepareRender");
        worldRenderer.prepareRender(particleManager.getParticlesCount(), interpolation);

        profiler.endStart("setup");
        renderer.resetDrawCalls();
        renderer.glClear();
        camera.calculateInterpolatedViewMatrix(interpolation);
        camera.bindInterpolatedWorldViewMatrix();
        spriteRenderer.bind();
        shader.enable();

        profiler.endStart("worldRenderer");
        worldRenderer.render();

        debugBoxesRenderRunnable.run();

        profiler.endStart("gui");
        camera.bindGUI();
        guiManager.render();
        spriteRenderer.render(BufferType.GUI);
        profiler.end();
    }

    private void renderDebug() {
        debugRenderer.bind();
        renderManager.renderDebug();
        debugRenderer.render(GL.GL_LINE_LOOP);
        debugRenderer.clear();
        spriteRenderer.bind();
        shader.enable();
    }

    public void reloadShaders() {
        shader.delete();
        shader.load();
        shader.init();
    }

    public void createBackgroundTexture(long seed) {
        worldRenderer.createBackgroundTexture(seed);
    }

    public void setDebugBoxesEnabled(boolean value) {
        if (value) {
            debugBoxesRenderRunnable = () -> {
                profiler.endStart("debugRenderer");
                renderDebug();
            };
        } else {
            debugBoxesRenderRunnable = RunnableUtils.EMPTY_RUNNABLE;
        }
    }

    public ParticleRenderer getParticleRenderer() {
        return worldRenderer.getParticleRenderer();
    }
}