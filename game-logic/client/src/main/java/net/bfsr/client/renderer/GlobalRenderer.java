package net.bfsr.client.renderer;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import net.bfsr.engine.renderer.debug.AbstractDebugRenderer;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;
import net.bfsr.engine.util.RunnableUtils;
import net.bfsr.engine.world.entity.ParticleManager;
import org.joml.Vector2i;

@RequiredArgsConstructor
public class GlobalRenderer {
    private final AbstractRenderer renderer = Engine.getRenderer();
    private final AbstractShaderProgram shader = renderer.getShader();
    private final AbstractCamera camera = renderer.getCamera();
    private final AbstractSpriteRenderer spriteRenderer = renderer.getSpriteRenderer();
    private final AbstractGUIRenderer guiRenderer = renderer.getGuiRenderer();
    private final AbstractDebugRenderer debugRenderer = renderer.getDebugRenderer();
    private final GuiManager guiManager = Engine.getGuiManager();

    private final Profiler profiler;
    private final EntityRenderer entityRenderer;
    private final ParticleManager particleManager;
    private final WorldRenderer worldRenderer;

    private Runnable debugBoxesRenderRunnable = RunnableUtils.EMPTY_RUNNABLE;

    public void render(float interpolation) {
        profiler.start("prepareRender");
        spriteRenderer.waitForLockedRange();
        worldRenderer.prepareRender(particleManager.getParticlesCount());
        spriteRenderer.updateBuffers();

        profiler.endStart("setup");
        renderer.resetDrawCalls();
        renderer.glClear();
        camera.calculateInterpolatedViewMatrix(interpolation);
        camera.bindWorldViewMatrix();
        shader.enable();

        profiler.endStart("worldRenderer");
        worldRenderer.render();

        debugBoxesRenderRunnable.run();

        profiler.endStart("gui");
        camera.bindGUI();
        shader.enable();
        Vector2i mousePosition = guiManager.getMousePosition();
        guiRenderer.render(guiManager.getGuiStack(), mousePosition.x, mousePosition.y);
        profiler.end();
    }

    private void renderDebug() {
        entityRenderer.renderDebug();
        debugRenderer.render(GL.GL_LINE_LOOP);
        debugRenderer.reset();
        shader.enable();
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