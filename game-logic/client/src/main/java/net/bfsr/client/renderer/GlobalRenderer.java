package net.bfsr.client.renderer;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.particle.ParticleManager;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;

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

    public void init() {
        worldRenderer.init();
    }

    public void update() {
        worldRenderer.update();
    }

    public void render(float interpolation) {
        profiler.startSection("prepareRender");
        worldRenderer.prepareRender(particleManager.getParticlesCount(), interpolation);

        profiler.endStartSection("globalRenderer.setup");
        renderer.resetDrawCalls();
        renderer.glClear();
        camera.calculateInterpolatedViewMatrix(interpolation);
        camera.bindInterpolatedWorldViewMatrix();
        spriteRenderer.bind();
        shader.enable();

        profiler.endStartSection("worldRenderer.render");
        worldRenderer.render();

        if (ClientSettings.SHOW_DEBUG_BOXES.getBoolean()) {
            profiler.endStartSection("debugRenderer");
            renderDebug();
        }

        profiler.endStartSection("guiManager.render");
        camera.bindGUI();
        guiManager.render();
        spriteRenderer.render(BufferType.GUI);

        profiler.endSection();
    }

    private void renderDebug() {
        debugRenderer.clear();
        debugRenderer.bind();
        camera.bindWorldViewMatrix();
        renderManager.renderDebug();
        debugRenderer.render(GL.GL_LINE_LOOP);
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

    public ParticleRenderer getParticleRenderer() {
        return worldRenderer.getParticleRenderer();
    }
}