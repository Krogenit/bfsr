package net.bfsr.client.renderer;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.client.event.gui.ExitToMainMenuEvent;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.engine.Engine;
import net.bfsr.engine.event.EventHandler;
import net.bfsr.engine.event.EventListener;
import net.bfsr.engine.profiler.Profiler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.culling.AbstractGPUFrustumCullingSystem;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.particle.ParticleRenderer;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class WorldRenderer {
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractSpriteRenderer spriteRenderer = renderer.spriteRenderer;

    private final Profiler profiler;
    private final EntityRenderer entityRenderer;
    @Getter
    private final ParticleRenderer particleRenderer = renderer.particleRenderer;
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final AbstractGPUFrustumCullingSystem cullingSystem = renderer.cullingSystem;

    public WorldRenderer(Profiler profiler, EntityRenderer entityRenderer) {
        this.profiler = profiler;
        this.entityRenderer = entityRenderer;
    }

    public void init() {
        if (ClientSettings.IS_DEBUG.getBoolean()) {
            renderer.setDebugWindow();
        }

        Engine.assetsManager.getTexture(TextureRegister.damageFire).bind();

        backgroundRenderer.init();

        Client.get().getEventBus().register(this);
    }

    void prepareRender(int totalParticlesCount) {
        particleRenderer.putBackgroundParticlesToBuffers(totalParticlesCount);
        backgroundRenderer.render();
        entityRenderer.renderAlpha();
        entityRenderer.renderAdditive();
        particleRenderer.putParticlesToBuffers();
    }

    public void render() {
        profiler.start("background");
        spriteRenderer.render(BufferType.BACKGROUND);

        profiler.endStart("particlesBackground");
        profiler.start("waitTasks");
        particleRenderer.waitBackgroundTasks();
        profiler.end();
        particleRenderer.renderBackground();

        profiler.endStart("entitiesAlpha");
        renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        if (renderer.isEntitiesGPUFrustumCulling()) {
            AbstractBuffersHolder buffersHolder = spriteRenderer.getBuffersHolder(BufferType.ENTITIES_ALPHA);
            cullingSystem.renderFrustumCulled(buffersHolder.getRenderObjects(), buffersHolder);
            buffersHolder.setRenderObjects(0);
            profiler.endStart("entitiesAdditive");
            renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
            buffersHolder = spriteRenderer.getBuffersHolder(BufferType.ENTITIES_ADDITIVE);
            cullingSystem.renderFrustumCulled(buffersHolder.getRenderObjects(), buffersHolder);
            buffersHolder.setRenderObjects(0);
        } else {
            spriteRenderer.render(BufferType.ENTITIES_ALPHA);
            profiler.endStart("entitiesAdditive");
            renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
            spriteRenderer.render(BufferType.ENTITIES_ADDITIVE);
        }

        profiler.endStart("particles");
        profiler.start("waitTasks");
        particleRenderer.waitTasks();
        profiler.end();
        particleRenderer.render();
        renderer.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        profiler.end();
    }

    @EventHandler
    public EventListener<ExitToMainMenuEvent> exitToMainMenuEvent() {
        return event -> {
            particleRenderer.removeAllRenders();
            backgroundRenderer.clear();
        };
    }

    public void clear() {
        particleRenderer.clear();
        backgroundRenderer.clear();
    }
}